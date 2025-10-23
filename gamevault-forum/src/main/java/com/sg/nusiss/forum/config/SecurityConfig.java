package com.sg.nusiss.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置
 *
 * 论坛微服务使用自定义 JWT 拦截器（ForumAuthInterceptor）进行认证
 * 所以 Spring Security 配置为允许所有请求通过，由拦截器控制具体权限
 *
 * 位置: gamevault-forum/src/main/java/sg/edu/nus/gamevaultforum/config/SecurityConfig.java
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 密码编码器（用于密码加密，如果有本地认证需求）
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置 Spring Security 过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（前后端分离，使用 JWT 认证）
                .csrf(csrf -> csrf.disable())

                // 禁用 CORS（由 CorsConfig 单独处理）
                .cors(cors -> cors.disable())

                // 无状态会话（不使用 Session）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 允许所有请求通过 Spring Security
                // 实际的认证由 ForumAuthInterceptor 处理
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}