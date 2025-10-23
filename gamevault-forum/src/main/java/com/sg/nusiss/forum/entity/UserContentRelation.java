package com.sg.nusiss.forum.entity;

import java.time.LocalDateTime;

/**
 * 用户-内容关系实体（用于点赞、收藏等）
 * 对应数据库 user_content_relations 表
 */

public class UserContentRelation {
    private Long id;
    private Long userId;
    private Long contentId;
    private Integer relationTypeId;  // 关系类型ID（对应 relationship_types 表）
    private LocalDateTime createdDate;

    // 默认构造函数
    public UserContentRelation() {}

    // 构造函数
    public UserContentRelation(Long userId, Long contentId, Integer relationTypeId) {
        this.userId = userId;
        this.contentId = contentId;
        this.relationTypeId = relationTypeId;
        this.createdDate = LocalDateTime.now();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public Integer getRelationTypeId() {
        return relationTypeId;
    }

    public void setRelationType(Integer relationTypeId) {
        this.relationTypeId = relationTypeId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    // 业务方法：判断是否为点赞关系
    public boolean isLike() {
        // 这里可以硬编码，或者从配置/缓存中获取
        // 假设点赞类型ID为1（需要从数据库确认）
        return this.relationTypeId != null && this.relationTypeId == 1L;
    }

    @Override
    public String toString() {
        return "UserContentRelation{" +
                "id=" + id +
                ", userId=" + userId +
                ", contentId=" + contentId +
                ", relationType=" + relationTypeId +
                ", createdDate=" + createdDate +
                '}';
    }
}