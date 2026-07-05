package com.ecommerce.common.interceptor;

import com.ecommerce.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public LoginInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":401,\"message\":\"请先登录\",\"timestamp\":" + System.currentTimeMillis() + "}");
            } catch (Exception ignored) {
            }
            return false;
        }
        
        // 检查是否是admin token（包含adminId claim）
        Long adminId = jwtUtil.getAdminId(token);
        if (adminId != null) {
            request.setAttribute("adminId", adminId);
            request.setAttribute("adminUsername", jwtUtil.getUsername(token));
        } else {
            // 普通用户token
            Long userId = jwtUtil.getUserId(token);
            request.setAttribute("userId", userId);
            request.setAttribute("username", jwtUtil.getUsername(token));
        }
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}