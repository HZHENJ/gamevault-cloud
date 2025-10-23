package com.sg.nusiss.forum.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * @ClassName RestTemplateConfig
 * @Author HUANG ZHENJIA
 * @Date 2025/10/23
 * @Description
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced  // ✅ 关键！让 RestTemplate 支持服务发现
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .interceptors(new TokenRelayInterceptor())
                .build();
    }
    /**
     * RestTemplate 拦截器 - 自动传递 JWT Token
     */
    public static class TokenRelayInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution
        ) throws IOException {

            // 从 Security Context 获取当前用户的认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
                Jwt jwt = jwtAuth.getToken();

                // 添加 Authorization Header
                request.getHeaders().setBearerAuth(jwt.getTokenValue());

                System.out.println("✅ RestTemplate 自动添加 Token: " + jwt.getTokenValue().substring(0, 20) + "...");
            } else {
                System.out.println("⚠️ 当前没有 JWT 认证信息，无法传递 Token");
            }

            return execution.execute(request, body);
        }
    }
}
