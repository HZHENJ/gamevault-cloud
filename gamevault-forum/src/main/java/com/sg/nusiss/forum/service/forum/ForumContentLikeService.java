package com.sg.nusiss.forum.service.forum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sg.nusiss.forum.constant.ForumRelationType;
import com.sg.nusiss.forum.entity.UserContentRelation;
import com.sg.nusiss.forum.repository.ForumContentLikeMapper;
import com.sg.nusiss.forum.repository.ForumMetricMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内容点赞服务（基于 user_content_relations 表）
 *
 * 位置: gamevault-forum/src/main/java/sg/edu/nus/gamevaultforum/service/forum/ForumContentLikeService.java
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForumContentLikeService {

    private final ForumContentLikeMapper contentLikeMapper;
    private final ForumMetricMapper metricMapper;

    /**
     * 点赞内容
     */
    @Transactional
    public boolean likeContent(Long contentId, Long userId) {
        if (contentId == null || userId == null) {
            throw new IllegalArgumentException("内容ID和用户ID不能为空");
        }

        // 检查是否已点赞
        if (contentLikeMapper.existsByUserAndContentAndType(userId, contentId, ForumRelationType.LIKE.intValue())) {
            return false;
        }

        // 创建点赞关系
        UserContentRelation relation = new UserContentRelation(userId, contentId, ForumRelationType.LIKE.intValue());
        relation.setCreatedDate(LocalDateTime.now());

        int inserted = contentLikeMapper.insert(relation);
        return inserted > 0;
    }

    /**
     * 取消点赞
     */
    @Transactional
    public boolean unlikeContent(Long contentId, Long userId) {
        if (contentId == null || userId == null) {
            throw new IllegalArgumentException("内容ID和用户ID不能为空");
        }

        if (!contentLikeMapper.existsByUserAndContentAndType(userId, contentId, ForumRelationType.LIKE.intValue())) {
            return false;
        }

        int deleted = contentLikeMapper.deleteByUserAndContentAndType(userId, contentId, ForumRelationType.LIKE.intValue());
        return deleted > 0;
    }

    /**
     * 切换点赞状态
     */
    @Transactional
    public boolean toggleLike(Long contentId, Long userId) {
        if (contentLikeMapper.existsByUserAndContentAndType(userId, contentId, ForumRelationType.LIKE.intValue())) {
            unlikeContent(contentId, userId);
            return false;
        } else {
            likeContent(contentId, userId);
            return true;
        }
    }

    /**
     * 检查用户是否已点赞某内容
     */
    public boolean isLiked(Long contentId, Long userId) {
        if (contentId == null || userId == null) {
            return false;
        }
        return contentLikeMapper.existsByUserAndContentAndType(userId, contentId, ForumRelationType.LIKE.intValue());
    }

    /**
     * 获取内容的点赞数
     */
    public int getLikeCount(Long contentId) {
        if (contentId == null) {
            return 0;
        }
        return contentLikeMapper.countByContentAndType(contentId, ForumRelationType.LIKE.intValue());
    }

    /**
     * 获取内容的所有点赞用户ID
     */
    public List<Long> getLikedUserIds(Long contentId) {
        if (contentId == null) {
            throw new IllegalArgumentException("内容ID不能为空");
        }
        return contentLikeMapper.findUserIdsByContentAndType(contentId, ForumRelationType.LIKE.intValue());
    }

    /**
     * 获取用户点赞的所有内容ID
     */
    public List<Long> getUserLikedContentIds(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return contentLikeMapper.findContentIdsByUserAndType(userId, ForumRelationType.LIKE.intValue());
    }

    /**
     * 批量获取用户对多个内容的点赞状态
     */
    public Map<Long, Boolean> batchCheckLikeStatus(Long userId, List<Long> contentIds) {
        if (userId == null || contentIds == null || contentIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Long> likedContentIds = contentLikeMapper
                .findLikedContentIdsByUserAndType(userId, contentIds, ForumRelationType.LIKE.intValue());

        Map<Long, Boolean> result = new HashMap<>();
        for (Long contentId : contentIds) {
            result.put(contentId, likedContentIds.contains(contentId));
        }

        return result;
    }

    /**
     * 批量获取多个内容的点赞数
     */
    public Map<Long, Integer> batchGetLikeCounts(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return new HashMap<>();
        }
        return metricMapper.getBatchMetrics(contentIds, "like_count");
    }

    /**
     * 获取用户最近点赞的内容
     */
    public List<UserContentRelation> getUserRecentLikes(Long userId, int limit) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return contentLikeMapper.findRecentByUserAndType(userId, ForumRelationType.LIKE.intValue(), limit);
    }

    /**
     * 获取内容的最近点赞记录
     */
    public List<UserContentRelation> getContentRecentLikes(Long contentId, int limit) {
        if (contentId == null) {
            throw new IllegalArgumentException("内容ID不能为空");
        }
        return contentLikeMapper.findRecentByContentAndType(contentId, ForumRelationType.LIKE.intValue(), limit);
    }

    /**
     * 同步点赞数
     */
    @Transactional
    public void syncLikeCount(Long contentId) {
        if (contentId == null) {
            throw new IllegalArgumentException("内容ID不能为空");
        }

        int actualCount = contentLikeMapper.countByContentAndType(contentId, ForumRelationType.LIKE.intValue());
        metricMapper.setMetricValue(contentId, "like_count", actualCount);
    }

    /**
     * 批量同步点赞数
     */
    @Transactional
    public void batchSyncLikeCounts(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return;
        }

        for (Long contentId : contentIds) {
            syncLikeCount(contentId);
        }
    }

    /**
     * 获取热门内容（按点赞数排序）
     */
    public List<Long> getTopLikedContents(int limit) {
        return metricMapper.findTopContentsByMetric("like_count", limit);
    }
}