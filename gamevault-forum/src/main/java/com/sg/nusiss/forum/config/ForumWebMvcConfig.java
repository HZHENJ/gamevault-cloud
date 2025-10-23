package com.sg.nusiss.forum.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 论坛 Web MVC 配置
 * 注册拦截器并配置拦截规则
 *
 * 位置: gamevault-forum/src/main/java/sg/edu/nus/gamevaultforum/config/ForumWebMvcConfig.java
 */
@Configuration
public class ForumWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ForumAuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/forum/**")  // 拦截所有论坛 API
                .excludePathPatterns(
                        "/api/forum/auth/**",               // 认证相关接口
                        "/api/test/**",                        // 测试接口
                        "/error"                               // 错误页面
                );
    }
}