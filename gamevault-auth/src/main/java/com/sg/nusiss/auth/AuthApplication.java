package com.sg.nusiss.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @ClassName AuthApplication
 * @Author HUANG ZHENJIA
 * @Date 2025/10/22
 * @Description
 */

@SpringBootApplication(scanBasePackages = {
        "com.sg.nusiss.auth",
})
@EnableDiscoveryClient
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
