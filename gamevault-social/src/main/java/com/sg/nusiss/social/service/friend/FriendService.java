package com.sg.nusiss.social.service.friend;


import com.sg.nusiss.common.domain.ErrorCode;
import com.sg.nusiss.common.dto.UserDTO;
import com.sg.nusiss.common.exception.BusinessException;
import com.sg.nusiss.social.dto.friend.response.FriendRequestResponse;
import com.sg.nusiss.social.dto.friend.response.FriendResponse;
import com.sg.nusiss.social.dto.friend.response.UserSearchResponse;
import com.sg.nusiss.social.entity.friend.FriendRequest;
import com.sg.nusiss.social.entity.friend.Friendship;
import com.sg.nusiss.social.repository.friend.FriendRequestRepository;
import com.sg.nusiss.social.repository.friend.FriendshipRepository;
import com.sg.nusiss.social.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @ClassName FriendService
 * @Author HUANG ZHENJIA
 * @Date 2025/9/29
 * @Description
 */
@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserService userService;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;

    /**
     * 搜索用户
     */
    public List<UserSearchResponse> searchUsers(String keyword, Long currentUserId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜索关键词不能为空");
        }

        List<UserDTO> users = userService.searchUsers(keyword.trim());

        return users.stream()
                .filter(user -> !user.getUserId().equals(currentUserId)) // 排除自己
                .map(user -> {
                    // 检查是否已是好友
                    boolean isFriend = friendshipRepository
                            .findByUserIdAndFriendIdAndIsActive(currentUserId, user.getUserId(), true)
                            .isPresent();

                    // 检查是否有待处理的请求
                    boolean hasPending = friendRequestRepository
                            .findExistingRequest(currentUserId, user.getUserId(), "pending")
                            .isPresent();

                    return new UserSearchResponse(
                            user.getUserId(),
                            user.getUsername(),
                            user.getEmail(),
                            isFriend,
                            hasPending
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 发送好友请求
     */
    @Transactional
    public void sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        // 验证目标用户存在
        UserDTO toUSer = userService.getUserById(toUserId);
        if (toUSer == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 不能添加自己
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能添加自己为好友");
        }

        // 检查是否已是好友
        if (friendshipRepository.findByUserIdAndFriendIdAndIsActive(fromUserId, toUserId, true).isPresent()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "已经是好友了");
        }

        // 检查是否有待处理的请求
        Optional<FriendRequest> existingRequest =
                friendRequestRepository.findExistingRequest(fromUserId, toUserId, "pending");

        if (existingRequest.isPresent()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "已有待处理的好友请求");
        }

        // 创建好友请求
        FriendRequest request = new FriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setMessage(message);
        request.setStatus("pending");

        friendRequestRepository.save(request);
    }

    /**
     * 获取收到的好友请求
     */
    public List<FriendRequestResponse> getReceivedRequests(Long userId) {
        List<FriendRequest> requests = friendRequestRepository
                .findByToUserIdAndStatus(userId, "pending");

        return requests.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取发送的好友请求
     */
    public List<FriendRequestResponse> getSentRequests(Long userId) {
        List<FriendRequest> requests = friendRequestRepository
                .findByFromUserIdAndStatus(userId, "pending");

        return requests.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 处理好友请求
     */
    @Transactional
    public void handleFriendRequest(Long requestId, Boolean accept, Long currentUserId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "好友请求不存在"));

        // 验证权限（只有接收者可以处理）
        if (!request.getToUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权处理此请求");
        }

        // 验证状态
        if (!"pending".equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求已处理");
        }

        if (accept) {
            // 接受请求，建立好友关系
            request.setStatus("accepted");
            createFriendship(request.getFromUserId(), request.getToUserId());
        } else {
            // 拒绝请求
            request.setStatus("rejected");
        }

        request.setHandledAt(LocalDateTime.now());
        friendRequestRepository.save(request);
    }

    /**
     * 创建双向好友关系
     */
    private void createFriendship(Long userId1, Long userId2) {
        LocalDateTime now = LocalDateTime.now();

        // 创建 userId1 -> userId2 关系
        Friendship friendship1 = new Friendship();
        friendship1.setUserId(userId1);
        friendship1.setFriendId(userId2);
        friendship1.setCreatedAt(now);
        friendship1.setIsActive(true);

        // 创建 userId2 -> userId1 关系
        Friendship friendship2 = new Friendship();
        friendship2.setUserId(userId2);
        friendship2.setFriendId(userId1);
        friendship2.setCreatedAt(now);
        friendship2.setIsActive(true);

        friendshipRepository.save(friendship1);
        friendshipRepository.save(friendship2);
    }

    /**
     * 获取好友列表
     */
    public List<FriendResponse> getFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository
                .findByUserIdAndIsActive(userId, true);

        return friendships.stream()
                .map(friendship -> {
                    UserDTO friend = userService.getUserById(friendship.getFriendId());

                    if (friend == null) {
                        throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "好友信息不存在");
                    }
                    return new FriendResponse(
                            friend.getUserId(),
                            friend.getUsername(),
                            friend.getEmail(),
                            friendship.getRemark(),
                            friendship.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 删除好友（逻辑删除）
     */
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        // 删除双向关系
        Friendship friendship1 = friendshipRepository
                .findByUserIdAndFriendIdAndIsActive(userId, friendId, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "好友关系不存在"));

        Friendship friendship2 = friendshipRepository
                .findByUserIdAndFriendIdAndIsActive(friendId, userId, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "好友关系不存在"));

        LocalDateTime now = LocalDateTime.now();

        friendship1.setIsActive(false);
        friendship1.setDeletedAt(now);

        friendship2.setIsActive(false);
        friendship2.setDeletedAt(now);

        friendshipRepository.save(friendship1);
        friendshipRepository.save(friendship2);
    }

    /**
     * 转换为响应对象
     */
    private FriendRequestResponse convertToResponse(FriendRequest request) {
        // 1. 查询发送者信息
        UserDTO fromUser = userService.getUserById(request.getFromUserId());
        if (fromUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "发送者不存在");
        }

        // 2. 查询接收者信息
        UserDTO toUser = userService.getUserById(request.getToUserId());
        if (toUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接收者不存在");
        }

        // 3. 组装响应
        return new FriendRequestResponse(
                request.getId(),
                fromUser.getUserId(),
                fromUser.getUsername(),
                fromUser.getEmail(),
                toUser.getUserId(),
                toUser.getUsername(),
                toUser.getEmail(),
                request.getMessage(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getHandledAt()
        );
    }
}