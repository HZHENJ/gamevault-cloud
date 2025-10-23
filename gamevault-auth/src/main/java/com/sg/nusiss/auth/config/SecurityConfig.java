package com.sg.nusiss.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // ✅ 禁用CORS - 让Gateway统一处理
                .cors(cors -> cors.disable())

                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/check-email",
                                "/api/auth/check-username",
                                "/api/auth/logout",
                                "/.well-known/jwks.json"
                        ).permitAll()
                        .requestMatchers("/api/forum/posts", "/api/forum/posts/**").permitAll()
                        .requestMatchers("/api/games/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/ws/**", "/ws").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
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

    // ❌ 删除这个方法 - 不需要在Auth服务配置CORS
    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    //     ...
    // }
}