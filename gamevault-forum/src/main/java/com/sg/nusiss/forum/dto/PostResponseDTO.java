package com.sg.nusiss.forum.dto;

import com.sg.nusiss.forum.entity.ForumContent;
import com.sg.nusiss.common.dto.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子响应数据传输对象
 * 用于向前端返回帖子信息（包含统计数据和作者信息）
 */
@Data
public class PostResponseDTO {

    private Long contentId;
    private String title;
    private String body;
    private String bodyPlain;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private Integer viewCount;
    private Integer likeCount;
    private Integer replyCount;
    private Boolean isLiked;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public PostResponseDTO() {}

    /**
     * 从 Content 实体创建 DTO（不包含用户信息）
     */
    public static PostResponseDTO fromContent(ForumContent content) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.contentId = content.getContentId();
        dto.title = content.getTitle();
        dto.body = content.getBody();
        dto.bodyPlain = content.getBodyPlain();
        dto.authorId = content.getAuthorId();
        dto.createdDate = content.getCreatedDate();
        dto.updatedDate = content.getUpdatedDate();
        // 统计数据
        dto.viewCount = content.getViewCount();
        dto.likeCount = content.getLikeCount();
        dto.replyCount = content.getReplyCount();
        dto.isLiked = content.getIsLikedByCurrentUser();
        return dto;
    }

    /**
     * 从 Content 和 UserDTO 创建完整的 DTO
     * @param content 帖子内容
     * @param author 作者信息（来自 Auth 服务）
     */
    public static PostResponseDTO fromContentAndUser(ForumContent content, UserDTO author) {
        PostResponseDTO dto = fromContent(content);
        if (author != null) {
            dto.authorName = author.getUsername();
            dto.authorAvatar = author.getAvatarUrl();
        }
        return dto;
    }

    /**
     * 设置作者信息（用于批量设置）
     */
    public void setAuthorInfo(UserDTO author) {
        if (author != null) {
            this.authorName = author.getUsername();
            this.authorAvatar = author.getAvatarUrl();
        }
    }
}