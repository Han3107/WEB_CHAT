package com.simplechat.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.simplechat.dto.LoginRequest;
import com.simplechat.dto.LoginResponse;
import com.simplechat.entity.User;
import com.simplechat.repository.UserRepository;
import com.simplechat.security.Permission;
import com.simplechat.security.RequestContext;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRepository userRepository;
    
    public AuthController() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Login endpoint - authenticate user and return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();
            
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Vui lòng nhập username và password");
                error.put("code", "INVALID_INPUT");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Authenticate from database ONLY
            var userOptional = userRepository.findByUsername(username);
            
            if (!userOptional.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Tên đăng nhập hoặc mật khẩu không chính xác");
                error.put("code", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            User user = userOptional.get();
            
            // Check if user is banned
            if ("banned".equals(user.getStatus())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "⛔ Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
                error.put("code", "ACCOUNT_BANNED");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Verify password with BCrypt
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Tên đăng nhập hoặc mật khẩu không chính xác");
                error.put("code", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Generate JWT token
            String token = generateToken(username, user.getRole());
            
            // Create and set response
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUsername(username);
            response.setRole(user.getRole());
            response.setMessage("Đăng nhập thành công");
            
            // Set user in RequestContext
            RequestContext.setCurrentUser(response);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi server: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Signup endpoint - register new user
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody LoginRequest signupRequest) {
        try {
            String username = signupRequest.getUsername();
            String password = signupRequest.getPassword();
            String email = signupRequest.getEmail();
            String fullName = signupRequest.getFullName();
            
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Vui lòng nhập username và password");
                error.put("code", "INVALID_INPUT");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Check if username already exists in database
            if (userRepository.findByUsername(username).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Tên đăng nhập đã tồn tại");
                error.put("code", "CONFLICT");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
            
            // Check if email already exists
            if (email != null && !email.isEmpty() && userRepository.findByEmail(email).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Email đã được sử dụng");
                error.put("code", "CONFLICT");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
            
            // Create new user
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPasswordHash(passwordEncoder.encode(password));
            newUser.setEmail(email);
            newUser.setFullName(fullName);
            newUser.setRole("user");
            newUser.setStatus("active");
            
            // Save to database
            userRepository.save(newUser);
            
            // Generate token
            String token = generateToken(username, "user");
            
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUsername(username);
            response.setRole("user");
            response.setMessage("Đăng ký thành công!");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi server: " + e.getMessage());
            error.put("code", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Verify token endpoint
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || authHeader.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không tìm thấy token");
                error.put("code", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

            if (token.length() < 20) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Token không hợp lệ");
                error.put("code", "UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Token hợp lệ");
            response.put("valid", true);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Lỗi xác thực token");
            error.put("code", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        RequestContext.clearContext();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đăng xuất thành công");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        LoginResponse user = RequestContext.getCurrentUser();
        if (user == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Vui lòng đăng nhập");
            error.put("code", "UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("permissions", Permission.getPermissionsForRole(user.getRole()));
        response.put("isAdmin", "admin".equals(user.getRole()));
        return ResponseEntity.ok(response);
    }

    /**
     * Generate simplified JWT token (base64 encoded: username:role:timestamp)
     */
    private String generateToken(String username, String role) {
        long timestamp = System.currentTimeMillis();
        String tokenData = username + ":" + role + ":" + timestamp;
        return java.util.Base64.getEncoder().encodeToString(tokenData.getBytes());
    }
}
