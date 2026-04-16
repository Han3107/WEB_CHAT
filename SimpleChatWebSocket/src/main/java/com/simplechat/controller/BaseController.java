package com.simplechat.controller;

import com.simplechat.dto.LoginResponse;
import com.simplechat.security.Permission;
import com.simplechat.security.RequestContext;
import com.simplechat.security.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

/**
 * BaseController - Base class cho các controller với authorization
 */
public class BaseController {
    
    /**
     * Kiểm tra user có đăng nhập không
     */
    protected boolean isAuthenticated() {
        return RequestContext.isAuthenticated();
    }
    
    /**
     * Kiểm tra user có phải admin không
     */
    protected boolean isAdmin() {
        return RequestContext.isAdmin();
    }
    
    /**
     * Lấy user hiện tại
     */
    protected LoginResponse getCurrentUser() {
        return RequestContext.getCurrentUser();
    }
    
    /**
     * Kiểm tra user có permission cụ thể không
     */
    protected boolean hasPermission(String permission) {
        LoginResponse user = getCurrentUser();
        if (user == null) return false;
        return Permission.hasPermission(user.getRole(), permission);
    }
    
    /**
     * Require authentication - trả về 401 nếu chưa đăng nhập
     */
    protected ResponseEntity<?> requireAuth() {
        if (!isAuthenticated()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Vui lòng đăng nhập trước");
            error.put("code", "UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        return null;
    }
    
    /**
     * Require admin role - trả về 403 nếu không phải admin
     */
    protected ResponseEntity<?> requireAdmin() {
        ResponseEntity<?> authCheck = requireAuth();
        if (authCheck != null) return authCheck;
        
        if (!isAdmin()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Chỉ quản trị viên mới có quyền truy cập");
            error.put("code", "FORBIDDEN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        return null;
    }
    
    /**
     * Require specific permission - trả về 403 nếu không có quyền
     */
    protected ResponseEntity<?> requirePermission(String permission) {
        ResponseEntity<?> authCheck = requireAuth();
        if (authCheck != null) return authCheck;
        
        if (!hasPermission(permission)) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Bạn không có quyền hạn để thực hiện hành động này");
            error.put("code", "FORBIDDEN");
            error.put("permission", permission);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        return null;
    }
    
    /**
     * Tạo response lỗi tùy chỉnh
     */
    protected ResponseEntity<?> errorResponse(HttpStatus status, String message, String code) {
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        error.put("code", code);
        return ResponseEntity.status(status).body(error);
    }
    
    /**
     * Tạo response thành công tùy chỉnh
     */
    protected <T> ResponseEntity<T> successResponse(T data) {
        return ResponseEntity.ok(data);
    }
}
