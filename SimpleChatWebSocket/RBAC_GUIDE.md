# Hệ Thống Phân Quyền (RBAC) - Simple Chat

## Tổng Quan

Ứng dụng thực hiện **Role-Based Access Control (RBAC)** với 3 vai trò chính:

1. **ADMIN** - Quản trị viên hệ thống
2. **MODERATOR** - Người kiểm duyệt nội dung
3. **USER** - Người dùng thường

## Demo Credentials

```
Admin:
  Username: admin
  Password: admin123

User:
  Username: user1
  Password: user123

Moderator:
  Username: mod1
  Password: mod123
```

## Phân Quyền Chi Tiết

### Admin Permissions

- `admin:view:users` - Xem danh sách tất cả users
- `admin:manage:users` - Quản lý users (ban, unban, etc.)
- `admin:manage:channels` - Quản lý channels
- `admin:view:analytics` - Xem thống kê hệ thống
- `admin:manage:system` - Cài đặt hệ thống
- `admin:view:logs` - Xem activity logs
- Plus: Tất cả user permissions

### Moderator Permissions

- `admin:manage:channels` - Quản lý channels
- `admin:view:logs` - Xem logs
- Plus: Tất cả user permissions

### User Permissions

- `user:create:channel` - Tạo channel
- `user:send:message` - Gửi tin nhắn
- `user:view:messages` - Xem tin nhắn
- `user:manage:profile` - Quản lý hồ sơ
- `user:join:channel` - Tham gia channels

## API Endpoints

### Authentication

```
POST /api/auth/login
  Body: { username, password }
  Response: { token, username, role, message }

POST /api/auth/signup
  Body: { username, password, email, fullName }
  Response: { token, username, role, message }

GET /api/auth/verify
  Header: Authorization: Bearer <token>

GET /api/auth/me
  Header: Authorization: Bearer <token>
  Response: { username, role, permissions, isAdmin }

POST /api/auth/logout
```

### Admin API (Requires admin role)

```
GET /api/admin/users - Xem tất cả users
GET /api/admin/users/{username} - Chi tiết user
POST /api/admin/users/{username}/ban - Ban user
POST /api/admin/users/{username}/unban - Unban user
GET /api/admin/logs - Xem activity logs
GET /api/admin/analytics - Xem thống kê
PUT /api/admin/settings - Cập nhật cài đặt
```

### User API (Requires authentication)

```
GET /api/users/profile - Hồ sơ cá nhân
PUT /api/users/profile - Cập nhật hồ sơ
GET /api/users/channels - Danh sách channels
POST /api/users/channels - Tạo channel
POST /api/users/channels/{id}/join - Tham gia channel
POST /api/users/channels/{id}/leave - Rời channel
GET /api/users/friends - Danh sách bạn bè
POST /api/users/change-password - Đổi mật khẩu
```

## Quy Trình Authorization

1. **Login** - User gửi username/password
2. **Authentication** - Server verify password, tạo token
3. **Token Storage** - Client lưu token vào localStorage
4. **Token Validation** - Sử dụng token trong Authorization header
5. **Permission Check** - Interceptor decode token, set RequestContext
6. **Access Control** - Controller kiểm tra permission trước xử lý

## Code Examples

### Sử dụng Authorization trong Controller

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController extends BaseController {

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        // Kiểm tra admin permission
        ResponseEntity<?> permCheck = requirePermission(Permission.ADMIN_VIEW_USERS);
        if (permCheck != null) return permCheck;

        // Xử lý logic...
        return ResponseEntity.ok(response);
    }
}
```

### Kiểm tra user role

```java
// Kiểm tra admin
if (isAdmin()) {
    // Cho phép admin actions
}

// Kiểm tra đăng nhập
if (isAuthenticated()) {
    // Cho phép authenticated actions
}

// Kiểm tra specific permission
if (hasPermission(Permission.USER_SEND_MESSAGE)) {
    // Cho phép gửi tin nhắn
}
```

### Frontend - Kiểm tra quyền trước gửi request

```javascript
// Sau khi login, lưu token
localStorage.setItem("token", data.token);

// Sử dụng token cho protected API
const response = await fetch("/api/admin/users", {
  headers: {
    Authorization: "Bearer " + localStorage.getItem("token"),
  },
});

if (response.status === 403) {
  console.log("Bạn không có quyền");
} else if (response.status === 401) {
  console.log("Vui lòng đăng nhập lại");
}
```

## Response Codes

- `200` - OK - Yêu cầu thành công
- `201` - Created - Tài nguyên được tạo
- `400` - Bad Request - Dữ liệu không hợp lệ
- `401` - Unauthorized - Chưa đăng nhập hoặc token hết hạn
- `403` - Forbidden - Không có quyền truy cập
- `404` - Not Found - Tài nguyên không tìm thấy
- `409` - Conflict - Tài nguyên đã tồn tại (duplicate username)
- `500` - Server Error - Lỗi server

## Security Features

✅ Password hashing với BCrypt
✅ Token-based authentication
✅ Role-based access control
✅ Request interceptor cho auto authorization
✅ Permission-based endpoint protection
✅ ThreadLocal context management
✅ Automatic context cleanup

## Cấu Trúc Classes

```
com.simplechat.security/
├── Permission.java - Định nghĩa quyền hạn
├── UserRole.java - Enum vai trò
├── RequestContext.java - Lưu thông tin user hiện tại
└── AuthorizationInterceptor.java - Interceptor kiểm tra token

com.simplechat.controller/
├── BaseController.java - Base class với methods phân quyền
├── AuthController.java - Login, logout, signup
├── UserController.java - User endpoints
└── AdminController.java - Admin endpoints
```

## Mở Rộng

Để thêm endpo int mới với RBAC:

```java
@PostMapping("/api/something")
public ResponseEntity<?> doSomething() {
    // Kiểm tra specific permission
    ResponseEntity<?> permCheck = requirePermission(Permission.SOME_ACTION);
    if (permCheck != null) return permCheck;

    // Tiếp tục xử lý...
}
```
