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

import com.simplechat.entity.Channel;
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", getCurrentUser().getUsername());
        response.put("role", getCurrentUser().getRole());
        response.put("email", getCurrentUser().getUsername() + "@simplechat.com");
        response.put("fullName", "User " + getCurrentUser().getUsername());
        response.put("avatar", "https://api.example.com/avatar/" + getCurrentUser().getUsername());
        response.put("joinedDate", "2026-04-01");
        response.put("lastLogin", System.currentTimeMillis());
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
                    .filter(m -> m.getChannel().getChannelId() == channel.getChannelId())
                    .count();
                channelMap.put("messageCount", realMessageCount);
                
                channelMap.put("isActive", channel.getIsActive());
                channelMap.put("createdAt", channel.getCreatedAt());
                return channelMap;
            }).collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("channels", channelList);
            response.put("count", channelList.size());
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
            int channelId = (int) messageData.get("channelId");
            String content = (String) messageData.get("content");
            
            Channel channel = channelRepository.findById(channelId).orElse(null);
            if (channel == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", true, "message", "Không tìm thấy channel"));
            }
            
            String currentUsername = getCurrentUser().getUsername();
            User user = userRepository.findByUsername(currentUsername).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", true, "message", "Không tìm thấy user"));
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
            response.put("message", "Tin nhắn đã được gửi thành công");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Lỗi khi gửi tin nhắn: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Tạo channel mới
     * POST /api/users/channels
     */
    @PostMapping("/channels")
    public ResponseEntity<?> createChannel(@RequestBody Map<String, String> channelData) {
        ResponseEntity<?> permCheck = requirePermission(Permission.USER_CREATE_CHANNEL);
        if (permCheck != null) return permCheck;
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Channel đã được tạo thành công");
        response.put("id", 4);
        response.put("name", channelData.get("name"));
        response.put("description", channelData.get("description"));
        response.put("createdBy", getCurrentUser().getUsername());
        response.put("createdAt", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Tham gia channel
     * POST /api/users/channels/{channelId}/join
     */
    @PostMapping("/channels/{channelId}/join")
    public ResponseEntity<?> joinChannel(@PathVariable int channelId) {
        ResponseEntity<?> permCheck = requirePermission(Permission.USER_JOIN_CHANNEL);
        if (permCheck != null) return permCheck;
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bạn đã tham gia kênh thành công");
        response.put("channelId", channelId);
        response.put("username", getCurrentUser().getUsername());
        response.put("joinedAt", System.currentTimeMillis());
        return ResponseEntity.ok(response);
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
            response.put("message", "Tin nhắn đã được gửi");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "message", "Lỗi: " + e.getMessage()));
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("friends", new Object[]{
            new Object[]{"username", "user1", "status", "online"},
            new Object[]{"username", "user2", "status", "offline"},
            new Object[]{"username", "mod1", "status", "online"}
        });
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
