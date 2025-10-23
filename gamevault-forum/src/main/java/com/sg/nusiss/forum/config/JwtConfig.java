package com.sg.nusiss.forum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;

@Slf4j
@Configuration
public class JwtConfig {

    @Value("${rsa.public-key}")
    private Resource publicKeyResource;

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            log.info("加载公钥文件: {}", publicKeyResource.getFilename());

            try (InputStream pubStream = publicKeyResource.getInputStream()) {
                RSAPublicKey publicKey = (RSAPublicKey) RsaKeyConverters.x509().convert(pubStream);
                log.info("公钥加载成功");
                return NimbusJwtDecoder.withPublicKey(publicKey).build();
            }
        } catch (Exception e) {
            log.error("加载公钥失败", e);
            throw new RuntimeException("无法加载JWT公钥", e);
        }
    }
}