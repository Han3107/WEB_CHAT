package com.simplechat.security;

import org.springframework.stereotype.Component;
import com.simplechat.dto.LoginResponse;

/**
 * RequestContext - Giữ thông tin về user hiện tại trong request
 */
@Component
public class RequestContext {
    
    private static final ThreadLocal<LoginResponse> userContext = new ThreadLocal<>();
    
    /**
     * Lưu user hiện tại vào context
     */
    public static void setCurrentUser(LoginResponse user) {
        userContext.set(user);
    }
    
    /**
     * Lấy user hiện tại từ context
     */
    public static LoginResponse getCurrentUser() {
        return userContext.get();
    }
    
    /**
     * Xóa user context khi request kết thúc
     */
    public static void clearContext() {
        userContext.remove();
    }
    
    /**
     * Kiểm tra user có phải admin không
     */
    public static boolean isAdmin() {
        LoginResponse user = userContext.get();
        return user != null && "admin".equals(user.getRole());
    }
    
    /**
     * Kiểm tra user có đăng nhập không
     */
    public static boolean isAuthenticated() {
        return userContext.get() != null;
    }
}
