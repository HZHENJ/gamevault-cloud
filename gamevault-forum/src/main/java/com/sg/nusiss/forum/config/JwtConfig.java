package com.sg.nusiss.forum.config;

import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;

/**
 * 论坛微服务 JWT 配置
 * 只负责验证从 auth 微服务生成的 JWT Token
 *
 * 位置: gamevault-forum/src/main/java/sg/edu/nus/gamevaultforum/config/JwtConfig.java
 */
@Configuration
public class JwtConfig {

    @Value("${rsa.public-key}")
    private Resource publicKeyResource;

    /**
     * 配置 JwtDecoder - 用于验证 JWT Token
     * 使用 RSA 公钥验证从 auth 微服务生成的 JWT
     */
    @Bean
    public JwtDecoder jwtDecoder() throws IOException {
        try (InputStream pubStream = publicKeyResource.getInputStream()) {
            RSAPublicKey publicKey = (RSAPublicKey) RsaKeyConverters.x509().convert(pubStream);
            return NimbusJwtDecoder.withPublicKey(publicKey).build();
        }
    }
}