package com.simplechat.security;

import com.simplechat.dto.LoginResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Base64;

/**
 * AuthorizationInterceptor - Kiểm tra token và set RequestContext cho mỗi request
 */
@Component
public class AuthorizationInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // Lấy Authorization header
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7); // Bỏ "Bearer "
                
                // Decode token (format: base64(username:role:timestamp))
                String decodedToken = new String(Base64.getDecoder().decode(token));
                String[] parts = decodedToken.split(":");
                
                if (parts.length >= 2) {
                    String username = parts[0];
                    String role = parts[1];
                    System.out.println("[DEBUG] Token decoded: user=" + username + ", role=" + role);
                    
                    // Set user vào context
                    LoginResponse user = new LoginResponse();
                    user.setUsername(username);
                    user.setRole(role);
                    user.setToken(token);
                    RequestContext.setCurrentUser(user);
                }
            }
            
            return true;
        } catch (Exception e) {
            // Nếu decode token thất bại, tiếp tục mà không set user
            return true;
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        // Clear context sau khi request xử lý xong
        RequestContext.clearContext();
    }
}
