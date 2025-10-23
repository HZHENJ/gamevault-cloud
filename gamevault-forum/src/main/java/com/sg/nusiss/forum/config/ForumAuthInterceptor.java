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

        // 如果不是方法处理器，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 检查是否需要强制认证
        RequireForumAuth requireAuth = handlerMethod.getMethodAnnotation(RequireForumAuth.class);
        if (requireAuth == null) {
            requireAuth = handlerMethod.getBeanType().getAnnotation(RequireForumAuth.class);
        }
        boolean authRequired = requireAuth != null && requireAuth.required();

        // 获取 Token
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (authHeader != null) {
            token = authHeader;
        }
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }

        // ✅ 关键修改：只要有Token就尝试解析
        boolean tokenValid = false;
        if (token != null && !token.isEmpty()) {
            try {
                ForumJwtUtil.TokenInfo tokenInfo = jwtUtil.validateAndParseToken(token);
                if (tokenInfo.valid) {
                    // ✅ 设置用户信息到 request 属性
                    request.setAttribute("userId", tokenInfo.userId);
                    request.setAttribute("username", tokenInfo.username);
                    tokenValid = true;
                    logger.debug("论坛认证成功 - 用户ID: {}, 用户名: {}, 路径: {}",
                            tokenInfo.userId, tokenInfo.username, request.getRequestURI());
                } else {
                    logger.warn("Token验证失败 - 路径: {}", request.getRequestURI());
                }
            } catch (Exception e) {
                logger.error("Token解析异常 - 路径: {}, 错误: {}",
                        request.getRequestURI(), e.getMessage());
            }
        }

        // ✅ 如果方法标记为需要认证，但Token无效，则拒绝访问
        if (authRequired && !tokenValid) {
            logger.warn("需要认证但Token无效 - 路径: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":40100,\"message\":\"请先登录\",\"data\":null}");
            return false;
        }

        // ✅ 不需要强制认证，或者Token有效，放行
        return true;
    }
}