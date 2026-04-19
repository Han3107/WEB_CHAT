package com.simplechat.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simplechat.dto.LoginResponse;
import com.simplechat.entity.Channel;
import com.simplechat.entity.ChannelMember;
import com.simplechat.entity.Message;
import com.simplechat.entity.User;
import com.simplechat.repository.ChannelMemberRepository;
import com.simplechat.repository.ChannelRepository;
import com.simplechat.repository.MessageRepository;
import com.simplechat.repository.UserRepository;
import com.simplechat.security.Permission;

/**
 * UserController - Quản lý hồ sơ và tài khoản user
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController extends BaseController {
    
    @Autowired
    private ChannelRepository channelRepository;
    
    @Autowired
    private ChannelMemberRepository channelMemberRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Lấy thông tin hồ sơ của user hiện tại
     * GET /api/users/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        // TODO: Thêm auth check khi auth hoàn tất
        // ResponseEntity<?> authCheck = requireAuth();
        // if (authCheck != null) return authCheck;

        String username = getCurrentUser().getUsername();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", true, "message", "Không tìm thấy user trong hệ thống"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("email", user.getEmail());
        response.put("fullName", user.getFullName());
        response.put("avatar", user.getAvatarUrl());
        response.put("joinedDate", user.getCreatedAt());
        response.put("lastLogin", user.getLastLogin());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cập nhật hồ sơ người dùng
     * PUT /api/users/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileData) {
        // TODO: Thêm permission check khi auth hoàn tất
        // ResponseEntity<?> permCheck = requirePermission(Permission.USER_MANAGE_PROFILE);
        // if (permCheck != null) return permCheck;
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hồ sơ đã được cập nhật thành công");
        response.put("username", getCurrentUser().getUsername());
        response.put("email", profileData.get("email"));
        response.put("fullName", profileData.get("fullName"));
        response.put("avatar", profileData.get("avatar"));
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy danh sách channels của user
     * GET /api/users/channels
     */
    @GetMapping("/channels")
    public ResponseEntity<?> getUserChannels() {
        // TODO: Thêm permission check khi auth hoàn tất
        // ResponseEntity<?> permCheck = requirePermission(Permission.USER_VIEW_MESSAGES);
        // if (permCheck != null) return permCheck;
        
        try {
            LoginResponse currentUser = getCurrentUser();
            User user = (currentUser != null) ? userRepository.findByUsername(currentUser.getUsername()).orElse(null) : null;
            
            System.out.println("[DEBUG] getUserChannels: currentUser=" + (currentUser != null ? currentUser.getUsername() : "null") + 
                               ", dbUserFound=" + (user != null));

            List<Channel> channels = channelRepository.findAll();
            List<Map<String, Object>> channelList = channels.stream().map(channel -> {
                Map<String, Object> channelMap = new HashMap<>();
                channelMap.put("channelId", channel.getChannelId());
                channelMap.put("channelName", channel.getChannelName());
                channelMap.put("description", channel.getDescription());
                channelMap.put("channelType", channel.getChannelType());
                channelMap.put("createdBy", channel.getCreatedBy().getUsername());
                
                // Get real member count from ChannelMember table
                Integer memberCount = channelMemberRepository.countMembersByChannel(channel.getChannelId());
                channelMap.put("memberCount", memberCount != null ? memberCount : 0);
                
                // Calculate real messageCount from messages table
                long realMessageCount = messageRepository.findAll().stream()
                    .filter(m -> m.getChannel().getChannelId().equals(channel.getChannelId()))
                    .count();
                channelMap.put("messageCount", realMessageCount);
                
                // Check if current user is member (approved only)
                boolean isMember = false;
                if (user != null) {
                    var membership = channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(
                        channel.getChannelId(), user.getUserId());
                    isMember = membership.isPresent() && "approved".equals(membership.get().getStatus());
                    String roleStr = user.getRole();
                    if (roleStr != null && roleStr.equalsIgnoreCase("admin")) isMember = true;
                }
                channelMap.put("isMember", isMember);
                // expose invite code and autoApprove to owner
                if (user != null) {
                    var membership = channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(channel.getChannelId(), user.getUserId());
                    if (membership.isPresent() && "owner".equals(membership.get().getRole())) {
                        channelMap.put("inviteCode", channel.getInviteCode());
                        channelMap.put("autoApprove", channel.getAutoApprove());
                    }
                    if (membership.isPresent()) channelMap.put("myRole", membership.get().getRole());
                }

                channelMap.put("isActive", channel.getIsActive());
                channelMap.put("createdAt", channel.getCreatedAt());
                return channelMap;
            }).collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("channels", channelList);
            response.put("count", channelList.size());
            
            // Diagnostic info
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("usernameInToken", currentUser != null ? currentUser.getUsername() : "NONE");
            debugInfo.put("dbUserFound", user != null);
            debugInfo.put("dbUserId", user != null ? user.getUserId() : "N/A");
            debugInfo.put("dbUserRole", user != null ? user.getRole() : "N/A");
            response.put("debugInfo", debugInfo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Lỗi khi lấy danh sách channels: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Lấy tin nhắn của một channel
     * GET /api/users/channels/{channelId}/messages?page=0&size=50
     */
    @GetMapping("/channels/{channelId}/messages")
    public ResponseEntity<?> getChannelMessages(@PathVariable int channelId, 
                                               org.springframework.data.domain.Pageable pageable) {
        try {
            Page<Message> messagesPage = messageRepository.findActiveMessagesByChannel(channelId, pageable);
            List<Map<String, Object>> messageList = messagesPage.getContent().stream()
                .map(message -> {
                    Map<String, Object> msgMap = new HashMap<>();
                    msgMap.put("messageId", message.getMessageId());
                    msgMap.put("sender", message.getUser().getUsername());
                    msgMap.put("content", message.getContent());
                    msgMap.put("createdAt", message.getCreatedAt());
                    msgMap.put("role", message.getUser().getRole());
                    return msgMap;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("messages", messageList);
            response.put("totalElements", messagesPage.getTotalElements());
            response.put("currentPage", pageable.getPageNumber());
            response.put("pageSize", pageable.getPageSize());
            response.put("totalPages", messagesPage.getTotalPages());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Lỗi khi lấy tin nhắn: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Gửi tin nhắn mới
     * POST /api/users/messages
     */
    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> messageData) {
        try {
            LoginResponse currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", true, "message", "Vui lòng đăng nhập trước"));
            }
            
            Object channelIdObj = messageData.get("channelId");
            if (channelIdObj == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", true, "message", "Thiếu thông tin channelId"));
            }
            int channelId = Integer.parseInt(channelIdObj.toString());
            String content = (String) messageData.get("content");
            
            Channel channel = channelRepository.findById(channelId).orElse(null);
            if (channel == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", true, "message", "Không tìm thấy channel"));
            }
            
            User user = userRepository.findByUsername(currentUser.getUsername()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", true, "message", "Không tìm thấy user"));
            }

            // Check membership
            boolean isMember = channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(
                channelId, user.getUserId()).isPresent();
            
            String roleStr = user.getRole();
            if (!isMember && (roleStr == null || !roleStr.equalsIgnoreCase("admin"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", true, "message", "Bạn phải tham gia kênh trước khi nhắn tin"));
            }
            
            Message message = new Message();
            message.setChannel(channel);
            message.setUser(user);
            message.setContent(content);
            message.setCreatedAt(LocalDateTime.now());
            message.setIsDeleted(false);
            
            Message savedMessage = messageRepository.save(message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("messageId", savedMessage.getMessageId());
            response.put("sender", savedMessage.getUser().getUsername());
            response.put("content", savedMessage.getContent());
            response.put("createdAt", savedMessage.getCreatedAt());
            response.put("role", savedMessage.getUser().getRole());
            response.put("message", "Tin nhắn đã được gửi thành công");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.err.println("[ERROR] sendMessage failed: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Lỗi server khi gửi tin nhắn: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Tạo channel mới (user)
     * POST /api/users/channels
     */
    @PostMapping("/channels")
    public ResponseEntity<?> createChannel(@RequestBody Map<String, Object> channelData) {
        ResponseEntity<?> authCheck = requireAuth();
        if (authCheck != null) return authCheck;
        try {
            LoginResponse currentUser = getCurrentUser();
            User user = userRepository.findByUsername(currentUser.getUsername()).orElse(null);
            if (user == null) return ResponseEntity.status(404).body(Map.of("error", true, "message", "User không tồn tại"));

            String name = (String) channelData.get("channelName");
            String desc = (String) channelData.getOrDefault("description", "");
            String type = (String) channelData.getOrDefault("channelType", "group");
            boolean autoApprove = channelData.get("autoApprove") == null || Boolean.parseBoolean(channelData.get("autoApprove").toString());

            if (name == null || name.trim().isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", true, "message", "Tên nhóm không được để trống"));

            // Generate unique invite code
            String inviteCode = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Channel channel = new Channel(name.trim(), type, user);
            channel.setDescription(desc);
            channel.setIsActive(true);
            channel.setInviteCode(inviteCode);
            channel.setAutoApprove(autoApprove);
            channelRepository.save(channel);

            // Add creator as owner
            ChannelMember cm = new ChannelMember(channel, user, "owner");
            cm.setStatus("approved");
            channelMemberRepository.save(cm);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Tạo nhóm thành công",
                "channelId", channel.getChannelId(),
                "channelName", channel.getChannelName(),
                "inviteCode", inviteCode
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    /**
     * Tham gia nhóm theo mã mời
     * POST /api/users/channels/join-by-code
     */
    @PostMapping("/channels/join-by-code")
    public ResponseEntity<?> joinByCode(@RequestBody Map<String, String> body) {
        ResponseEntity<?> authCheck = requireAuth();
        if (authCheck != null) return authCheck;
        try {
            String code = body.get("inviteCode");
            if (code == null || code.trim().isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", true, "message", "Thiếu mã mời"));

            Channel channel = channelRepository.findByInviteCode(code.trim().toUpperCase()).orElse(null);
            if (channel == null)
                return ResponseEntity.status(404).body(Map.of("error", true, "message", "Mã mời không hợp lệ"));

            User user = userRepository.findByUsername(getCurrentUser().getUsername()).orElse(null);
            if (user == null) return ResponseEntity.status(404).body(Map.of("error", true, "message", "User không tồn tại"));

            // Already member?
            if (channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(channel.getChannelId(), user.getUserId()).isPresent())
                return ResponseEntity.ok(Map.of("message", "Bạn đã là thành viên của nhóm này", "channelId", channel.getChannelId()));

            String status = Boolean.TRUE.equals(channel.getAutoApprove()) ? "approved" : "pending";
            ChannelMember cm = new ChannelMember(channel, user, "member");
            cm.setStatus(status);
            channelMemberRepository.save(cm);

            if ("pending".equals(status))
                return ResponseEntity.ok(Map.of("message", "Yêu cầu tham gia đã được gửi, chờ duyệt", "status", "pending", "channelId", channel.getChannelId()));

            return ResponseEntity.ok(Map.of("message", "Tham gia nhóm thành công", "status", "approved", "channelId", channel.getChannelId(), "channelName", channel.getChannelName()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    /**
     * Tìm kiếm nhóm theo mã mời
     * GET /api/users/channels/search-by-code
     */
    @GetMapping("/channels/search-by-code")
    public ResponseEntity<?> searchByCode(@org.springframework.web.bind.annotation.RequestParam String code) {
        try {
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", true, "message", "Thiếu mã mời"));
            }
            Channel channel = channelRepository.findByInviteCode(code.trim().toUpperCase()).orElse(null);
            if (channel == null) {
                return ResponseEntity.status(404).body(Map.of("error", true, "message", "Không tìm thấy nhóm"));
            }

            Map<String, Object> channelMap = new HashMap<>();
            channelMap.put("channelId", channel.getChannelId());
            channelMap.put("channelName", channel.getChannelName());
            channelMap.put("description", channel.getDescription());
            channelMap.put("channelType", channel.getChannelType());
            channelMap.put("inviteCode", channel.getInviteCode());
            return ResponseEntity.ok(Map.of("channel", channelMap));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách thành viên chờ duyệt
     * GET /api/users/channels/{channelId}/pending
     */
    @GetMapping("/channels/{channelId}/pending")
    public ResponseEntity<?> getPendingMembers(@PathVariable int channelId) {
        ResponseEntity<?> authCheck = requireAuth();
        if (authCheck != null) return authCheck;
        try {
            User user = userRepository.findByUsername(getCurrentUser().getUsername()).orElse(null);
            ChannelMember myMembership = channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(channelId, user.getUserId()).orElse(null);
            
            // Allow if user is owner/moderator OR if user is a system Admin
            boolean isAuthorized = (myMembership != null && (myMembership.getRole().equals("owner") || myMembership.getRole().equals("moderator")))
                                || (user != null && "admin".equalsIgnoreCase(user.getRole()));

            if (!isAuthorized)
                return ResponseEntity.status(403).body(Map.of("error", true, "message", "Bạn không có quyền xem danh sách chờ"));

            List<com.simplechat.entity.ChannelMember> pending = channelMemberRepository.findByChannel_ChannelIdAndStatus(channelId, "pending");
            List<Map<String, Object>> list = pending.stream().map(cm -> Map.<String, Object>of(
                "memberId", cm.getMemberId(),
                "username", cm.getUser().getUsername(),
                "fullName", cm.getUser().getFullName() != null ? cm.getUser().getFullName() : "",
                "joinedAt", cm.getJoinedAt()
            )).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("pending", list, "count", list.size()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    /**
     * Duyệt / từ chối thành viên chờ
     * POST /api/users/channels/{channelId}/approve/{memberId}
     */
    @PostMapping("/channels/{channelId}/approve/{memberId}")
    public ResponseEntity<?> approveMember(@PathVariable int channelId, @PathVariable int memberId,
                                           @RequestBody Map<String, String> body) {
        ResponseEntity<?> authCheck = requireAuth();
        if (authCheck != null) return authCheck;
        try {
            User user = userRepository.findByUsername(getCurrentUser().getUsername()).orElse(null);
            ChannelMember myMembership = channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(channelId, user.getUserId()).orElse(null);
            
            // Allow if user is owner/moderator OR if user is a system Admin
            boolean isAuthorized = (myMembership != null && (myMembership.getRole().equals("owner") || myMembership.getRole().equals("moderator")))
                                || (user != null && "admin".equalsIgnoreCase(user.getRole()));

            if (!isAuthorized)
                return ResponseEntity.status(403).body(Map.of("error", true, "message", "Bạn không có quyền duyệt thành viên"));

            ChannelMember target = channelMemberRepository.findById(memberId).orElse(null);
            if (target == null) return ResponseEntity.status(404).body(Map.of("error", true, "message", "Không tìm thấy thành viên"));

            String action = body.getOrDefault("action", "approve");
            if ("approve".equals(action)) {
                target.setStatus("approved");
                channelMemberRepository.save(target);
                return ResponseEntity.ok(Map.of("message", "Đã duyệt thành viên " + target.getUser().getUsername()));
            } else {
                channelMemberRepository.delete(target);
                return ResponseEntity.ok(Map.of("message", "Đã từ chối thành viên " + target.getUser().getUsername()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    /**
     * Bật/tắt tự duyệt thành viên
     * POST /api/users/channels/{channelId}/auto-approve
     */
    @PostMapping("/channels/{channelId}/auto-approve")
    public ResponseEntity<?> toggleAutoApprove(@PathVariable int channelId, @RequestBody Map<String, Boolean> body) {
        ResponseEntity<?> authCheck = requireAuth();
        if (authCheck != null) return authCheck;
        try {
            User user = userRepository.findByUsername(getCurrentUser().getUsername()).orElse(null);
            ChannelMember myMembership = channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(channelId, user.getUserId()).orElse(null);
            if (myMembership == null || !myMembership.getRole().equals("owner"))
                return ResponseEntity.status(403).body(Map.of("error", true, "message", "Chỉ owner mới có thể thay đổi cài đặt này"));

            Channel channel = channelRepository.findById(channelId).orElse(null);
            if (channel == null) return ResponseEntity.status(404).body(Map.of("error", true, "message", "Không tìm thấy nhóm"));

            boolean newVal = Boolean.TRUE.equals(body.get("autoApprove"));
            channel.setAutoApprove(newVal);
            channelRepository.save(channel);
            return ResponseEntity.ok(Map.of("message", newVal ? "Đã bật tự duyệt" : "Đã tắt tự duyệt", "autoApprove", newVal));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    /**
     * Mời thành viên vào nhóm
     * POST /api/users/channels/{channelId}/invite
     */
    @PostMapping("/channels/{channelId}/invite")
    public ResponseEntity<?> inviteMember(@PathVariable int channelId, @RequestBody Map<String, String> body) {
        ResponseEntity<?> authCheck = requireAuth();
        if (authCheck != null) return authCheck;
        try {
            User inviter = userRepository.findByUsername(getCurrentUser().getUsername()).orElse(null);
            ChannelMember myMembership = channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(channelId, inviter.getUserId()).orElse(null);
            if (myMembership == null || myMembership.getStatus().equals("pending"))
                return ResponseEntity.status(403).body(Map.of("error", true, "message", "Bạn không phải thành viên của nhóm này"));

            String targetUsername = body.get("username");
            User target = userRepository.findByUsername(targetUsername).orElse(null);
            if (target == null) return ResponseEntity.status(404).body(Map.of("error", true, "message", "Không tìm thấy user " + targetUsername));

            Channel channel = channelRepository.findById(channelId).orElse(null);
            if (channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(channelId, target.getUserId()).isPresent())
                return ResponseEntity.ok(Map.of("message", target.getUsername() + " đã là thành viên của nhóm"));

            ChannelMember cm = new ChannelMember(channel, target, "member");
            cm.setStatus("approved"); // Invited = auto approved
            channelMemberRepository.save(cm);
            return ResponseEntity.ok(Map.of("message", "Đã mời " + target.getUsername() + " vào nhóm"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }
    
    /**
     * Tham gia channel
     * POST /api/users/channels/{channelId}/join
     */
    @PostMapping("/channels/{channelId}/join")
    public ResponseEntity<?> joinChannel(@PathVariable int channelId) {
        ResponseEntity<?> authCheck = requireAuth();
        if (authCheck != null) return authCheck;

        LoginResponse currentUser = getCurrentUser();
        User user = userRepository.findByUsername(currentUser.getUsername()).orElse(null);
        Channel channel = channelRepository.findById(channelId).orElse(null);

        if (user == null || channel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", true, "message", "User hoặc Channel không tồn tại"));
        }

        // Check if already a member (any status)
        var existing = channelMemberRepository.findByChannel_ChannelIdAndUser_UserId(channelId, user.getUserId());
        if (existing.isPresent()) {
            String s = existing.get().getStatus();
            if ("approved".equals(s)) return ResponseEntity.ok(Map.of("message", "Bạn đã là thành viên của kênh này"));
            if ("pending".equals(s))  return ResponseEntity.ok(Map.of("message", "Yêu cầu của bạn đang chờ duyệt", "status", "pending"));
        }

        // Determine status based on autoApprove
        boolean autoApprove = Boolean.TRUE.equals(channel.getAutoApprove());
        String status = autoApprove ? "approved" : "pending";

        ChannelMember membership = new ChannelMember();
        membership.setChannel(channel);
        membership.setUser(user);
        membership.setRole("member");
        membership.setStatus(status);
        membership.setJoinedAt(LocalDateTime.now());
        channelMemberRepository.save(membership);

        if ("pending".equals(status)) {
            return ResponseEntity.ok(Map.of(
                "message", "Yêu cầu tham gia đã được gửi, chờ owner duyệt",
                "status", "pending",
                "channelId", channelId
            ));
        }

        return ResponseEntity.ok(Map.of(
            "message", "Bạn đã tham gia kênh thành công",
            "status", "approved",
            "channelId", channelId,
            "username", user.getUsername()
        ));
    }
    
    /**
     * Rời khỏi channel
     * POST /api/users/channels/{channelId}/leave
     */
    @PostMapping("/channels/{channelId}/leave")
    public ResponseEntity<?> leaveChannel(@PathVariable int channelId) {
        ResponseEntity<?> permCheck = requirePermission(Permission.USER_JOIN_CHANNEL);
        if (permCheck != null) return permCheck;
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bạn đã rời khỏi kênh");
        response.put("channelId", channelId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy danh sách tất cả users (để DM)
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            
            List<Map<String, Object>> userList = users.stream()
                .filter(u -> "active".equals(u.getStatus())) // Only active users
                .map(u -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("username", u.getUsername());
                    userMap.put("fullName", u.getFullName());
                    userMap.put("role", u.getRole());
                    userMap.put("email", u.getEmail());
                    return userMap;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("users", userList);
            response.put("count", userList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy tin nhắn riêng với user
     * GET /api/users/dm/{recipientUsername}?sender={senderUsername}
     */
    @GetMapping("/dm/{recipientUsername}")
    public ResponseEntity<?> getDirectMessages(@PathVariable String recipientUsername,
                                               @org.springframework.web.bind.annotation.RequestParam String sender) {
        try {
            User senderUser = userRepository.findByUsername(sender).orElse(null);
            User recipientUser = userRepository.findByUsername(recipientUsername).orElse(null);
            
            if (senderUser == null || recipientUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", true, "message", "Không tìm thấy user"));
            }
            
            // Get all messages between the two users
            List<Message> messages = messageRepository.findAll().stream()
                .filter(m -> m.getIsDeleted() == false)
                .filter(m -> {
                    String channelName = m.getChannel().getChannelName();
                    return channelName.equals("dm:" + recipientUsername) || 
                           channelName.equals("dm:" + sender);
                })
                .filter(m -> m.getUser().getUserId().equals(senderUser.getUserId()) ||
                            m.getUser().getUserId().equals(recipientUser.getUserId()))
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .collect(Collectors.toList());
            
            List<Map<String, Object>> messageList = messages.stream()
                .map(msg -> {
                    Map<String, Object> msgMap = new HashMap<>();
                    msgMap.put("messageId", msg.getMessageId());
                    msgMap.put("sender", msg.getUser().getUsername());
                    msgMap.put("content", msg.getContent());
                    msgMap.put("createdAt", msg.getCreatedAt());
                    msgMap.put("role", msg.getUser().getRole());
                    return msgMap;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("messages", messageList);
            response.put("recipientUsername", recipientUsername);
            response.put("count", messageList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Gửi tin nhắn riêng
     * POST /api/users/dm
     */
    @PostMapping("/dm")
    public ResponseEntity<?> sendDirectMessage(@RequestBody Map<String, String> dmData) {
        try {
            String senderUsername = dmData.get("senderUsername");
            String recipientUsername = dmData.get("recipientUsername");
            String content = dmData.get("content");
            
            if (senderUsername == null || recipientUsername == null || content == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", true, "message", "Thiếu thông tin: senderUsername, recipientUsername hoặc content"));
            }
            
            User sender = userRepository.findByUsername(senderUsername).orElse(null);
            User recipient = userRepository.findByUsername(recipientUsername).orElse(null);
            
            if (sender == null || recipient == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", true, "message", "Không tìm thấy user"));
            }
            
            // Use or create a virtual DM channel
            String dmChannelName = "dm:" + recipientUsername;
            Channel dmChannel = channelRepository.findAll().stream()
                .filter(c -> c.getChannelName().equals(dmChannelName))
                .findFirst()
                .orElse(null);
            
            // If DM channel doesn't exist, create it on the fly
            if (dmChannel == null) {
                dmChannel = new Channel();
                dmChannel.setChannelName(dmChannelName);
                dmChannel.setChannelType("private");
                dmChannel.setDescription("Direct message: " + senderUsername + " <-> " + recipientUsername);
                dmChannel.setCreatedBy(sender);
                dmChannel.setIsActive(true);
                channelRepository.save(dmChannel);
            }
            
            Message message = new Message();
            message.setChannel(dmChannel);
            message.setUser(sender);
            message.setContent(content);
            message.setCreatedAt(LocalDateTime.now());
            message.setIsDeleted(false);
            
            Message savedMessage = messageRepository.save(message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("messageId", savedMessage.getMessageId());
            response.put("sender", savedMessage.getUser().getUsername());
            response.put("recipient", recipientUsername);
            response.put("content", savedMessage.getContent());
            response.put("createdAt", savedMessage.getCreatedAt());
            response.put("role", savedMessage.getUser().getRole());
            response.put("message", "Tin nhắn đã được gửi");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "message", "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách thành viên của channel
     * GET /api/users/channels/{channelId}/members
     */
    @GetMapping("/channels/{channelId}/members")
    public ResponseEntity<?> getChannelMembers(@PathVariable int channelId) {
        try {
            List<com.simplechat.entity.ChannelMember> members = channelMemberRepository.findByChannel_ChannelId(channelId);
            List<Map<String, Object>> memberList = members.stream().map(cm -> {
                Map<String, Object> m = new HashMap<>();
                m.put("username", cm.getUser().getUsername());
                m.put("fullName", cm.getUser().getFullName());
                m.put("role", cm.getRole());
                m.put("userRole", cm.getUser().getRole());
                m.put("joinedAt", cm.getJoinedAt());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("members", memberList, "count", memberList.size()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách bạn bè
     * GET /api/users/friends
     */
    @GetMapping("/friends")
    public ResponseEntity<?> getFriends() {
        ResponseEntity<?> authCheck = requireAuth();
        if (authCheck != null) return authCheck;

        LoginResponse currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", true, "message", "Vui lòng đăng nhập trước"));
        }

        // Lấy userId hiện tại
        User user = userRepository.findByUsername(currentUser.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", true, "message", "Không tìm thấy user trong hệ thống"));
        }

        // Lấy tất cả channel mà user này là thành viên
        List<com.simplechat.entity.ChannelMember> myMemberships = channelMemberRepository.findByUser_UserId(user.getUserId());
        // Lấy tất cả userId trong các channel đó (trừ chính mình)
        java.util.Set<Integer> friendIds = new java.util.HashSet<>();
        for (com.simplechat.entity.ChannelMember cm : myMemberships) {
            List<com.simplechat.entity.ChannelMember> members = channelMemberRepository.findByChannel_ChannelId(cm.getChannel().getChannelId());
            for (com.simplechat.entity.ChannelMember m : members) {
                if (!m.getUser().getUserId().equals(user.getUserId())) {
                    friendIds.add(m.getUser().getUserId());
                }
            }
        }
        // Lấy thông tin user bạn bè
        List<User> friends = userRepository.findAllById(friendIds);
        List<Map<String, Object>> friendList = friends.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("username", u.getUsername());
            map.put("fullName", u.getFullName());
            map.put("email", u.getEmail());
            map.put("role", u.getRole());
            map.put("avatar", u.getAvatarUrl());
            map.put("status", u.getStatus());
            return map;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("friends", friendList);
        response.put("count", friendList.size());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Thay đổi mật khẩu
     * POST /api/users/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwords) {
        ResponseEntity<?> permCheck = requirePermission(Permission.USER_MANAGE_PROFILE);
        if (permCheck != null) return permCheck;
        
        String oldPassword = passwords.get("oldPassword");
        String newPassword = passwords.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Thiếu thông tin mật khẩu", "INVALID_INPUT");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Mật khẩu đã được thay đổi thành công");
        response.put("username", getCurrentUser().getUsername());
        response.put("changedAt", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
