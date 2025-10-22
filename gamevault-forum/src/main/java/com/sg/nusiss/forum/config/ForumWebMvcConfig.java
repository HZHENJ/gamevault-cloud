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
                        // 以下接口不需要认证（可以匿名访问）
                        "/api/forum/posts",                    // 帖子列表
                        "/api/forum/posts/{id}",               // 帖子详情
                        "/api/forum/posts/search",             // 搜索帖子
                        "/api/forum/posts/{postId}/replies",   // 回复列表
                        "/api/forum/users/{userId}/posts",     // 用户帖子列表（公开）
                        "/api/test/**",                        // 测试接口
                        "/error"                               // 错误页面
                );
    }
}