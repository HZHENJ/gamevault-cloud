package com.sg.nusiss.social.service.user;

import com.sg.nusiss.common.domain.BaseResponse;
import com.sg.nusiss.common.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String AUTH_SERVICE_URL = "http://gamevault-auth";
    private static final String CACHE_PREFIX = "user:";
    private static final long CACHE_EXPIRE_HOURS = 1;

    /**
     * 根据用户ID查询用户信息（带缓存）
     */
    public UserDTO getUserById(Long userId) {
        if (userId == null) {
            return null;
        }

        // 1. 先查 Redis 缓存
        String cacheKey = CACHE_PREFIX + userId;
        UserDTO cached = (UserDTO) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.debug("从缓存获取用户信息: userId={}", userId);
            return cached;
        }

        // 2. 调用 Auth 服务
        try {
            String url = AUTH_SERVICE_URL + "/api/users/" + userId;

            ResponseEntity<BaseResponse<UserDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<BaseResponse<UserDTO>>() {}
            );

            if (response.getBody() != null && response.getBody().getCode() == 0) {
                UserDTO user = response.getBody().getData();

                if (user != null) {
                    // 3. 存入 Redis 缓存
                    redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

                    log.debug("从 Auth 服务获取用户信息: userId={}", userId);
                    return user;
                }
            }
        } catch (Exception e) {
            log.error("调用 Auth 服务失败: userId={}", userId, e);
        }

        // 返回默认值
        return UserDTO.builder()
                .userId(userId)
                .username("未知用户")
                .email("")
                .build();
    }

    /**
     * 批量查询用户信息
     */
    public List<UserDTO> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String url = AUTH_SERVICE_URL + "/api/users/batch";

            ResponseEntity<BaseResponse<List<UserDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(userIds),
                    new ParameterizedTypeReference<BaseResponse<List<UserDTO>>>() {}
            );

            if (response.getBody() != null && response.getBody().getCode() == 0) {
                return response.getBody().getData();
            }
        } catch (Exception e) {
            log.error("批量查询用户失败: userIds={}", userIds, e);
        }

        return new ArrayList<>();
    }

    /**
     * 搜索用户
     */
    public List<UserDTO> searchUsers(String keyword) {
        try {
            String url = AUTH_SERVICE_URL + "/api/users/search?keyword=" + keyword;

            ResponseEntity<BaseResponse<List<UserDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<BaseResponse<List<UserDTO>>>() {}
            );

            if (response.getBody() != null && response.getBody().getCode() == 0) {
                return response.getBody().getData();
            }
        } catch (Exception e) {
            log.error("搜索用户失败: keyword={}", keyword, e);
        }

        return new ArrayList<>();
    }

    /**
     * 清除用户缓存
     */
    public void clearUserCache(Long userId) {
        String cacheKey = CACHE_PREFIX + userId;
        redisTemplate.delete(cacheKey);
    }
}