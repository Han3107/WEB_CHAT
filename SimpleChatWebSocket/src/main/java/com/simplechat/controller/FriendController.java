package com.simplechat.controller;

import com.simplechat.entity.Friendship;
import com.simplechat.entity.User;
import com.simplechat.repository.FriendshipRepository;
import com.simplechat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendController extends BaseController {

    @Autowired private FriendshipRepository friendshipRepository;
    @Autowired private UserRepository userRepository;

    // ── Tìm kiếm user ──────────────────────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q) {
        if (q == null || q.trim().length() < 2)
            return ResponseEntity.badRequest().body(Map.of("error", true, "message", "Nhập ít nhất 2 ký tự"));
        try {
            User me = currentDbUser();
            List<User> results = userRepository.findAll().stream()
                .filter(u -> !u.getUserId().equals(me.getUserId()))
                .filter(u -> !"admin".equals(u.getRole()))
                .filter(u -> "active".equals(u.getStatus()))
                .filter(u -> u.getUsername().toLowerCase().contains(q.toLowerCase())
                          || (u.getFullName() != null && u.getFullName().toLowerCase().contains(q.toLowerCase())))
                .limit(20)
                .collect(Collectors.toList());

            List<Map<String, Object>> list = results.stream().map(u -> {
                Map<String, Object> m = new HashMap<>();
                m.put("username", u.getUsername());
                m.put("fullName", u.getFullName() != null ? u.getFullName() : "");
                m.put("role", u.getRole());
                // friendship status
                friendshipRepository.findBetween(me.getUserId(), u.getUserId()).ifPresentOrElse(
                    f -> m.put("friendStatus", f.getStatus()),
                    () -> m.put("friendStatus", "none")
                );
                return m;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("users", list, "count", list.size()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    // ── Gửi lời mời kết bạn ────────────────────────────────────────
    @PostMapping("/request/{username}")
    public ResponseEntity<?> sendRequest(@PathVariable String username) {
        try {
            User me = currentDbUser();
            User target = userRepository.findByUsername(username).orElse(null);
            if (target == null) return ResponseEntity.status(404).body(Map.of("error", true, "message", "Không tìm thấy user"));
            if (target.getUserId().equals(me.getUserId()))
                return ResponseEntity.badRequest().body(Map.of("error", true, "message", "Không thể kết bạn với chính mình"));

            Optional<Friendship> existing = friendshipRepository.findBetween(me.getUserId(), target.getUserId());
            if (existing.isPresent()) {
                String s = existing.get().getStatus();
                if ("accepted".equals(s)) return ResponseEntity.ok(Map.of("message", "Đã là bạn bè"));
                if ("pending".equals(s)) return ResponseEntity.ok(Map.of("message", "Lời mời đã được gửi trước đó"));
                if ("blocked".equals(s)) return ResponseEntity.status(403).body(Map.of("error", true, "message", "Không thể kết bạn"));
                // rejected → resend
                existing.get().setStatus("pending");
                friendshipRepository.save(existing.get());
                return ResponseEntity.ok(Map.of("message", "Đã gửi lại lời mời kết bạn"));
            }

            friendshipRepository.save(new Friendship(me, target));
            return ResponseEntity.ok(Map.of("message", "Đã gửi lời mời kết bạn đến " + username));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    // ── Chấp nhận / từ chối lời mời ────────────────────────────────
    @PostMapping("/respond/{friendshipId}")
    public ResponseEntity<?> respond(@PathVariable int friendshipId, @RequestBody Map<String, String> body) {
        try {
            User me = currentDbUser();
            Friendship f = friendshipRepository.findById(friendshipId).orElse(null);
            if (f == null) return ResponseEntity.status(404).body(Map.of("error", true, "message", "Không tìm thấy lời mời"));
            if (!f.getReceiver().getUserId().equals(me.getUserId()))
                return ResponseEntity.status(403).body(Map.of("error", true, "message", "Không có quyền"));

            String action = body.getOrDefault("action", "accept");
            if ("accept".equals(action)) {
                f.setStatus("accepted");
                friendshipRepository.save(f);
                return ResponseEntity.ok(Map.of("message", "Đã chấp nhận lời mời từ " + f.getRequester().getUsername()));
            } else {
                f.setStatus("rejected");
                friendshipRepository.save(f);
                return ResponseEntity.ok(Map.of("message", "Đã từ chối lời mời"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    // ── Danh sách bạn bè ───────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getFriends() {
        try {
            User me = currentDbUser();
            List<Friendship> friendships = friendshipRepository.findFriends(me.getUserId());
            List<Map<String, Object>> list = friendships.stream().map(f -> {
                User friend = f.getRequester().getUserId().equals(me.getUserId()) ? f.getReceiver() : f.getRequester();
                Map<String, Object> m = new HashMap<>();
                m.put("friendshipId", f.getFriendshipId());
                m.put("username", friend.getUsername());
                m.put("fullName", friend.getFullName() != null ? friend.getFullName() : "");
                m.put("role", friend.getRole());
                m.put("since", f.getUpdatedAt());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("friends", list, "count", list.size()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    // ── Lời mời chờ xử lý (nhận được) ─────────────────────────────
    @GetMapping("/pending")
    public ResponseEntity<?> getPending() {
        try {
            User me = currentDbUser();
            List<Friendship> pending = friendshipRepository.findPendingReceived(me.getUserId());
            List<Map<String, Object>> list = pending.stream().map(f -> {
                Map<String, Object> m = new HashMap<>();
                m.put("friendshipId", f.getFriendshipId());
                m.put("username", f.getRequester().getUsername());
                m.put("fullName", f.getRequester().getFullName() != null ? f.getRequester().getFullName() : "");
                m.put("sentAt", f.getCreatedAt());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("pending", list, "count", list.size()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    // ── Lời mời đã gửi đi (pending) ───────────────────────────────
    @GetMapping("/sent")
    public ResponseEntity<?> getSentPending() {
        try {
            User me = currentDbUser();
            List<Friendship> sent = friendshipRepository.findPendingSent(me.getUserId());
            List<Map<String, Object>> list = sent.stream().map(f -> {
                Map<String, Object> m = new HashMap<>();
                m.put("friendshipId", f.getFriendshipId());
                m.put("username", f.getReceiver().getUsername());
                m.put("fullName", f.getReceiver().getFullName() != null ? f.getReceiver().getFullName() : "");
                m.put("sentAt", f.getCreatedAt());
                return m;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("sent", list, "count", list.size()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    // ── Hủy lời mời đã gửi ─────────────────────────────────────────
    @DeleteMapping("/request/{username}")
    public ResponseEntity<?> cancelRequest(@PathVariable String username) {
        try {
            User me = currentDbUser();
            User target = userRepository.findByUsername(username).orElse(null);
            if (target == null) return ResponseEntity.status(404).body(Map.of("error", true, "message", "Không tìm thấy user"));

            Friendship f = friendshipRepository.findBetween(me.getUserId(), target.getUserId()).orElse(null);
            if (f == null || !"pending".equals(f.getStatus()) || !f.getRequester().getUserId().equals(me.getUserId())) {
                return ResponseEntity.status(404).body(Map.of("error", true, "message", "Không tìm thấy lời mời đã gửi"));
            }

            friendshipRepository.delete(f);
            return ResponseEntity.ok(Map.of("message", "Đã hủy lời mời kết bạn đến " + username));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    // ── Xóa bạn bè ─────────────────────────────────────────────────
    @DeleteMapping("/{username}")
    public ResponseEntity<?> removeFriend(@PathVariable String username) {
        try {
            User me = currentDbUser();
            User target = userRepository.findByUsername(username).orElse(null);
            if (target == null) return ResponseEntity.status(404).body(Map.of("error", true, "message", "Không tìm thấy user"));

            Friendship f = friendshipRepository.findBetween(me.getUserId(), target.getUserId()).orElse(null);
            if (f == null || !"accepted".equals(f.getStatus()))
                return ResponseEntity.status(404).body(Map.of("error", true, "message", "Không phải bạn bè"));

            friendshipRepository.delete(f);
            return ResponseEntity.ok(Map.of("message", "Đã xóa " + username + " khỏi danh sách bạn bè"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", true, "message", e.getMessage()));
        }
    }

    // ── Helper ─────────────────────────────────────────────────────
    private User currentDbUser() {
        return userRepository.findByUsername(getCurrentUser().getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
