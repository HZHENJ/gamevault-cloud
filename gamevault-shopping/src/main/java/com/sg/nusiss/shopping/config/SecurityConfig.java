package com.sg.nusiss.shopping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;


import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // .authorizeHttpRequests(auth -> auth
                //         // 公开接口 - 游戏列表查询
                //         .requestMatchers("/api/games/**").permitAll()
                //         // 公开接口 - 健康检查
                //         .requestMatchers("/actuator/**").permitAll()
                //         // 公开接口 - 文件上传（游戏图片）
                //         .requestMatchers("/uploads/**").permitAll()
                //         // 需要认证 - 购物车相关
                //         .requestMatchers("/api/cart/**").authenticated()
                //         // 需要认证 - 用户激活码查询
                //         .requestMatchers("/api/user/**").authenticated()
                //         // 需要认证 - 管理员功能
                //         .requestMatchers("/api/admin/**").authenticated()
                //         // 其他请求都需要认证
                //         .anyRequest().authenticated()
                // )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> Collections.emptyList());
        converter.setPrincipalClaimName("sub");
        return converter;
    }
}