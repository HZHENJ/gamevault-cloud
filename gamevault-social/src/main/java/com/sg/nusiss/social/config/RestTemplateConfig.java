package com.sg.nusiss.social.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @ClassName RestTemplateConfig
 * @Author HUANG ZHENJIA
 * @Date 2025/10/23
 * @Description
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced  // 启用 Nacos 服务发现
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
