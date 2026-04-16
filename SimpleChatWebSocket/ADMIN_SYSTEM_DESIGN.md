# Simple Chat - Hệ Thống Admin & Khách

## 📋 Tổng Quan Kiến Trúc

### Cơ Sở Dữ Liệu (14 Bảng Chính)

```
┌─────────────────────────────────────────────┐
│          SIMPLE CHAT DATABASE               │
├─────────────────────────────────────────────┤
│
│  ┌──────────────────────────────────┐
│  │  Users (Người dùng)              │ - Quản lý tất cả người dùng
│  │  ├─ user_id, username, role     │ - Admin, Moderator, User
│  │  └─ authentication              │
│  └──────────────────────────────────┘
│            ↓
│  ┌──────────────────────────────────┐
│  │  Channels (Phòng/Nhóm chat)      │ - Public, Private, Group
│  │  ├─ channel_id, name, type      │ - Tạo bởi người nào
│  │  └─ member_count, msg_count     │ - Thống kê thành viên/tin nhắn
│  └──────────────────────────────────┘
│            ↓
│  ┌──────────────────────────────────┐
│  │  Channel_Members (Thành viên)    │ - Ai là owner, moderator, member
│  │  └─ joined_at, last_read_at      │
│  └──────────────────────────────────┘
│            ↓
│  ┌──────────────────────────────────┐
│  │  Messages (Tin nhắn)             │ - Text, Image, File, Vote, Quiz
│  │  ├─ message_id, content          │ - Ai gửi, lúc nào
│  │  ├─ message_type                 │ - Có bị xóa không
│  │  └─ is_deleted, deleted_by       │
│  └──────────────────────────────────┘
│            ↓
│  ┌──────────────────────────────────┐
│  │  Tasks (Vote/Quiz/File/Board)    │ - Các tác vụ tương tác
│  │  ├─ task_id, task_type           │
│  │  ├─ title, description           │
│  │  └─ status (pending/active/...)  │
│  └──────────────────────────────────┘
│            ↓
│  ┌──────────────────────────────────┐
│  │  Activity_Logs (Nhật ký)         │ - Ghi lại mọi hành động
│  │  ├─ action (create, update, del) │ - Ai làm, lúc nào
│  │  └─ IP address                   │
│  └──────────────────────────────────┘
│
│  ┌──────────────────────────────────┐
│  │  Statistics (Thống kê)           │ - Dữ liệu tổng hợp hàng ngày
│  │  └─ daily reports                │
│  └──────────────────────────────────┘
│
└─────────────────────────────────────────────┘
```

---

## 👨‍💼 Trang Admin - Quản Lý Hệ Thống

### URL: `/admin/dashboard`

### 1. **Dashboard Tổng Quan**

```
┌─────────────────────────────────────────────┐
│        📊 ADMIN DASHBOARD - TỔNG HỢP        │
├─────────────────────────────────────────────┤
│                                             │
│  ┌─────────────┬────────────┬──────────┐   │
│  │ 👥 Users    │ 💬 Channels│ 💌 Msgs  │   │
│  │ 1,234       │ 45         │ 25,678   │   │
│  └─────────────┴────────────┴──────────┘   │
│                                             │
│  ┌─────────────┬────────────┬──────────┐   │
│  │ 🗳️ Vote      │ 🧠 Quiz    │ 📎 Files │   │
│  │ 234         │ 156        │ 789      │   │
│  └─────────────┴────────────┴──────────┘   │
│                                             │
│  📈 Biểu đồ hoạt động 7 ngày               │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│  [Graph visualization here]                 │
│                                             │
└─────────────────────────────────────────────┘
```

**Thông tin hiển thị:**

- Tổng số người dùng (active/inactive/banned)
- Tổng số kênh (public/private/group)
- Tổng tin nhắn
- Tổng vote, quiz, file
- Biểu đồ hoạt động hàng ngày
- Danh sách hoạt động gần đây

---

### 2. **Quản Lý Người Dùng** - `/admin/users`

```
┌─────────────────────────────────────────────────────┐
│      👥 QUẢN LÝ NGƯỜI DÙNG                          │
├─────────────────────────────────────────────────────┤
│                                                     │
│  🔍 Tìm kiếm: [____________] [Lọc] [Làm mới]      │
│                                                     │
│  ┌─────┬──────────┬──────────┬─────────────┐       │
│  │ ID  │ Username │ Role     │ Status      │ Action│
│  ├─────┼──────────┼──────────┼─────────────┼───────┤
│  │ 1   │ admin    │ Admin    │ Active   ✓ │ ✎ 🗑️ │
│  │ 2   │ user1    │ User     │ Active   ✓ │ ✎ 🗑️ │
│  │ 3   │ banned   │ User     │ Banned   ✗ │ ✎ 🗑️ │
│  └─────┴──────────┴──────────┴─────────────┴───────┘
│
│  [➕ Tạo người dùng mới] [⬇️ Export CSV] [⬆️ Import]
│
└─────────────────────────────────────────────────────┘

Chức năng:
✓ Tìm kiếm, lọc theo role, status
✓ Tạo tài khoản mới
✓ Edit user (username, email, role, status)
✓ Ban/Unban user
✓ Xem lịch sử hoạt động
✓ Reset password
✓ Export/Import dữ liệu
```

**Thông tin người dùng chi tiết:**

- User ID, Username, Email
- Full Name, Avatar
- Role (Admin, Moderator, User)
- Status (Active, Inactive, Banned)
- Số tin nhắn gửi
- Channels tham gia
- Last login
- Created date
- Activity history

---

### 3. **Quản Lý Kênh/Nhóm** - `/admin/channels`

```
┌─────────────────────────────────────────────────────┐
│     💬 QUẢN LÝ PHÒNG CHAT / NHÓM                    │
├─────────────────────────────────────────────────────┤
│                                                     │
│  🔍 Tìm kiếm: [____________] [Lọc] [Làm mới]      │
│                                                     │
│  ┌────────────┬────────┬──────────┬──────────┐     │
│  │ Channel    │ Type   │ Members  │ Messages │Action│
│  ├────────────┼────────┼──────────┼──────────┼─────┤
│  │ # Chung    │ Public │ 45       │ 5,623    │ ✎🗑️ │
│  │ # Nhóm A   │ Private│ 12       │ 1,234    │ ✎🗑️ │
│  │ # Project1 │ Group  │ 8        │ 892      │ ✎🗑️ │
│  └────────────┴────────┴──────────┴──────────┴─────┘
│
│  [➕ Tạo kênh mới] [👥 Xem thành viên]
│
└─────────────────────────────────────────────────────┘

Chức năng:
✓ Tạo kênh mới (public/private/group)
✓ Xem danh sách thành viên
✓ Quản lý thành viên (thêm/xóa/quyền)
✓ Xem tin nhắn trong kênh
✓ Xóa kênh
✓ Thống kê kênh (member growth, message volume)
```

**Thông tin chi tiết kênh:**

- Channel ID, Name, Description
- Type (Public/Private/Group)
- Owner, Created date
- Total members, Total messages
- Growth chart
- Member list with roles

---

### 4. **Kiểm Duyệt Nội Dung** - `/admin/moderation`

```
┌─────────────────────────────────────────────────────┐
│    🔍 KIỂM DUYỆT NỘI DUNG / TIN NHẮN              │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Filter: [Tất cả] [Đơi cờ] [Báo cáo] [Xóa]       │
│                                                     │
│  ┌──────────────────────────────────────────┐      │
│  │ User: john | Channel: #chung             │      │
│  │ 2 giờ trước                              │      │
│  │                                          │      │
│  │ "This is inappropriate content..."       │      │
│  │                                          │      │
│  │ 👍 5 | 👎 12 | 🚩 3 reports             │      │
│  │                                          │      │
│  │ [ Duyệt OK ] [ Xóa ] [ Ban user ] [ ❌ ]│      │
│  └──────────────────────────────────────────┘      │
│                                                     │
│  ┌──────────────────────────────────────────┐      │
│  │ ...                                      │      │
│  └──────────────────────────────────────────┘      │
│
└─────────────────────────────────────────────────────┘

Chức năng:
✓ Xem tin nhắn bị đánh dấu/báo cáo
✓ Xem nội dung bị report
✓ Duyệt/Xóa tin nhắn
✓ Ban/Suspend user
✓ Ghi log hành động
```

---

### 5. **Thống Kê Dữ Liệu** - `/admin/analytics`

```
┌─────────────────────────────────────────────────────┐
│     📊 THỐNG KÊ & PHÂN TÍCH DỮ LIỆU                │
├─────────────────────────────────────────────────────┤
│                                                     │
│  📈 BIỂU ĐỒ HOẠT ĐỘNG                              │
│  • Messages per day (7 days)                       │
│  • Active users (7 days)                           │
│  • Vote/Quiz participation                         │
│  • File uploads                                    │
│                                                     │
│  📋 THỐNG KÊ CHI TIẾT                              │
│  ┌──────────────────────────────────────┐         │
│  │ Tổng người dùng: 1,234               │         │
│  │ • Active: 890 (72%)                  │         │
│  │ • Inactive: 244 (20%)                │         │
│  │ • Banned: 100 (8%)                   │         │
│  │                                      │         │
│  │ Tổng kênh: 45                        │         │
│  │ • Public: 25                         │         │
│  │ • Private: 15                        │         │
│  │ • Group: 5                           │         │
│  │                                      │         │
│  │ Tổng tin nhắn: 25,678                │         │
│  │ • Hôm nay: 234                       │         │
│  │ • Tuần này: 1,890                    │         │
│  │ • Tháng này: 7,456                   │         │
│  │                                      │         │
│  │ Vote: 234 | Quiz: 156 | File: 789    │         │
│  │                                      │         │
│  │ Avg messages per user: 21            │         │
│  │ Avg users per channel: 27            │         │
│  └──────────────────────────────────────┘         │
│                                                     │
│  [📥 Export PDF] [📊 Excel] [🖨️ In ra]             │
│
└─────────────────────────────────────────────────────┘
```

---

### 6. **Nhật Ký Hoạt Động** - `/admin/logs`

```
┌─────────────────────────────────────────────────────┐
│     📋 NHẬT KÝ HOẠT ĐỘNG HỆ THỐNG                 │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Filter: [Tất cả] [User] [Channel] [Message] [Ban]│
│  Date: [From] ──────── [To] [Filter]              │
│                                                     │
│  ┌──────────────────────────────────────────┐      │
│  │ 2:45 PM | admin | DELETE | User #5      │      │
│  │         | Reason: Spam                   │      │
│  │                                          │      │
│  │ 2:30 PM | john  | SEND_MSG | Channel #1 │      │
│  │         | "Hello everyone!"              │      │
│  │                                          │      │
│  │ 2:15 PM | moderator | BAN | User #12    │      │
│  │         | Duration: 7 days | Reason: ...│      │
│  │                                          │      │
│  │ 2:00 PM | admin | CREATE | Channel #5   │      │
│  │         | "New Project Room"             │      │
│  │                                          │      │
│  └──────────────────────────────────────────┘      │
│
│  [📥 Export] [🔄 Refresh]
│
└─────────────────────────────────────────────────────┘

Actions ghi lại:
- USER: create, update, delete, login, logout, ban, unban
- CHANNEL: create, update, delete, add_member, remove_member
- MESSAGE: create, update, delete
- VOTE/QUIZ: create, respond
- FILE: upload, download
- SYSTEM: settings_change, backup
```

---

### 7. **Cài Đặt Hệ Thống** - `/admin/settings`

```
┌─────────────────────────────────────────────────────┐
│     ⚙️ CÀI ĐẶT HỆ THỐNG                            │
├─────────────────────────────────────────────────────┤
│                                                     │
│  🔧 CẤU HÌNH CHUNG                                  │
│  ┌────────────────────────────────────────┐        │
│  │ Site Name: [Simple Chat Web]           │        │
│  │ Server Port: [8080]                    │        │
│  │ Max users per channel: [500]           │        │
│  │ Message retention (days): [365]        │        │
│  │ File upload limit (MB): [100]          │        │
│  └────────────────────────────────────────┘        │
│                                                     │
│  🔐 BẢO MẬT                                        │
│  ┌────────────────────────────────────────┐        │
│  │ ☑️ Enable SSL                          │        │
│  │ ☑️ Require email verification          │        │
│  │ ☑️ Enable user reporting               │        │
│  │ ☑️ Auto-ban on 5 reports               │        │
│  │ Session timeout (min): [30]            │        │
│  └────────────────────────────────────────┘        │
│                                                     │
│  📧 EMAIL                                          │
│  ┌────────────────────────────────────────┐        │
│  │ SMTP Server: [smtp.gmail.com]          │        │
│  │ SMTP Port: [587]                       │        │
│  │ Email: [admin@simplechat.com]          │        │
│  │ Password: [••••••••]                   │        │
│  └────────────────────────────────────────┘        │
│                                                     │
│  [ 💾 Lưu ] [ 🔄 Reset ]
│
└─────────────────────────────────────────────────────┘
```

---

## 👤 Trang Khách (User Profile) - `/profile`

### 1. **Hồ Sơ Cá Nhân**

```
┌─────────────────────────────────────────────────────┐
│      👤 HỒ SƠ CÁ NHÂN CỦA TÔI                      │
├─────────────────────────────────────────────────────┤
│                                                     │
│  [Avatar 🖼️]                                        │
│  Username: john_doe                                 │
│  Email: john@email.com                              │
│  Full Name: John Doe                                │
│  Joined: Jan 15, 2024                               │
│                                                     │
│  ✏️ Chỉnh sửa hồ sơ                                 │
│  🔐 Đổi mật khẩu                                   │
│  🔔 Thông báo                                       │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### 2. **Thống Kê Cá Nhân**

```
┌─────────────────────────────────────────────────────┐
│      📊 THỐNG KÊ CÁ NHÂN CỦA TÔI                   │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌─────────────┬───────────┬──────────┐            │
│  │ Tin nhắn    │ Channels  │ Votes    │            │
│  │ 234         │ 12        │ 45       │            │
│  └─────────────┴───────────┴──────────┘            │
│                                                     │
│  ┌──────────────────────────┐                      │
│  │ Hoạt động 7 ngày         │                      │
│  │ [Graph]                  │                      │
│  └──────────────────────────┘                      │
│                                                     │
│  Channels của tôi:                                  │
│  • #Chung (12 thành viên)                           │
│  • #Nhóm A (8 thành viên)                           │
│  • #Project1 (5 thành viên)                         │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### 3. **Tin Nhắn Được Lưu**

```
┌─────────────────────────────────────────────────────┐
│      ⭐ TIN NHẮN ĐƯỢC LƯU CỦA TÔI                  │
├─────────────────────────────────────────────────────┤
│                                                     │
│  [💬] "Important announcement..." - #Chung         │
│       Saved at 10:30 AM                             │
│       Original from: admin                          │
│                                                     │
│  [📎] "Project_v2.pdf" - #Project1                 │
│       Saved at 2:15 PM                              │
│                                                     │
│  [🗳️] "Best programming language?" - #Dev         │
│       Saved at 5:45 PM                              │
│                                                     │
│  [ 🗑️ Xóa ] [ 📝 Thêm ghi chú ]
│
└─────────────────────────────────────────────────────┘
```

---

## 🔑 Roles & Permissions (Quyền Hạn)

### Admin (Quản Trị Viên)

```
✅ Xem tất cả tin nhắn
✅ Xóa tin nhắn bất kỳ
✅ Quản lý người dùng (create/edit/delete/ban)
✅ Quản lý kênh (create/delete)
✅ Quản lý thành viên kênh
✅ Xem thống kê toàn hệ thống
✅ Kiểm duyệt nội dung
✅ Xem nhật ký hoạt động
✅ Cài đặt hệ thống
✅ Đặt lại mật khẩu người dùng
```

### Moderator (Kiểm Duyệt Viên)

```
✅ Xem tin nhắn trong kênh được chỉ định
✅ Xóa tin nhắn không phù hợp
✅ Ban người dùng (giới hạn)
✅ Xem thống kê kênh
✅ Quản lý thành viên kênh
✅ Kiểm duyệt nội dung
❌ Quản lý người dùng
❌ Xem cài đặt hệ thống
```

### User (Người Dùng)

```
✅ Gửi/xem tin nhắn trong kênh tham gia
✅ Tạo kênh riêng
✅ Tham gia vote/quiz
✅ Tải lên/tải xuống file
✅ Xem hồ sơ cá nhân
✅ Xem thống kê cá nhân
❌ Quản lý người dùng khác
❌ Xóa kênh
❌ Ban người dùng
```

---

## 📈 Công Thức Thống Kê

### Dashboard Metrics

```sql
-- Tổng người dùng
SELECT COUNT(*) FROM users WHERE status = 'active'

-- Tổng kênh
SELECT COUNT(*) FROM channels WHERE is_active = TRUE

-- Tổng tin nhắn
SELECT COUNT(*) FROM messages WHERE is_deleted = FALSE

-- Người dùng online
SELECT COUNT(DISTINCT user_id) FROM sessions WHERE is_active = TRUE

-- Tin nhắn hôm nay
SELECT COUNT(*) FROM messages
WHERE DATE(created_at) = CURDATE()

-- Tỷ lệ hoạt động
(Active Users / Total Users) * 100
```

### User Analytics

```sql
-- Top 10 users by message count
SELECT user_id, username, COUNT(*) as msg_count
FROM messages m
JOIN users u ON m.user_id = u.user_id
GROUP BY user_id
ORDER BY msg_count DESC
LIMIT 10

-- Channel growth
SELECT DATE(created_at) as join_date, COUNT(*) as new_users
FROM users
GROUP BY DATE(created_at)

-- Vote participation rate
(Users who voted / Total users) * 100
```

---

## 🚀 Hientes Triển Khai

### Phase 1: Database & Backend

1. ✅ Tạo schema SQL
2. ⏳ Tạo Java DAOs (User, Channel, Message, etc.)
3. ⏳ Tạo REST endpoints cho admin

### Phase 2: Admin Frontend

1. ⏳ Login admin page
2. ⏳ Dashboard
3. ⏳ User Management
4. ⏳ Channel Management
5. ⏳ Moderation Panel
6. ⏳ Analytics
7. ⏳ Settings

### Phase 3: User Features

1. ⏳ User Profile page
2. ⏳ Personal statistics
3. ⏳ Saved messages

### Phase 4: Testing & Deployment

1. ⏳ Unit tests
2. ⏳ Integration tests
3. ⏳ Performance tuning
4. ⏳ Deployment

---

## 💾 Database Connection (Java)

```java
// application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/simplechat
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

// Connection pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

---

## 🔐 Security Best Practices

1. **Password Hashing**: Use BCrypt

   ```java
   hash = BCrypt.hashpw(password, BCrypt.gensalt())
   ```

2. **Authentication**: JWT Tokens

   ```java
   Token = Base64(header.payload.signature)
   ```

3. **Authorization**: Role-based access control (RBAC)

4. **SQL Injection Protection**: Use PreparedStatements

5. **XSS Protection**: HTML escape user input

6. **CSRF Protection**: Add CSRF tokens

7. **Rate Limiting**: Prevent spam/brute force

---

## 📞 Thao Tác Quản Trị Phổ Biến

### 1. Ban người dùng spam

```sql
UPDATE users SET status = 'banned'
WHERE user_id = 5;

INSERT INTO banned_users
(user_id, banned_by, reason, is_permanent)
VALUES (5, 1, 'Spam', TRUE);

INSERT INTO activity_logs
(user_id, action, entity_type, entity_id, description)
VALUES (1, 'BAN_USER', 'user', 5, 'Banned for spam');
```

### 2. Xóa tin nhắn không phù hợp

```sql
UPDATE messages
SET is_deleted = TRUE, deleted_by = 1, deleted_at = NOW()
WHERE message_id = 42;
```

### 3. Thêm moderator cho kênh

```sql
UPDATE channel_members
SET role = 'moderator'
WHERE channel_id = 3 AND user_id = 7;
```

### 4. Tạo channel mới

```sql
INSERT INTO channels
(channel_name, description, created_by, channel_type)
VALUES ('Project X', 'Nhóm làm việc Project X', 1, 'private');
```

---

## 📊 Queries Hữu Ích

```sql
-- Top channels by activity
SELECT c.channel_id, c.channel_name,
       COUNT(DISTINCT cm.user_id) as members,
       COUNT(m.message_id) as total_messages,
       MAX(m.created_at) as last_message
FROM channels c
JOIN channel_members cm ON c.channel_id = cm.channel_id
LEFT JOIN messages m ON c.channel_id = m.channel_id
GROUP BY c.channel_id
ORDER BY total_messages DESC;

-- User engagement score
SELECT u.user_id, u.username,
       COUNT(m.message_id) as messages_sent,
       COUNT(DISTINCT t.task_id) as votes_responses,
       ROUND(COUNT(m.message_id) /
             DATEDIFF(NOW(), u.created_at), 2) as msg_per_day
FROM users u
LEFT JOIN messages m ON u.user_id = m.user_id
LEFT JOIN user_responses ur ON u.user_id = ur.user_id
LEFT JOIN tasks t ON ur.task_id = t.task_id
GROUP BY u.user_id
ORDER BY messages_sent DESC;

-- Daily statistics
SELECT DATE(created_at) as date,
       COUNT(DISTINCT user_id) as active_users,
       COUNT(*) as total_messages,
       COUNT(DISTINCT channel_id) as active_channels
FROM messages
WHERE is_deleted = FALSE
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

---

## 🎯 Kết Luận

Hệ thống này cung cấp:

- ✅ **14 bảng database** với relationships rõ ràng
- ✅ **3 loại roles** với quyền hạn cụ thể
- ✅ **Admin Dashboard** toàn diện
- ✅ **Audit Logging** đầy đủ
- ✅ **Analytics & Statistics** chi tiết
- ✅ **User Profiles & Settings**
- ✅ **Moderation Tools** hiệu quả

Bạn đã sẵn sàng để xây dựng một nền tảng chat chuyên nghiệp! 🚀
