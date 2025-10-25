package com.sg.nusiss.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("➡️ [Auth] Incoming Request: " +
                request.getMethod() + " " + request.getRequestURI());

        filterChain.doFilter(request, response);

        System.out.println("⬅️ [Auth] Response Status: " + response.getStatus());
    }
}
