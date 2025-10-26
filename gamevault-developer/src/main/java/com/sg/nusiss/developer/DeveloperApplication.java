package com.sg.nusiss.developer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
		"com.sg.nusiss.developer",
		"com.sg.nusiss.common"
})
@MapperScan("com.sg.nusiss.developer.mapper")
@EnableScheduling
@EnableDiscoveryClient
public class DeveloperApplication {
	public static void main(String[] args) {
		SpringApplication.run(DeveloperApplication.class, args);
	}
}
