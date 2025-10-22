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
        "com.sg.nusiss.auth",     // 扫描当前服务
        "com.sg.nusiss.common"    // 扫描 common 模块
})
@EnableDiscoveryClient  // 启用 Nacos 服务注册
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
