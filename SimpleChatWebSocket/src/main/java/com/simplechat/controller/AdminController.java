package com.simplechat.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.simplechat.entity.ActivityLog;
import com.simplechat.entity.Channel;
import com.simplechat.entity.ChannelMember;
import com.simplechat.entity.User;
import com.simplechat.repository.ActivityLogRepository;
import com.simplechat.repository.AppStatisticsRepository;
import com.simplechat.repository.BannedUserRepository;
import com.simplechat.repository.ChannelMemberRepository;
import com.simplechat.repository.ChannelRepository;
import com.simplechat.repository.MessageRepository;
import com.simplechat.repository.TaskRepository;
import com.simplechat.repository.UserRepository;

/**
 * AdminController - Quản lý hệ thống, users, logs (chỉ dành cho admin)
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController extends BaseController {
    
    @Autowired
    private ActivityLogRepository activityLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private com.simplechat.repository.ChannelMemberRepository channelMemberRepository;
    
    @Autowired
    private AppStatisticsRepository appStatisticsRepository;
    
    @Autowired
    private BannedUserRepository bannedUserRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired(required = false)
    private TaskRepository taskRepository;
    
    /**
     * Lấy danh sách tất cả users
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        // TODO: Thêm permission check khi auth hoàn tất
        // ResponseEntity<?> permCheck = requirePermission(Permission.ADMIN_VIEW_USERS);
        // if (permCheck != null) return permCheck;
        
        try {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> userList = users.stream().map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", user.getUserId());
                userMap.put("username", user.getUsername());
                userMap.put("email", user.getEmail());
                userMap.put("fullName", user.getFullName());
                userMap.put("role", user.getRole());
                userMap.put("status", user.getStatus());
                userMap.put("createdAt", user.getCreatedAt());
                userMap.put("lastLogin", user.getLastLogin());
                return userMap;
            }).collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", userList);
            response.put("count", userList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Lỗi khi lấy danh sách users: " + e.getMessage());
        }
    }
    
    /**
     * Xem chi tiết user
     * GET /api/admin/users/{username}
     */
    @GetMapping("/users/{username}")
    public ResponseEntity<?> getUserDetail(@PathVariable String username) {
        // TODO: Thêm permission check khi auth hoàn tất
        // ResponseEntity<?> permCheck = requirePermission(Permission.ADMIN_VIEW_USERS);
        // if (permCheck != null) return permCheck;
        
        try {
            var user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                return buildErrorResponse("User không tồn tại");
            }
            
            User u = user.get();
            Map<String, Object> response = new HashMap<>();
            response.put("userId", u.getUserId());
            response.put("username", u.getUsername());
            response.put("email", u.getEmail());
            response.put("fullName", u.getFullName());
            response.put("role", u.getRole());
            response.put("status", u.getStatus());
            response.put("avatarUrl", u.getAvatarUrl());
            response.put("createdAt", u.getCreatedAt());
            response.put("updatedAt", u.getUpdatedAt());
            response.put("lastLogin", u.getLastLogin());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Lỗi: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật thông tin user
     * PUT /api/admin/users/{username}
     */
    @PutMapping("/users/{username}")
    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody Map<String, String> payload) {
        try {
            var user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                return buildErrorResponse("User không tồn tại");
            }
            
            User u = user.get();
            if (payload.containsKey("email")) u.setEmail(payload.get("email"));
            if (payload.containsKey("fullName")) u.setFullName(payload.get("fullName"));
            if (payload.containsKey("role")) u.setRole(payload.get("role"));
            if (payload.containsKey("status")) u.setStatus(payload.get("status"));
            
            userRepository.save(u);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Thông tin user " + username + " đã được cập nhật");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Lỗi khi cập nhật user: " + e.getMessage());
        }
    }

    /**
     * Ban user
     * POST /api/admin/users/{username}/ban
     */
    @PostMapping("/users/{username}/ban")
    public ResponseEntity<?> banUser(@PathVariable String username, @RequestBody Map<String, String> payload) {
        // TODO: Thêm permission check khi auth hoàn tất
        // ResponseEntity<?> permCheck = requirePermission(Permission.ADMIN_MANAGE_USERS);
        // if (permCheck != null) return permCheck;
        
        try {
            var user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                return buildErrorResponse("User không tồn tại");
            }
            
            User u = user.get();
            u.setStatus("banned");
            userRepository.save(u);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User " + username + " đã bị khóa");
            response.put("reason", payload.get("reason"));
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Lỗi khi khóa user: " + e.getMessage());
        }
    }
    
    /**
     * Unban user
     * POST /api/admin/users/{username}/unban
     */
    @PostMapping("/users/{username}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable String username) {
        // TODO: Thêm permission check khi auth hoàn tất
        // ResponseEntity<?> permCheck = requirePermission(Permission.ADMIN_MANAGE_USERS);
        // if (permCheck != null) return permCheck;
        
        try {
            var user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                return buildErrorResponse("User không tồn tại");
            }
            
            User u = user.get();
            u.setStatus("active");
            userRepository.save(u);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User " + username + " đã được kích hoạt");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Lỗi khi kích hoạt user: " + e.getMessage());
        }
    }
    
    /**
     * Vô hiệu hóa tài khoản (inactive, không phải ban)
     * POST /api/admin/users/{username}/disable
     */
    @PostMapping("/users/{username}/disable")
    public ResponseEntity<?> disableUser(@PathVariable String username) {
        try {
            User u = userRepository.findByUsername(username).orElse(null);
            if (u == null) return buildErrorResponse("User không tồn tại");
            u.setStatus("inactive");
            userRepository.save(u);
            return ResponseEntity.ok(Map.of("message", "Đã vô hiệu hóa tài khoản " + username));
        } catch (Exception e) { return buildErrorResponse(e.getMessage()); }
    }

    /**
     * Khôi phục tài khoản (inactive/banned → active)
     * POST /api/admin/users/{username}/restore
     */
    @PostMapping("/users/{username}/restore")
    public ResponseEntity<?> restoreUser(@PathVariable String username) {
        try {
            User u = userRepository.findByUsername(username).orElse(null);
            if (u == null) return buildErrorResponse("User không tồn tại");
            u.setStatus("active");
            userRepository.save(u);
            return ResponseEntity.ok(Map.of("message", "Đã khôi phục tài khoản " + username));
        } catch (Exception e) { return buildErrorResponse(e.getMessage()); }
    }

    /**
     * Tìm kiếm user
     * GET /api/admin/users/search?q=...
     */
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q) {
        try {
            List<User> results = userRepository.findAll().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(q.toLowerCase())
                    || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q.toLowerCase()))
                    || (u.getFullName() != null && u.getFullName().toLowerCase().contains(q.toLowerCase())))
                .collect(Collectors.toList());
            List<Map<String, Object>> list = results.stream().map(u -> {
                Map<String, Object> m = new HashMap<>();
                m.put("username", u.getUsername()); m.put("email", u.getEmail());
                m.put("fullName", u.getFullName()); m.put("role", u.getRole());
                m.put("status", u.getStatus()); m.put("createdAt", u.getCreatedAt());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("data", list, "count", list.size()));
        } catch (Exception e) { return buildErrorResponse(e.getMessage()); }
    }

    /**
     * Tìm kiếm channel
     * GET /api/admin/channels/search?q=...
     */
    @GetMapping("/channels/search")
    public ResponseEntity<?> searchChannels(@RequestParam String q) {
        try {
            List<Channel> results = channelRepository.findAll().stream()
                .filter(c -> c.getChannelName().toLowerCase().contains(q.toLowerCase())
                    || (c.getDescription() != null && c.getDescription().toLowerCase().contains(q.toLowerCase())))
                .collect(Collectors.toList());
            List<Map<String, Object>> list = results.stream().map(c -> {
                Map<String, Object> m = new HashMap<>();
                m.put("channelId", c.getChannelId()); m.put("channelName", c.getChannelName());
                m.put("description", c.getDescription()); m.put("channelType", c.getChannelType());
                m.put("inviteCode", c.getInviteCode()); m.put("autoApprove", c.getAutoApprove());
                m.put("isActive", c.getIsActive());
                m.put("memberCount", channelMemberRepository.countMembersByChannel(c.getChannelId()));
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("channels", list, "count", list.size()));
        } catch (Exception e) { return buildErrorResponse(e.getMessage()); }
    }

    /**
     * Bật/tắt auto-approve cho channel (admin)
     * POST /api/admin/channels/{channelId}/auto-approve
     */
    @PostMapping("/channels/{channelId}/auto-approve")
    public ResponseEntity<?> toggleAutoApprove(@PathVariable Integer channelId, @RequestBody Map<String, Boolean> body) {
        try {
            Channel ch = channelRepository.findById(channelId).orElse(null);
            if (ch == null) return buildErrorResponse("Channel không tồn tại");
            boolean val = Boolean.TRUE.equals(body.get("autoApprove"));
            ch.setAutoApprove(val);
            channelRepository.save(ch);
            return ResponseEntity.ok(Map.of("message", val ? "Đã bật tự duyệt" : "Đã tắt tự duyệt", "autoApprove", val));
        } catch (Exception e) { return buildErrorResponse(e.getMessage()); }
    }

    /**
     * Xem activity logs
     * GET /api/admin/logs?page=1&pageSize=50
     */
    @GetMapping("/logs")
    public ResponseEntity<?> getActivityLogs(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "50") int pageSize) {
        
        try {
            // Lấy tất cả logs (sắp xếp theo thời gian mới nhất)
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
            List<ActivityLog> allLogs = activityLogRepository.findAll(sort);
            
            // Phân trang
            int startIdx = (page - 1) * pageSize;
            int endIdx = Math.min(startIdx + pageSize, allLogs.size());
            List<ActivityLog> paginatedLogs = startIdx < allLogs.size() 
                ? allLogs.subList(startIdx, endIdx) 
                : new ArrayList<>();
            
            List<Map<String, Object>> logsData = paginatedLogs.stream().map(log -> {
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("logId", log.getLogId());
                logMap.put("userId", log.getUser() != null ? log.getUser().getUserId() : null);
                logMap.put("username", log.getUser() != null ? log.getUser().getUsername() : "Unknown");
                logMap.put("action", log.getAction());
                logMap.put("entityType", log.getEntityType());
                logMap.put("entityId", log.getEntityId());
                logMap.put("description", log.getDescription());
                logMap.put("ipAddress", log.getIpAddress());
                logMap.put("createdAt", log.getCreatedAt());
                return logMap;
            }).collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("page", page);
            response.put("pageSize", pageSize);
            response.put("total", allLogs.size());
            response.put("data", logsData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Lỗi khi lấy logs: " + e.getMessage());
        }
    }
    
    /**
     * Xem system analytics - Real data từ database
     * GET /api/admin/analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics() {
        try {
            // Calculate real analytics from database tables
            long totalUsers = userRepository.count();
            long totalChannels = channelRepository.count();
            long totalMessages = messageRepository.count();
            
            System.out.println("[DEBUG] userRepository.count() = " + totalUsers);
            System.out.println("[DEBUG] channelRepository.count() = " + totalChannels);
            System.out.println("[DEBUG] messageRepository.count() = " + totalMessages);
            
            // Count active users (not banned)
            List<User> activeUsers = userRepository.findAll().stream()
                    .filter(u -> "active".equals(u.getStatus()))
                    .collect(Collectors.toList());
            
            // Count banned users
            long bannedUsers = userRepository.findAll().stream()
                    .filter(u -> "banned".equals(u.getStatus()))
                    .count();
            
            // Count votes and quizzes from tasks table
            long totalTasks = 0;
            long totalVotes = 0;
            long totalQuiz = 0;
            try {
                if (taskRepository != null) {
                    totalTasks = taskRepository.count();
                    totalVotes = taskRepository.findAll().stream()
                            .filter(t -> "vote".equals(t.getTaskType()))
                            .count();
                    totalQuiz = taskRepository.findAll().stream()
                            .filter(t -> "quiz".equals(t.getTaskType()))
                            .count();
                } else {
                    System.out.println("⚠️ TaskRepository not available");
                }
            } catch (Exception e) {
                System.out.println("Task counting failed: " + e.getMessage());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", totalUsers);
            response.put("activeUsers", activeUsers.size());
            response.put("totalChannels", totalChannels);
            response.put("totalMessages", totalMessages);
            response.put("totalVotes", totalVotes);
            response.put("totalQuiz", totalQuiz);
            response.put("activeSessions", activeUsers.size());
            response.put("bannedUsers", bannedUsers);
            // Admin count
            long adminCount = userRepository.findAll().stream().filter(u -> "admin".equals(u.getRole())).count();
            response.put("adminUsers", adminCount);
            // Pending channel members
            long pendingMembers = channelMemberRepository.findAll().stream()
                .filter(cm -> "pending".equals(cm.getStatus())).count();
            response.put("pendingMembers", pendingMembers);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Lỗi khi lấy analytics: " + e.getMessage());
        }
    }
    
    /**
     * Tạo channel mới
     * POST /api/admin/channels
     */
    @PostMapping("/channels")
    public ResponseEntity<?> createChannel(@RequestBody Map<String, String> payload) {
        try {
            String channelName = payload.get("channelName");
            String description = payload.get("description");
            String channelType = payload.getOrDefault("channelType", "public");
            
            if (channelName == null || channelName.trim().isEmpty()) {
                return buildErrorResponse("Tên channel không được để trống");
            }
            
            User adminUser = userRepository.findByUsername("admin").orElse(null);
            if (adminUser == null) {
                adminUser = userRepository.findAll().stream().findFirst().orElse(null);
            }
            
            if (adminUser == null) {
                return buildErrorResponse("Không tìm thấy user để tạo channel");
            }
            
            // Generate unique invite code (same logic as UserController)
            String inviteCode = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            Channel channel = new Channel(channelName.trim(), channelType, adminUser);
            channel.setDescription(description);
            channel.setIsActive(true);
            channel.setInviteCode(inviteCode);
            channel.setAutoApprove(true); // Default to auto-approve for admin-created channels
            channel.setMemberCount(1);
            
            channelRepository.save(channel);
            
            // Add creator as owner
            ChannelMember cm = new ChannelMember(channel, adminUser, "owner");
            cm.setStatus("approved");
            channelMemberRepository.save(cm);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Nhóm #" + channelName + " đã được tạo với mã mời: " + inviteCode);
            response.put("channelId", channel.getChannelId());
            response.put("channelName", channel.getChannelName());
            response.put("inviteCode", inviteCode);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Lỗi khi tạo channel: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật channel
     * PUT /api/admin/channels/{channelId}
     */
    @PutMapping("/channels/{channelId}")
    public ResponseEntity<?> updateChannel(@PathVariable Integer channelId, @RequestBody Map<String, String> payload) {
        // TODO: Thêm permission check khi auth hoàn tất
        // ResponseEntity<?> permCheck = requirePermission(Permission.ADMIN_MANAGE_CHANNELS);
        // if (permCheck != null) return permCheck;
        
        try {
            var channel = channelRepository.findById(channelId);
            if (channel.isEmpty()) {
                return buildErrorResponse("Channel không tồn tại");
            }
            
            Channel ch = channel.get();
            
            if (payload.containsKey("channelName")) {
                ch.setChannelName(payload.get("channelName"));
            }
            if (payload.containsKey("description")) {
                ch.setDescription(payload.get("description"));
            }
            if (payload.containsKey("channelType")) {
                ch.setChannelType(payload.get("channelType"));
            }
            
            channelRepository.save(ch);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Channel #" + ch.getChannelName() + " đã được cập nhật");
            response.put("channel", ch.getChannelName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Lỗi khi cập nhật channel: " + e.getMessage());
        }
    }
    
    /**
     * Xoá channel
     * DELETE /api/admin/channels/{channelId}
     */
    @PostMapping("/channels/{channelId}/delete")  // Using POST because DELETE might have issues with some clients
    public ResponseEntity<?> deleteChannel(@PathVariable Integer channelId) {
        // TODO: Thêm permission check khi auth hoàn tất
        // ResponseEntity<?> permCheck = requirePermission(Permission.ADMIN_MANAGE_CHANNELS);
        // if (permCheck != null) return permCheck;
        
        try {
            var channel = channelRepository.findById(channelId);
            if (channel.isEmpty()) {
                return buildErrorResponse("Channel không tồn tại");
            }
            
            Channel ch = channel.get();
            String channelName = ch.getChannelName();
            
            channelRepository.delete(ch);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Channel #" + channelName + " đã bị xoá");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Lỗi khi xoá channel: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật system settings
     * PUT /api/admin/settings
     */
    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> settings) {
        // TODO: Thêm permission check khi auth hoàn tất
        // ResponseEntity<?> permCheck = requirePermission(Permission.ADMIN_MANAGE_SYSTEM);
        // if (permCheck != null) return permCheck;
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "System settings updated successfully");
            response.put("settings", settings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse("Lỗi khi cập nhật settings: " + e.getMessage());
        }
    }
    
    /**
     * Helper method để tạo error response
     */
    private ResponseEntity<?> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", message);
        return ResponseEntity.status(500).body(response);
    }
}
