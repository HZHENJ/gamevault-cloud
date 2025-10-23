package com.sg.nusiss.forum.service.user;

import com.sg.nusiss.common.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * UserService - 适配直接返回 UserDTO 的版本
 *
 * 适用场景: Auth 服务直接返回 UserDTO,而不是 BaseResponse<UserDTO>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://gamevault-auth}")
    private String authServiceUrl;

    /**
     * 根据用户ID查询用户信息
     *
     * 🔥 修改点: 直接接收 UserDTO,而不是 BaseResponse<UserDTO>
     */
    public UserDTO getUserById(Long userId) {
        if (userId == null) {
            log.warn("getUserById - userId 为 null");
            return null;
        }

        log.info("🔍 开始查询用户信息 - userId: {}", userId);

        try {
            String url = authServiceUrl + "/api/users/" + userId;
            log.info("🌐 调用 Auth 服务 - URL: {}", url);

            // 🔥 直接接收 UserDTO,不是 BaseResponse
            ResponseEntity<UserDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<UserDTO>() {}
            );

            if (response.getBody() != null) {
                UserDTO user = response.getBody();
                log.info("✅ 成功获取用户信息 - userId: {}, username: {}",
                        userId, user.getUsername());
                return user;
            } else {
                log.warn("⚠️ Auth 服务响应体为空 - userId: {}", userId);
            }

        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("❌ 网络连接失败 - 无法访问 Auth 服务 - URL: {}, 错误: {}",
                    authServiceUrl, e.getMessage());
            log.error("💡 请检查: 1) Auth 服务是否启动 2) Nacos 是否正常 3) 网络是否互通");

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            log.warn("⚠️ 用户不存在 - userId: {}", userId);

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("❌ HTTP客户端错误 - userId: {}, 状态码: {}, 响应: {}",
                    userId, e.getStatusCode(), e.getResponseBodyAsString());
            log.error("💡 Auth 服务可能返回了错误响应");

        } catch (Exception e) {
            log.error("❌ 调用 Auth 服务失败 - userId: {}, 错误类型: {}, 错误信息: {}",
                    userId, e.getClass().getSimpleName(), e.getMessage(), e);
        }

        // 返回 null,让调用方处理
        log.warn("⚠️ 获取用户信息失败,返回 null - userId: {}", userId);
        return null;
    }

    /**
     * 批量查询用户信息
     *
     * 🔥 修改点: 直接接收 List<UserDTO>
     */
    public List<UserDTO> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("🔍 批量查询用户信息 - userIds: {}", userIds);

        try {
            String url = authServiceUrl + "/api/users/batch";

            ResponseEntity<List<UserDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(userIds),
                    new ParameterizedTypeReference<List<UserDTO>>() {}
            );

            if (response.getBody() != null) {
                List<UserDTO> users = response.getBody();
                log.info("✅ 成功批量获取 {} 个用户信息", users.size());
                return users;
            }

        } catch (Exception e) {
            log.error("❌ 批量查询用户失败: userIds={}", userIds, e);
        }

        return new ArrayList<>();
    }

    /**
     * 搜索用户
     *
     * 🔥 修改点: 直接接收 List<UserDTO>
     */
    public List<UserDTO> searchUsers(String keyword) {
        log.info("🔍 搜索用户 - keyword: {}", keyword);

        try {
            String url = authServiceUrl + "/api/users/search?keyword=" + keyword;

            ResponseEntity<List<UserDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<UserDTO>>() {}
            );

            if (response.getBody() != null) {
                List<UserDTO> users = response.getBody();
                log.info("✅ 搜索到 {} 个用户", users.size());
                return users;
            }

        } catch (Exception e) {
            log.error("❌ 搜索用户失败: keyword={}", keyword, e);
        }

        return new ArrayList<>();
    }
}