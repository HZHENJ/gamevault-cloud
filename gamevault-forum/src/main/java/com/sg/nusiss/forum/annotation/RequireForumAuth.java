package com.sg.nusiss.forum.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 论坛认证注解
 * 用于标记需要认证的 Controller 方法或类
 *
 * 位置: gamevault-forum/src/main/java/sg/edu/nus/gamevaultforum/annotation/RequireForumAuth.java
 *
 * 使用示例:
 * <pre>
 * &#64;RequireForumAuth
 * &#64;PostMapping("/posts")
 * public BaseResponse<ForumContent> createPost(...) {
 *     // 需要认证才能创建帖子
 * }
 *
 * &#64;RequireForumAuth(required = false)
 * &#64;GetMapping("/posts/{id}")
 * public BaseResponse<ForumContent> getPost(...) {
 *     // 可选认证（认证后可以获取更多信息）
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireForumAuth {

    /**
     * 是否必须认证
     *
     * @return true  = 必须认证，未认证返回 401
     *         false = 可选认证，未认证也能访问但功能受限
     */
    boolean required() default true;
}