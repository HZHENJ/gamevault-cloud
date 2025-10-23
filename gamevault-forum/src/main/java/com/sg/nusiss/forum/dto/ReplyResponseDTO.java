package com.sg.nusiss.forum.dto;

import com.sg.nusiss.forum.entity.ForumContent;
import com.sg.nusiss.common.dto.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回复响应数据传输对象
 * 用于向前端返回回复信息（包含统计数据、作者信息和回复关系）
 */
@Data
public class ReplyResponseDTO {

    private Long replyId;           // 回复ID (对应 contentId)
    private Long parentId;          // 父帖子ID
    private Long replyTo;           // 回复的目标回复ID (楼中楼)
    private String replyToName;     // 被回复用户的名称
    private String body;            // 回复内容
    private String bodyPlain;       // 纯文本内容
    private Long authorId;          // 作者ID
    private String authorName;      // 作者用户名
    private String authorAvatar;    // 作者头像
    private Integer likeCount;      // 点赞数
    private Boolean isLiked;        // 当前用户是否点赞
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public ReplyResponseDTO() {}

    /**
     * 从 ForumContent 实体创建 DTO（不包含用户信息）
     */
    public static ReplyResponseDTO fromContent(ForumContent content) {
        ReplyResponseDTO dto = new ReplyResponseDTO();
        dto.replyId = content.getContentId();
        dto.parentId = content.getParentId();
        dto.replyTo = content.getReplyTo();
        dto.body = content.getBody();
        dto.bodyPlain = content.getBodyPlain();
        dto.authorId = content.getAuthorId();
        dto.createdDate = content.getCreatedDate();
        dto.updatedDate = content.getUpdatedDate();
        dto.likeCount = content.getLikeCount() != null ? content.getLikeCount() : 0;
        dto.isLiked = content.getIsLikedByCurrentUser() != null ? content.getIsLikedByCurrentUser() : false;
        return dto;
    }

    /**
     * 从 ForumContent 和 UserDTO 创建完整的 DTO
     * @param content 回复内容
     * @param author 作者信息（来自 Auth 服务）
     */
    public static ReplyResponseDTO fromContentAndUser(ForumContent content, UserDTO author) {
        ReplyResponseDTO dto = fromContent(content);
        if (author != null) {
            dto.authorName = author.getUsername();
            dto.authorAvatar = author.getAvatarUrl();
        }
        return dto;
    }

    /**
     * 从 ForumContent、作者信息和被回复用户信息创建完整的 DTO
     * @param content 回复内容
     * @param author 作者信息（来自 Auth 服务）
     * @param replyToUser 被回复用户信息（来自 Auth 服务）
     */
    public static ReplyResponseDTO fromContentAndUsers(
            ForumContent content,
            UserDTO author,
            UserDTO replyToUser
    ) {
        ReplyResponseDTO dto = fromContentAndUser(content, author);
        if (replyToUser != null && content.getReplyTo() != null) {
            dto.replyToName = replyToUser.getUsername();
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

    /**
     * 设置被回复用户信息（用于批量设置）
     */
    public void setReplyToUserInfo(UserDTO replyToUser) {
        if (replyToUser != null) {
            this.replyToName = replyToUser.getUsername();
        }
    }
}