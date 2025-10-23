package com.sg.nusiss.forum.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.sg.nusiss.forum.annotation.RequireForumAuth;
import com.sg.nusiss.forum.util.ForumJwtUtil;

/**
 * 论坛认证拦截器
 * 拦截需要认证的请求，验证JWT token
 */
@Component
public class ForumAuthInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ForumAuthInterceptor.class);

    @Autowired
    private ForumJwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        logger.info("========== 认证拦截器开始 ==========");
        logger.info("请求路径: {}", request.getRequestURI());
        logger.info("请求方法: {}", request.getMethod());

        // 获取 Token
        String authHeader = request.getHeader("Authorization");
        logger.info("Authorization header: {}", authHeader);

        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.info("提取的 token (前50字符): {}", token.substring(0, Math.min(50, token.length())));
        } else {
            logger.warn("Authorization header 格式不正确或不存在");
        }

        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
            logger.info("从 URL 参数获取 token: {}", token != null ? "存在" : "不存在");
        }

        // 验证 Token
        if (token != null) {
            try {
                ForumJwtUtil.TokenInfo tokenInfo = jwtUtil.validateAndParseToken(token);
                logger.info("Token 验证结果 - valid: {}, userId: {}, username: {}",
                        tokenInfo.valid, tokenInfo.userId, tokenInfo.username);

                if (tokenInfo.valid) {
                    request.setAttribute("userId", tokenInfo.userId);
                    request.setAttribute("username", tokenInfo.username);
                    logger.info("✅ 认证成功 - 用户ID: {}, 用户名: {}", tokenInfo.userId, tokenInfo.username);
                    return true;
                } else {
                    logger.warn("❌ Token 无效");
                }
            } catch (Exception e) {
                logger.error("❌ Token 验证异常: {}", e.getMessage(), e);
            }
        }

        // 检查是否需要认证
        RequireForumAuth requireAuth = ((HandlerMethod) handler).getMethodAnnotation(RequireForumAuth.class);
        if (requireAuth != null && requireAuth.required()) {
            logger.warn("❌ 需要认证但 Token 无效 - 返回 401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"未授权\",\"message\":\"请先登录\"}");
            return false;
        }

        logger.info("========== 认证拦截器结束 ==========");
        return true;
    }
}