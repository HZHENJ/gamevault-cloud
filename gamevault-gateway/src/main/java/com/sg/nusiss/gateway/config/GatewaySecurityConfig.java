package com.sg.nusiss.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .cors(Customizer.withDefaults())                // 让 globalcors 生效
                .csrf(ServerHttpSecurity.CsrfSpec::disable)    // 无状态 API 关 CSRF
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()          // 预检放行
                        .pathMatchers("/api/auth/**", "/.well-known/jwks.json", "/uploads/**").permitAll()
                        .anyExchange().permitAll()   // 先放行全部，确认链路通；以后再收紧到 authenticated()
                )
                // 如果 Gateway 不做 JWT 校验，就先别启用资源服务器
                .build();
    }
}
