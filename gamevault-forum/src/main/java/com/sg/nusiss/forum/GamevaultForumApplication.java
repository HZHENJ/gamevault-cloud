package com.sg.nusiss.forum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {
        "com.sg.nusiss.forum",
        "com.sg.nusiss.common"
})
@EnableDiscoveryClient
@EnableAsync
public class GamevaultForumApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamevaultForumApplication.class, args);
    }

}
