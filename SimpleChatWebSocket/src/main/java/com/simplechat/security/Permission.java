package com.simplechat.security;

/**
 * Permission - Định nghĩa quyền hạn cho các chức năng
 */
public class Permission {
    
    // Admin permissions
    public static final String ADMIN_VIEW_USERS = "admin:view:users";
    public static final String ADMIN_MANAGE_USERS = "admin:manage:users";
    public static final String ADMIN_MANAGE_CHANNELS = "admin:manage:channels";
    public static final String ADMIN_VIEW_ANALYTICS = "admin:view:analytics";
    public static final String ADMIN_MANAGE_SYSTEM = "admin:manage:system";
    public static final String ADMIN_VIEW_LOGS = "admin:view:logs";
    
    // User permissions
    public static final String USER_CREATE_CHANNEL = "user:create:channel";
    public static final String USER_SEND_MESSAGE = "user:send:message";
    public static final String USER_VIEW_MESSAGES = "user:view:messages";
    public static final String USER_MANAGE_PROFILE = "user:manage:profile";
    public static final String USER_JOIN_CHANNEL = "user:join:channel";
    
    /**
     * Kiểm tra user có permission không
     */
    public static boolean hasPermission(String role, String permission) {
        UserRole userRole = UserRole.fromCode(role);
        
        switch (userRole) {
            case ADMIN:
                // Admin có tất cả permissions
                return true;
                
            case MODERATOR:
                // Moderator có quyền quản lý kênh và messages
                return permission.startsWith("user:") || 
                       permission.equals(ADMIN_MANAGE_CHANNELS) ||
                       permission.equals(ADMIN_VIEW_LOGS);
                
            case USER:
            default:
                // User bình thường chỉ có user permissions
                return permission.startsWith("user:");
        }
    }
    
    /**
     * Lấy danh sách permissions của một role
     */
    public static String[] getPermissionsForRole(String role) {
        UserRole userRole = UserRole.fromCode(role);
        
        switch (userRole) {
            case ADMIN:
                return new String[]{
                    ADMIN_VIEW_USERS,
                    ADMIN_MANAGE_USERS,
                    ADMIN_MANAGE_CHANNELS,
                    ADMIN_VIEW_ANALYTICS,
                    ADMIN_MANAGE_SYSTEM,
                    ADMIN_VIEW_LOGS,
                    USER_CREATE_CHANNEL,
                    USER_SEND_MESSAGE,
                    USER_VIEW_MESSAGES,
                    USER_MANAGE_PROFILE,
                    USER_JOIN_CHANNEL
                };
                
            case MODERATOR:
                return new String[]{
                    ADMIN_MANAGE_CHANNELS,
                    ADMIN_VIEW_LOGS,
                    USER_CREATE_CHANNEL,
                    USER_SEND_MESSAGE,
                    USER_VIEW_MESSAGES,
                    USER_MANAGE_PROFILE,
                    USER_JOIN_CHANNEL
                };
                
            case USER:
            default:
                return new String[]{
                    USER_CREATE_CHANNEL,
                    USER_SEND_MESSAGE,
                    USER_VIEW_MESSAGES,
                    USER_MANAGE_PROFILE,
                    USER_JOIN_CHANNEL
                };
        }
    }
}
