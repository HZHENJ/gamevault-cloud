package com.sg.nusiss.forum.controller;

import com.sg.nusiss.common.domain.BaseResponse;
import com.sg.nusiss.common.domain.ErrorCode;
import com.sg.nusiss.common.domain.ResultUtils;
import com.sg.nusiss.common.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.sg.nusiss.forum.dto.PostDTO;
import com.sg.nusiss.forum.dto.PostResponseDTO;
import com.sg.nusiss.forum.dto.ReplyResponseDTO;
import com.sg.nusiss.forum.entity.ForumContent;
import com.sg.nusiss.forum.service.forum.ForumContentLikeService;
import com.sg.nusiss.forum.service.forum.ForumPostService;
import com.sg.nusiss.forum.service.forum.ViewTracker;
import com.sg.nusiss.forum.service.user.UserService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import com.sg.nusiss.forum.annotation.RequireForumAuth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帖子控制器
 * 提供帖子相关的 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/forum/posts")
@RequiredArgsConstructor
public class ForumPostController {

    private final ForumPostService postService;
    private final UserService userService;
    private final ForumContentLikeService contentLikeService;
    private final ViewTracker viewTracker;

    /**
     * 获取帖子列表（分页）
     */
    @GetMapping
    public BaseResponse<?> getPostList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {  // ✅ 添加参数

        log.info("获取帖子列表 - 页码: {}, 每页大小: {}", page, size);

        try {
            Long currentUserId = getCurrentUserIdOrNull(request);  // ✅ 传入request

            List<ForumContent> posts = postService.getPostList(page, size, currentUserId);
            int totalCount = postService.getPostCount();

            List<PostResponseDTO> postDTOs = convertToResponseDTOs(posts);

            Map<String, Object> response = new HashMap<>();
            response.put("posts", postDTOs);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalCount", totalCount);

            return ResultUtils.success(response);

        } catch (Exception e) {
            log.error("获取帖子列表失败", e);
            return ResultUtils.error(50000, "获取帖子列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取帖子详情
     */
    @GetMapping("/{id}")
    public BaseResponse<?> getPostById(
            @PathVariable Long id,
            HttpServletRequest request) {  // ✅ 添加参数

        log.info("获取帖子详情 - 帖子ID: {}", id);

        try {
            Long currentUserId = getCurrentUserIdOrNull(request);  // ✅ 传入request

            ForumContent post = postService.getPostById(id, currentUserId);

            // 只有登录用户的浏览才计入统计
            if (currentUserId != null) {
                boolean shouldIncrement = viewTracker.shouldIncrementView(currentUserId, null, id);

                if (shouldIncrement) {
                    postService.incrementViewCount(id);
                    log.debug("浏览量+1 - 帖子ID: {}, 用户ID: {}", id, currentUserId);
                } else {
                    log.debug("浏览量不变 - 用户{}在5分钟内重复访问帖子{}", currentUserId, id);
                }
            } else {
                log.debug("未登录用户访问 - 不计入浏览量 - 帖子ID: {}", id);
            }

            // 获取作者信息
            UserDTO author = userService.getUserById(post.getAuthorId());
            PostResponseDTO dto = PostResponseDTO.fromContentAndUser(post, author);

            return ResultUtils.success(Map.of("post", dto));

        } catch (IllegalArgumentException e) {
            log.warn("参数错误: {}", e.getMessage());
            return ResultUtils.error(40000, e.getMessage());

        } catch (RuntimeException e) {
            log.warn("帖子不存在: {}", e.getMessage());
            return ResultUtils.error(40400, e.getMessage());

        } catch (Exception e) {
            log.error("获取帖子详情失败", e);
            return ResultUtils.error(50000, "获取帖子详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建新帖子
     */
    @PostMapping
    @RequireForumAuth
    public BaseResponse<?> createPost(
            @Valid @RequestBody PostDTO postDTO,
            HttpServletRequest request) {

        try {
            // ✅ 获取当前用户ID
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null) {
                log.warn("用户未登录 - 无法创建帖子");
                return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
            }

            log.info("创建帖子 - 用户ID: {}, 标题: {}", userId, postDTO.getTitle());

            // 创建帖子
            ForumContent post = postService.createPost(postDTO.getTitle(), postDTO.getBody(), userId);

            // 获取作者信息
            UserDTO author = userService.getUserById(userId);
            PostResponseDTO dto = PostResponseDTO.fromContentAndUser(post, author);

            Map<String, Object> response = new HashMap<>();
            response.put("post", dto);
            response.put("message", "帖子创建成功");

            log.info("帖子创建成功 - 帖子ID: {}", post.getContentId());
            return ResultUtils.success(response);

        } catch (IllegalArgumentException e) {
            log.warn("参数错误: {}", e.getMessage());
            return ResultUtils.error(40000, e.getMessage());

        } catch (Exception e) {
            log.error("创建帖子失败", e);
            return ResultUtils.error(50000, "创建帖子失败: " + e.getMessage());
        }
    }

    /**
     * 搜索帖子
     */
    @GetMapping("/search")
    public BaseResponse<?> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {  // ✅ 添加参数

        log.info("搜索帖子 - 关键词: {}", keyword);

        try {
            Long currentUserId = getCurrentUserIdOrNull(request);  // ✅ 传入request

            List<ForumContent> posts = postService.searchPosts(keyword, page, size, currentUserId);
            int totalCount = postService.getSearchCount(keyword);

            List<PostResponseDTO> postDTOs = convertToResponseDTOs(posts);

            Map<String, Object> response = new HashMap<>();
            response.put("posts", postDTOs);
            response.put("keyword", keyword);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalCount", totalCount);

            return ResultUtils.success(response);

        } catch (Exception e) {
            log.error("搜索帖子失败", e);
            return ResultUtils.error(50000, "搜索帖子失败: " + e.getMessage());
        }
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/{id}")
    @RequireForumAuth
    public BaseResponse<?> deletePost(
            @PathVariable Long id,
            HttpServletRequest request) {

        try {
            Long userId = (Long) request.getAttribute("userId");  // ✅ 修正类型转换

            if (userId == null) {
                return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
            }

            log.info("删除帖子 - 帖子ID: {}, 用户ID: {}", id, userId);

            postService.deletePost(id, userId);

            return ResultUtils.success(Map.of("message", "帖子删除成功"));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("权限")) {
                return ResultUtils.error(40300, e.getMessage());
            } else if (e.getMessage().contains("不存在")) {
                return ResultUtils.error(40400, e.getMessage());
            } else {
                return ResultUtils.error(50000, e.getMessage());
            }
        } catch (Exception e) {
            log.error("删除帖子失败", e);
            return ResultUtils.error(50000, "删除帖子失败: " + e.getMessage());
        }
    }

    /**
     * 点赞帖子
     */
    @PostMapping("/{id}/like")
    @RequireForumAuth
    public BaseResponse<?> likePost(
            @PathVariable Long id,
            HttpServletRequest request) {  // ✅ 添加参数
        try {
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null) {
                return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
            }

            boolean result = contentLikeService.likeContent(id, userId);

            if (result) {
                int newCount = contentLikeService.getLikeCount(id);
                return ResultUtils.success(Map.of(
                        "message", "点赞成功",
                        "likeCount", newCount
                ));
            } else {
                return ResultUtils.error(40000, "已经点赞过了");
            }
        } catch (Exception e) {
            log.error("点赞失败", e);
            return ResultUtils.error(50000, "点赞失败: " + e.getMessage());
        }
    }

    /**
     * 取消点赞帖子
     */
    @DeleteMapping("/{id}/like")
    @RequireForumAuth
    public BaseResponse<?> unlikePost(
            @PathVariable Long id,
            HttpServletRequest request) {  // ✅ 添加参数
        try {
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null) {
                return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
            }

            boolean result = contentLikeService.unlikeContent(id, userId);

            if (result) {
                int newCount = contentLikeService.getLikeCount(id);
                return ResultUtils.success(Map.of(
                        "message", "取消点赞成功",
                        "likeCount", newCount
                ));
            } else {
                return ResultUtils.error(40000, "未点赞过");
            }
        } catch (Exception e) {
            log.error("取消点赞失败", e);
            return ResultUtils.error(50000, "取消点赞失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户发布的帖子
     */
    @GetMapping("/user/{userId}")
    public BaseResponse<?> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {  // ✅ 添加参数

        log.info("获取用户帖子列表 - 用户ID: {}", userId);

        try {
            Long currentUserId = getCurrentUserIdOrNull(request);  // ✅ 传入request

            List<ForumContent> posts = postService.getPostsByAuthorId(userId, page, size, currentUserId);
            int totalCount = postService.getPostCountByAuthorId(userId);

            List<PostResponseDTO> postDTOs = convertToResponseDTOs(posts);

            Map<String, Object> response = new HashMap<>();
            response.put("posts", postDTOs);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalCount", totalCount);

            return ResultUtils.success(response);

        } catch (Exception e) {
            log.error("获取用户帖子列表失败", e);
            return ResultUtils.error(50000, "获取用户帖子列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取帖子的回复列表
     */
    @GetMapping("/{postId}/replies")
    public BaseResponse<?> getReplies(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {  // ✅ 添加参数
        try {
            Long currentUserId = getCurrentUserIdOrNull(request);  // ✅ 传入request

            List<ForumContent> replies = postService.getRepliesByPostId(postId, page, size, currentUserId);

            // 转换为 DTO 列表
            List<ReplyResponseDTO> replyDTOs = new ArrayList<>();

            for (ForumContent reply : replies) {
                // 获取回复作者信息
                UserDTO author = userService.getUserById(reply.getAuthorId());

                // 如果是楼中楼回复，获取被回复用户的信息
                UserDTO replyToUser = null;
                if (reply.getReplyTo() != null) {
                    ForumContent targetReply = postService.getContentById(reply.getReplyTo());
                    if (targetReply != null) {
                        replyToUser = userService.getUserById(targetReply.getAuthorId());
                    }
                }

                ReplyResponseDTO dto = ReplyResponseDTO.fromContentAndUsers(reply, author, replyToUser);
                replyDTOs.add(dto);
            }

            int total = postService.getReplyCountByPostId(postId);

            Map<String, Object> response = new HashMap<>();
            response.put("replies", replyDTOs);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);

            return ResultUtils.success(response);

        } catch (Exception e) {
            log.error("获取回复列表失败", e);
            return ResultUtils.error(50000, "获取回复列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建回复
     */
    @PostMapping("/{postId}/replies")
    @RequireForumAuth
    public BaseResponse<?> createReply(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> requestBody,  // ✅ 改名为 requestBody
            HttpServletRequest request) {  // ✅ 添加 HttpServletRequest

        try {
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null) {
                return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
            }

            String body = (String) requestBody.get("body");  // ✅ 使用 requestBody
            Long replyTo = requestBody.get("replyTo") != null
                    ? Long.valueOf(requestBody.get("replyTo").toString())
                    : null;

            log.info("创建回复 - 帖子ID: {}, 用户ID: {}, replyTo: {}", postId, userId, replyTo);

            if (body == null || body.trim().isEmpty()) {
                return ResultUtils.error(40000, "回复内容不能为空");
            }

            ForumContent reply = postService.createReply(postId, body, userId, replyTo);

            // 获取作者信息
            UserDTO author = userService.getUserById(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("reply", reply);
            response.put("authorName", author != null ? author.getUsername() : null);
            response.put("message", "回复创建成功");

            log.info("回复创建成功 - 回复ID: {}", reply.getContentId());
            return ResultUtils.success(response);

        } catch (IllegalArgumentException e) {
            log.warn("参数错误: {}", e.getMessage());
            return ResultUtils.error(40000, e.getMessage());

        } catch (Exception e) {
            log.error("创建回复失败", e);
            return ResultUtils.error(50000, "创建回复失败: " + e.getMessage());
        }
    }

    /**
     * 删除回复
     */
    @DeleteMapping("/{postId}/replies/{replyId}")
    @RequireForumAuth
    public BaseResponse<?> deleteReply(
            @PathVariable Long postId,
            @PathVariable Long replyId,
            HttpServletRequest request) {  // ✅ 添加参数

        try {
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null) {
                return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
            }

            log.info("删除回复 - 帖子ID: {}, 回复ID: {}, 用户ID: {}", postId, replyId, userId);

            postService.deleteReply(replyId, userId);

            return ResultUtils.success(Map.of("message", "回复删除成功"));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("权限")) {
                return ResultUtils.error(40300, e.getMessage());
            } else if (e.getMessage().contains("不存在")) {
                return ResultUtils.error(40400, e.getMessage());
            } else {
                return ResultUtils.error(50000, e.getMessage());
            }
        } catch (Exception e) {
            log.error("删除回复失败", e);
            return ResultUtils.error(50000, "删除回复失败: " + e.getMessage());
        }
    }

    /**
     * 点赞回复
     */
    @PostMapping("/{postId}/replies/{replyId}/like")
    @RequireForumAuth
    public BaseResponse<?> likeReply(
            @PathVariable Long postId,
            @PathVariable Long replyId,
            HttpServletRequest request) {  // ✅ 添加参数
        try {
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null) {
                return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
            }

            boolean result = contentLikeService.likeContent(replyId, userId);

            if (result) {
                int newCount = contentLikeService.getLikeCount(replyId);
                return ResultUtils.success(Map.of(
                        "message", "点赞成功",
                        "likeCount", newCount
                ));
            } else {
                return ResultUtils.error(40000, "已经点赞过了");
            }
        } catch (Exception e) {
            log.error("点赞回复失败", e);
            return ResultUtils.error(50000, "点赞回复失败: " + e.getMessage());
        }
    }

    /**
     * 取消点赞回复
     */
    @DeleteMapping("/{postId}/replies/{replyId}/like")
    @RequireForumAuth
    public BaseResponse<?> unlikeReply(
            @PathVariable Long postId,
            @PathVariable Long replyId,
            HttpServletRequest request) {  // ✅ 添加参数
        try {
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null) {
                return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
            }

            boolean result = contentLikeService.unlikeContent(replyId, userId);

            if (result) {
                int newCount = contentLikeService.getLikeCount(replyId);
                return ResultUtils.success(Map.of(
                        "message", "取消点赞成功",
                        "likeCount", newCount
                ));
            } else {
                return ResultUtils.error(40000, "未点赞过");
            }
        } catch (Exception e) {
            log.error("取消点赞回复失败", e);
            return ResultUtils.error(50000, "取消点赞回复失败: " + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取当前用户ID（可能为null）
     */
    private Long getCurrentUserIdOrNull(HttpServletRequest request) {
        try {
            return (Long) request.getAttribute("userId");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 转换为响应 DTO 列表
     */
    private List<PostResponseDTO> convertToResponseDTOs(List<ForumContent> posts) {
        List<PostResponseDTO> postDTOs = new ArrayList<>();
        for (ForumContent post : posts) {
            UserDTO author = userService.getUserById(post.getAuthorId());
            PostResponseDTO dto = PostResponseDTO.fromContentAndUser(post, author);
            postDTOs.add(dto);
        }
        return postDTOs;
    }
}