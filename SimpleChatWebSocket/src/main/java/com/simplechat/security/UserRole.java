package com.simplechat.security;

/**
 * UserRole - Các vai trò người dùng
 */
public enum UserRole {
    ADMIN("admin", "Quản trị viên"),
    MODERATOR("moderator", "Người kiểm duyệt"),
    USER("user", "Người dùng thường");
    
    private final String code;
    private final String displayName;
    
    UserRole(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static UserRole fromCode(String code) {
        for (UserRole role : UserRole.values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return USER; // Default role
    }
}
