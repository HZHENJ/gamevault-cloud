package com.sg.nusiss.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

   @Bean
   SecurityFilterChain security(HttpSecurity http) throws Exception {
       http.csrf(

               csrf -> csrf.disable())
               .cors(Customizer.withDefaults())
               .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .authorizeHttpRequests(a -> a
                       .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                       .requestMatchers("/.well-known/jwks.json").permitAll()
                       .requestMatchers("/uploads/**").permitAll()
                       .requestMatchers("/api/auth/**").permitAll()
                       .requestMatchers("/api/users/**").authenticated()
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

//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedOriginPatterns("*")
//                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
//                        .allowedHeaders("*")
//                        .allowCredentials(true)
//                        .maxAge(3600);
//            }
//        };
//    }

   @Bean
   CorsConfigurationSource corsConfigurationSource() {
       CorsConfiguration cfg = new CorsConfiguration();
       // 与前端同源；如果之后有域名，就换成域名
       cfg.setAllowedOrigins(List.of("http://52.77.169.8:30131"));
       cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
       cfg.setAllowedHeaders(List.of("*"));
       cfg.setAllowCredentials(true);
       cfg.setMaxAge(3600L);

       UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
       source.registerCorsConfiguration("/**", cfg);
       return source;
   }
}