# 🚀 Hướng Dẫn Triển Khai Hệ Thống Admin SimpleChatWebSocket

## 📦 File được cung cấp

1. **database.sql** - Schema database hoàn chỉnh (14 bảng + Views + Indexes)
2. **sample_data.sql** - Dữ liệu mẫu để test
3. **ADMIN_SYSTEM_DESIGN.md** - Tài liệu thiết kế chi tiết
4. **README_ADMIN.md** - Hướng dẫn này

---

## 🛠️ Setup Database

### Bước 1: Tạo Database

```bash
# Bằng MySQL Workbench hoặc CLI
mysql -u root -p

# Trong MySQL shell:
CREATE DATABASE simplechat;
USE simplechat;
```

### Bước 2: Chạy Schema SQL

```bash
# Upload database.sql vào MySQL Workbench
# Hoặc dùng CLI:
mysql -u root -p simplechat < database.sql

# Hoặc copy-paste toàn bộ nội dung database.sql vào MySQL Workbench
```

### Bước 3: Chạy Sample Data (Optional - để test)

```bash
mysql -u root -p simplechat < sample_data.sql
```

### Bước 4: Kiểm tra lại

```sql
-- Xem tất cả bảng
SHOW TABLES;

-- Xem số lượng dữ liệu
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM channels;
SELECT COUNT(*) FROM messages;

-- Xem views
SHOW FULL TABLES WHERE TABLE_TYPE = 'VIEW';
```

---

## 💻 Mô Hình Dữ Liệu

```
USERS (Người dùng)
  ├── Role: admin, moderator, user
  ├── Status: active, inactive, banned
  └── Permissions: Quyền hạn chi tiết

CHANNELS (Kênh Chat)
  ├── Type: public, private, group
  ├── Members: Danh sách thành viên
  └── Messages: Tin nhắn trong kênh

MESSAGES (Tin nhắn)
  ├── User: Người gửi
  ├── Channel: Kênh
  ├── Type: text, image, file, vote, quiz, system
  └── Editable/Deletable: Có thể sửa/xóa

TASKS (Tác vụ tương tác)
  ├── Vote: Bình chọn
  ├── Quiz: Câu hỏi
  ├── File: Chia sẻ file
  └── Board: Thông báo

ACTIVITY_LOGS (Nhật ký)
  └── Ghi lại mọi hành động người dùng

STATISTICS (Thống kê)
  └── Dữ liệu tổng hợp hàng ngày
```

---

## 🔧 Cấu Hình Java/Spring Boot

### application.properties

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/simplechat
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# Tomcat
server.port=8080
server.servlet.context-path=/SimpleChatWebSocket

# Logging
logging.level.root=INFO
logging.level.com.simplechat=DEBUG
```

### pom.xml (Thêm dependencies)

```xml
<!-- MySQL Connector -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
    <version>3.0.5</version>
</dependency>

<!-- MySQL Driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
    <scope>runtime</scope>
</dependency>

<!-- BCrypt for Password Hashing -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
    <version>6.0.2</version>
</dependency>

<!-- JWT for Authentication -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

---

## 📁 Cấu Trúc Project (Với Admin)

```
SimpleChatWebSocket/
├── src/main/java/com/simplechat/
│   ├── ChatEndpoint.java (WebSocket)
│   ├── Server.java (Tomcat Server)
│   │
│   ├── model/
│   │   ├── User.java
│   │   ├── Channel.java
│   │   ├── Message.java
│   │   ├── Task.java
│   │   ├── ActivityLog.java
│   │   └── Statistics.java
│   │
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── ChannelRepository.java
│   │   ├── MessageRepository.java
│   │   ├── TaskRepository.java
│   │   └── ActivityLogRepository.java
│   │
│   ├── service/
│   │   ├── UserService.java
│   │   ├── ChannelService.java
│   │   ├── MessageService.java
│   │   ├── AdminService.java
│   │   └── AnalyticsService.java
│   │
│   ├── controller/
│   │   ├── AdminController.java
│   │   ├── UserController.java
│   │   ├── ChannelController.java
│   │   └── AnalyticsController.java
│   │
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── SecurityConfig.java
│   │   └── AuthFilter.java
│   │
│   └── dto/
│       ├── UserDTO.java
│       ├── ChannelDTO.java
│       ├── MessageDTO.java
│       └── DashboardDTO.java
│
├── src/main/webapp/
│   ├── index.html (Chat Client)
│   ├── admin/
│   │   ├── index.html (Admin Dashboard)
│   │   ├── users.html (User Management)
│   │   ├── channels.html (Channel Management)
│   │   ├── messages.html (Message Moderation)
│   │   ├── analytics.html (Analytics)
│   │   └── css/
│   │   │   └── admin.css
│   │   └── js/
│   │       ├── admin-api.js (API calls)
│   │       ├── admin-dashboard.js
│   │       ├── admin-users.js
│   │       ├── admin-channels.js
│   │       └── admin-analytics.js
│   │
│   ├── profile/
│   │   ├── index.html (User Profile)
│   │   ├── css/profile.css
│   │   └── js/profile.js
│   │
│   └── WEB-INF/
│       └── web.xml
│
├── database.sql (Schema)
├── sample_data.sql (Test Data)
├── pom.xml (Maven)
└── README.md
```

---

## 🔐 Rolebase Access Control (RBAC)

### Admin Permissions

```
✅ /admin/dashboard - Dashboard
✅ /admin/users - User Management
✅ /admin/channels - Channel Management
✅ /admin/messages - Message Moderation
✅ /admin/analytics - Analytics & Reports
✅ /admin/logs - Activity Logs
✅ /admin/settings - System Settings
```

### Moderator Permissions

```
✅ /moderator/dashboard - Limited Dashboard
✅ /moderator/messages - Message Review
✅ /moderator/reports - User Reports
✅ /moderator/channels - Channel Management (assigned channels only)
❌ /admin/* - No admin access
```

### User Permissions

```
✅ /profile - User Profile
✅ / - Chat
✅ /api/channels - View my channels
❌ /admin/* - No admin access
❌ /moderator/* - No moderator access
```

---

## 📊 Các REST API Endpoint

### Authentication

```
POST   /api/auth/login           - Đăng nhập
POST   /api/auth/register        - Đăng ký
POST   /api/auth/logout          - Đăng xuất
POST   /api/auth/refresh-token   - Làm mới token
```

### Admin Users

```
GET    /api/admin/users          - Danh sách user
GET    /api/admin/users/{id}     - Chi tiết user
POST   /api/admin/users          - Tạo user mới
PUT    /api/admin/users/{id}     - Cập nhật user
DELETE /api/admin/users/{id}     - Xóa user
POST   /api/admin/users/{id}/ban - Ban user
POST   /api/admin/users/{id}/unban - Unban user
```

### Admin Channels

```
GET    /api/admin/channels       - Danh sách channel
GET    /api/admin/channels/{id}  - Chi tiết channel
POST   /api/admin/channels       - Tạo channel mới
PUT    /api/admin/channels/{id}  - Cập nhật channel
DELETE /api/admin/channels/{id}  - Xóa channel
GET    /api/admin/channels/{id}/members - Members
POST   /api/admin/channels/{id}/members - Thêm member
```

### Admin Messages

```
GET    /api/admin/messages       - Danh sách message
GET    /api/admin/messages/{id}  - Chi tiết message
DELETE /api/admin/messages/{id}  - Xóa message
GET    /api/admin/reports        - Messages being reported
```

### Admin Analytics

```
GET    /api/admin/analytics/dashboard   - Dashboard data
GET    /api/admin/analytics/users       - User statistics
GET    /api/admin/analytics/channels    - Channel statistics
GET    /api/admin/analytics/messages    - Message statistics
GET    /api/admin/analytics/engagement  - User engagement
GET    /api/admin/analytics/daily       - Daily reports
```

### Admin Logs

```
GET    /api/admin/logs           - Activity logs
GET    /api/admin/logs?action=LOG_TYPE - Filter logs
```

### User Profile

```
GET    /api/profile              - My profile
PUT    /api/profile              - Update profile
GET    /api/profile/statistics   - My statistics
GET    /api/profile/messages/saved - Saved messages
POST   /api/profile/messages/save - Save message
```

---

## 🧪 Testing

### Test dữ liệu đã tạo

```bash
# 1. Đăng nhập Admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"your_password"}'

# Response:
# {
#   "token": "eyJhbGc...",
#   "user": {
#     "id": 1,
#     "username": "admin",
#     "role": "admin"
#   }
# }

# 2. Lấy Dashboard data
curl -X GET http://localhost:8080/api/admin/analytics/dashboard \
  -H "Authorization: Bearer eyJhbGc..."

# 3. Lấy danh sách users
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer eyJhbGc..."

# 4. Xem activity logs
curl -X GET http://localhost:8080/api/admin/logs \
  -H "Authorization: Bearer eyJhbGc..."
```

---

## 📈 Key Features

### 1. Dashboard Analytics

- Tổng người dùng (active/inactive/banned)
- Tổng kênh
- Tổng tin nhắn
- Biểu đồ hoạt động 7 ngày
- Top users, top channels

### 2. User Management

- Tạo/Edit/Delete users
- Ban/Unban users
- Role assignment (admin/moderator/user)
- Password reset
- Activity history

### 3. Channel Management

- Tạo/Edit/Delete channels
- Member management
- Growth statistics
- Message count tracking

### 4. Message Moderation

- View reported messages
- Delete inappropriate content
- Ban users (auto or manual)
- Content review workflow

### 5. Statistics & Analytics

- User engagement metrics
- Channel growth
- Daily/weekly/monthly reports
- Vote/Quiz analytics
- Export to PDF/CSV

### 6. Activity Logs

- Audit trail
- User actions tracking
- IP logging
- Timestamp recording

### 7. System Settings

- Server configuration
- Security settings
- Email configuration
- Feature toggles
- Rate limiting

---

## ⚙️ Performance Optimization

### Indexes Created

```sql
CREATE INDEX idx_messages_channel_created ON messages(channel_id, created_at DESC);
CREATE INDEX idx_messages_user_created ON messages(user_id, created_at DESC);
CREATE INDEX idx_activity_date ON activity_logs(created_at DESC);
CREATE INDEX idx_channel_members_user ON channel_members(user_id);
CREATE INDEX idx_banned_users_active ON banned_users(ban_end, is_permanent);
```

### Query Optimization Tips

1. Sử dụng pagination cho danh sách dài
2. Cache dashboard data hàng giờ
3. Use prepared statements (param binding)
4. Denormalize cho thống kê (statistics table)
5. Archive old messages

---

## 🔍 Queries Hữu Ích

### Top 10 Users by Message Count

```sql
SELECT user_id, username, COUNT(*) as msg_count
FROM messages m
JOIN users u ON m.user_id = u.user_id
WHERE m.is_deleted = FALSE
GROUP BY user_id
ORDER BY msg_count DESC
LIMIT 10;
```

### Daily Activity Report

```sql
SELECT DATE(created_at) as date,
       COUNT(DISTINCT user_id) as active_users,
       COUNT(*) as total_messages
FROM messages
WHERE is_deleted = FALSE
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

### User Engagement Score

```sql
SELECT u.username,
       COUNT(m.message_id) as messages,
       COUNT(ur.response_id) as responses,
       DATEDIFF(NOW(), MAX(m.created_at)) as days_since_active
FROM users u
LEFT JOIN messages m ON u.user_id = m.user_id
LEFT JOIN user_responses ur ON u.user_id = ur.user_id
GROUP BY u.user_id
ORDER BY messages DESC;
```

---

## 🚀 Triển khai Production

### Server Requirements

- Ubuntu/CentOS
- Java 21+
- MySQL 8.0+
- Nginx (reverse proxy)
- 2GB RAM minimum
- 20GB disk space

### Deployment Checklist

- [ ] Database backup plan
- [ ] SSL certificates (HTTPS)
- [ ] Email configuration
- [ ] Rate limiting rules
- [ ] Firewall rules
- [ ] Monitor & logging
- [ ] Auto-restart services
- [ ] Regular backups

---

## 📞 Support & Troubleshooting

### Vấn đề thường gặp

**1. Database connection error**

```
Solution: Kiểm tra credentials, port MySQL, database name
```

**2. WebSocket connection failed after admin login**

```
Solution: Kiểm tra firewall, port 8080, CORS settings
```

**3. Admin dashboard blank**

```
Solution: Kiểm tra token, browser console logs, API responses
```

**4. Slow queries**

```
Solution: Thêm indexes, optimize queries, check slow query log
```

---

## 📚 Tài liệu Tham Khảo

- `database.sql` - Schema hoàn chỉnh
- `sample_data.sql` - Dữ liệu mẫu
- `ADMIN_SYSTEM_DESIGN.md` - Thiết kế chi tiết
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- MySQL Documentation: https://dev.mysql.com/doc/

---

## 🎉 Kết Luận

Hệ thống Admin này cung cấp:
✅ Quản lý người dùng hoàn chỉnh
✅ Kiểm duyệt nội dung
✅ Thống kê & phân tích
✅ Nhật ký hoạt động
✅ Hệ thống phân quyền

Bạn đã sẵn sàng xây dựng một nền tảng chat chuyên nghiệp! 🚀

---

**Được tạo bởi:** GitHub Copilot
**Ngày:** April 10, 2026
**Version:** 1.0
