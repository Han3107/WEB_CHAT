-- SQL SERVER SETUP SCRIPT
-- Xoa connection va database cu
USE master;
GO

IF EXISTS(SELECT 1 FROM sys.databases WHERE name = 'simplechat')
BEGIN
    ALTER DATABASE simplechat SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE simplechat;
END
GO

CREATE DATABASE simplechat;
GO
USE simplechat;
GO

-- DROP ALL TABLES
IF OBJECT_ID('banned_users', 'U') IS NOT NULL DROP TABLE banned_users;
IF OBJECT_ID('role_permissions', 'U') IS NOT NULL DROP TABLE role_permissions;
IF OBJECT_ID('permissions', 'U') IS NOT NULL DROP TABLE permissions;
IF OBJECT_ID('user_responses', 'U') IS NOT NULL DROP TABLE user_responses;
IF OBJECT_ID('task_options', 'U') IS NOT NULL DROP TABLE task_options;
IF OBJECT_ID('tasks', 'U') IS NOT NULL DROP TABLE tasks;
IF OBJECT_ID('files', 'U') IS NOT NULL DROP TABLE files;
IF OBJECT_ID('messages', 'U') IS NOT NULL DROP TABLE messages;
IF OBJECT_ID('activity_logs', 'U') IS NOT NULL DROP TABLE activity_logs;
IF OBJECT_ID('channel_members', 'U') IS NOT NULL DROP TABLE channel_members;
IF OBJECT_ID('channels', 'U') IS NOT NULL DROP TABLE channels;
IF OBJECT_ID('app_statistics', 'U') IS NOT NULL DROP TABLE app_statistics;
IF OBJECT_ID('system_settings', 'U') IS NOT NULL DROP TABLE system_settings;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;
GO

-- BANG USERS
CREATE TABLE users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user' CHECK (role IN ('admin', 'moderator', 'user')),
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'banned')),
    avatar_url VARCHAR(255),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    last_login DATETIME2 NULL
);
CREATE INDEX idx_role ON users(role);
CREATE INDEX idx_status ON users(status);
GO

-- BANG CHANNELS
CREATE TABLE channels (
    channel_id INT PRIMARY KEY IDENTITY(1,1),
    channel_name VARCHAR(100) NOT NULL,
    description TEXT,
    channel_type VARCHAR(20) DEFAULT 'public' CHECK (channel_type IN ('public', 'private', 'group')),
    created_by INT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    is_active BIT DEFAULT 1,
    member_count INT DEFAULT 0,
    message_count INT DEFAULT 0,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE NO ACTION
);
CREATE INDEX idx_created_by ON channels(created_by);
CREATE INDEX idx_type ON channels(channel_type);
GO

-- BANG CHANNEL_MEMBERS
CREATE TABLE channel_members (
    member_id INT PRIMARY KEY IDENTITY(1,1),
    channel_id INT NOT NULL,
    user_id INT NOT NULL,
    role VARCHAR(20) DEFAULT 'member' CHECK (role IN ('owner', 'moderator', 'member')),
    joined_at DATETIME2 DEFAULT GETDATE(),
    last_read_at DATETIME2 NULL,
    UNIQUE (channel_id, user_id),
    FOREIGN KEY (channel_id) REFERENCES channels(channel_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE INDEX idx_channel ON channel_members(channel_id);
CREATE INDEX idx_user ON channel_members(user_id);
GO

-- BANG MESSAGES
CREATE TABLE messages (
    message_id INT PRIMARY KEY IDENTITY(1,1),
    channel_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) DEFAULT 'text' CHECK (message_type IN ('text', 'image', 'file', 'vote', 'quiz', 'system')),
    created_at DATETIME2 DEFAULT GETDATE(),
    edited_at DATETIME2 NULL,
    is_deleted BIT DEFAULT 0,
    deleted_by INT NULL,
    deleted_at DATETIME2 NULL,
    FOREIGN KEY (channel_id) REFERENCES channels(channel_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE NO ACTION,
    FOREIGN KEY (deleted_by) REFERENCES users(user_id) ON DELETE NO ACTION
);
CREATE INDEX idx_channel_msg ON messages(channel_id);
CREATE INDEX idx_user_msg ON messages(user_id);
CREATE INDEX idx_created_at ON messages(created_at);
GO

-- BANG TASKS
CREATE TABLE tasks (
    task_id INT PRIMARY KEY IDENTITY(1,1),
    channel_id INT NOT NULL,
    message_id INT NOT NULL,
    task_type VARCHAR(20) NOT NULL CHECK (task_type IN ('vote', 'quiz', 'file', 'board')),
    title VARCHAR(255) NOT NULL,
    created_by INT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('pending', 'active', 'closed')),
    FOREIGN KEY (channel_id) REFERENCES channels(channel_id) ON DELETE CASCADE,
    FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE NO ACTION,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE NO ACTION
);
CREATE INDEX idx_channel_task ON tasks(channel_id);
CREATE INDEX idx_task_type ON tasks(task_type);
GO

-- BANG TASK_OPTIONS
CREATE TABLE task_options (
    option_id INT PRIMARY KEY IDENTITY(1,1),
    task_id INT NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    option_order INT,
    vote_count INT DEFAULT 0,
    FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE
);
CREATE INDEX idx_task_opt ON task_options(task_id);
GO

-- BANG USER_RESPONSES
CREATE TABLE user_responses (
    response_id INT PRIMARY KEY IDENTITY(1,1),
    task_id INT NOT NULL,
    user_id INT NOT NULL,
    option_id INT NOT NULL,
    responded_at DATETIME2 DEFAULT GETDATE(),
    UNIQUE (task_id, user_id),

    FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,

    -- ❌ bỏ CASCADE ở đây
    FOREIGN KEY (option_id) REFERENCES task_options(option_id)
);
CREATE INDEX idx_task_resp ON user_responses(task_id);
GO

-- BANG FILES
CREATE TABLE files (
    file_id INT PRIMARY KEY IDENTITY(1,1),
    message_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(50),
    file_path VARCHAR(500),
    uploaded_by INT NOT NULL,
    uploaded_at DATETIME2 DEFAULT GETDATE(),
    download_count INT DEFAULT 0,
    FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id) ON DELETE NO ACTION
);
CREATE INDEX idx_message_file ON files(message_id);
GO

-- BANG ACTIVITY_LOGS
CREATE TABLE activity_logs (
    log_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT,
    action VARCHAR(50),
    entity_type VARCHAR(50),
    entity_id INT,
    description TEXT,
    ip_address VARCHAR(45),
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE NO ACTION
);
CREATE INDEX idx_user_log ON activity_logs(user_id);
CREATE INDEX idx_action_log ON activity_logs(action);
CREATE INDEX idx_created_at_log ON activity_logs(created_at DESC);
GO

-- BANG SYSTEM_SETTINGS
CREATE TABLE system_settings (
    setting_id INT PRIMARY KEY IDENTITY(1,1),
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    setting_type VARCHAR(50) DEFAULT 'string' CHECK (setting_type IN ('string', 'int', 'boolean', 'json')),
    updated_at DATETIME2 DEFAULT GETDATE()
);
GO

-- BANG BANNED_USERS
CREATE TABLE banned_users (
    ban_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    banned_by INT NOT NULL,
    reason TEXT,
    ban_start DATETIME2 DEFAULT GETDATE(),
    ban_end DATETIME2 NULL,
    is_permanent BIT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (banned_by) REFERENCES users(user_id) ON DELETE NO ACTION
);
CREATE INDEX idx_user_ban ON banned_users(user_id);
CREATE INDEX idx_ban_end ON banned_users(ban_end);
GO

-- BANG PERMISSIONS
CREATE TABLE permissions (
    permission_id INT PRIMARY KEY IDENTITY(1,1),
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);
GO

-- BANG ROLE_PERMISSIONS
CREATE TABLE role_permissions (
    role_permission_id INT PRIMARY KEY IDENTITY(1,1),
    role VARCHAR(50) NOT NULL,
    permission_id INT NOT NULL,
    UNIQUE (role, permission_id),
    FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
);
GO

-- BANG APP_STATISTICS
CREATE TABLE app_statistics (
    stat_id INT PRIMARY KEY IDENTITY(1,1),
    stat_date DATE DEFAULT CAST(GETDATE() AS DATE),
    total_users INT DEFAULT 0,
    active_users INT DEFAULT 0,
    total_channels INT DEFAULT 0,
    total_messages INT DEFAULT 0,
    total_votes INT DEFAULT 0,
    total_quiz INT DEFAULT 0,
    active_sessions INT DEFAULT 0,
    UNIQUE (stat_date)
);
GO

-- ==== INSERT DEMO DATA ====

-- INSERT PERMISSIONS
INSERT INTO permissions (permission_name, description) VALUES
('view_all_messages', 'Xem tat ca tin nhan'),
('delete_message', 'Xoa tin nhan'),
('manage_users', 'Quan ly nguoi dung'),
('manage_channels', 'Quan ly kenh'),
('view_statistics', 'Xem thong ke'),
('ban_user', 'Cam trai nguoi dung'),
('moderate_content', 'Kiem duyet noi dung'),
('manage_system', 'Quan ly he thong');
GO

-- INSERT DEMO USERS (with BCrypt hashed passwords)
-- admin/admin123, user1-5/user123, mod1/mod123
INSERT INTO users (username, email, password_hash, full_name, role, status) VALUES
('admin', 'admin@quay.com', '$2a$10$/uqdirNZ4HEaXCm6SpkADeo5DkSabwdXJJgby3eBz2LQum8eDyIDC', 'Quan tri he thong', 'admin', 'active'),
('user1', 'user1@quay.com', '$2a$10$fL.n6KzFMc5GIeKzwOMamu5OMLY4XfrsKEYOX8tiw.A/xDrDRalVq', 'Nguyen Van A', 'user', 'active'),
('user2', 'user2@quay.com', '$2a$10$fL.n6KzFMc5GIeKzwOMamu5OMLY4XfrsKEYOX8tiw.A/xDrDRalVq', 'Tran Thi B', 'user', 'active'),
('mod1', 'mod1@quay.com', '$2a$10$JGw5CeGwxtpBUqJkZhiuxeLn9EN/7kT1hsRemrHMsX6PipHM85PsC', 'Nguyen Van Moderator', 'moderator', 'active'),
('user3', 'user3@quay.com', '$2a$10$fL.n6KzFMc5GIeKzwOMamu5OMLY4XfrsKEYOX8tiw.A/xDrDRalVq', 'Pham Minh C', 'user', 'active'),
('user4', 'user4@quay.com', '$2a$10$fL.n6KzFMc5GIeKzwOMamu5OMLY4XfrsKEYOX8tiw.A/xDrDRalVq', 'Hoang Duc D', 'user', 'active'),
('user5', 'user5@quay.com', '$2a$10$fL.n6KzFMc5GIeKzwOMamu5OMLY4XfrsKEYOX8tiw.A/xDrDRalVq', 'Vo Hong E', 'user', 'active');
GO



-- INSERT ADMIN ROLE PERMISSIONS
INSERT INTO role_permissions (role, permission_id) 
SELECT 'admin', permission_id FROM permissions;
GO

-- INSERT MODERATOR PERMISSIONS
INSERT INTO role_permissions (role, permission_id) VALUES
('moderator', 3), ('moderator', 4), ('moderator', 7), ('moderator', 8);
GO

-- INSERT USER PERMISSIONS
INSERT INTO role_permissions (role, permission_id) VALUES
('user', 1), ('user', 2);
GO

-- INSERT DEMO CHANNELS
INSERT INTO channels (channel_name, description, channel_type, created_by) VALUES
('general', 'Kenh chung cho tat ca', 'public', 1),
('random', 'Chat tu do', 'public', 1),
('tech', 'Thao luan technology', 'group', 1),
('work', 'Cong viec', 'private', 1),
('admin', 'Kenh rieng admin', 'private', 1);
GO

-- INSERT CHANNEL MEMBERS
INSERT INTO channel_members (channel_id, user_id, role) VALUES
(1, 1, 'owner'), (1, 2, 'member'), (1, 3, 'member'), (1, 4, 'moderator'), (1, 5, 'member'),
(2, 1, 'owner'), (2, 2, 'member'), (2, 3, 'member'), (2, 4, 'member'), (2, 5, 'member'), (2, 6, 'member'), (2, 7, 'member'),
(3, 1, 'owner'), (3, 4, 'moderator'), (3, 2, 'member'), (3, 5, 'member'),
(4, 1, 'owner'), (4, 2, 'member'),
(5, 1, 'owner'), (5, 4, 'member');
GO

-- INSERT DEMO MESSAGES
INSERT INTO messages (channel_id, user_id, content, message_type) VALUES
(1, 1, 'Xin chao cac ban, day la kenh general', 'text'),
(1, 2, 'Hello everyone! Rat vui duoc o day', 'text'),
(1, 3, 'Chao ban, website nay sao the?', 'text'),
(1, 4, 'Toi la moderator, can giup gi khong?', 'text'),
(2, 5, 'Chat the nay vui roi', 'text'),
(2, 6, 'Chac sach :D', 'text'),
(3, 2, 'Technology la gi?', 'text'),
(3, 4, 'Technology la ky thuat cong nghe', 'text'),
(4, 1, 'Day la kenh cong viec', 'text'),
(4, 2, 'Chung ta can lam gi hom nay?', 'text');
GO

-- INSERT DEMO TASKS (Vote)
INSERT INTO tasks (channel_id, message_id, task_type, title, created_by) VALUES
(1, 1, 'vote', 'Chon loai database: SQL Server hay MySQL?', 1),
(2, 5, 'vote', 'Chon framework: React hay Vue?', 1);
GO

-- INSERT TASK OPTIONS
INSERT INTO task_options (task_id, option_text, option_order) VALUES
(1, 'SQL Server', 1), (1, 'MySQL', 2), (1, 'PostgreSQL', 3),
(2, 'React', 1), (2, 'Vue.js', 2), (2, 'Angular', 3);
GO

-- INSERT USER RESPONSES
INSERT INTO user_responses (task_id, user_id, option_id) VALUES
(1, 1, 1), (1, 2, 1), (1, 3, 2), (1, 4, 1), (1, 5, 3),
(2, 5, 4), (2, 6, 5), (2, 7, 4);
GO

-- INSERT ACTIVITY LOGS
INSERT INTO activity_logs (user_id, action, entity_type, entity_id, description, ip_address) VALUES
(1, 'LOGIN', 'users', 1, 'Admin dang nhap', '127.0.0.1'),
(2, 'LOGIN', 'users', 2, 'User1 dang nhap', '127.0.0.1'),
(1, 'CREATE_CHANNEL', 'channels', 1, 'Tao kenh general', '127.0.0.1'),
(1, 'CREATE_MESSAGE', 'messages', 1, 'Gui tin nhan', '127.0.0.1'),
(2, 'CREATE_VOTE', 'tasks', 1, 'Tao phieu binh chon', '127.0.0.1');
GO

-- INSERT SYSTEM SETTINGS
INSERT INTO system_settings (setting_key, setting_value, setting_type) VALUES
('app_name', 'Quan ly Chat', 'string'),
('app_version', '1.0.0', 'string'),
('max_users', '1000', 'int'),
('allow_registration', 'true', 'boolean'),
('maintenance_mode', 'false', 'boolean');
GO

-- INSERT STATISTICS
INSERT INTO app_statistics (stat_date, total_users, active_users, total_channels, total_messages, total_votes, total_quiz) VALUES
(CAST(GETDATE() AS DATE), 7, 5, 5, 10, 2, 0);
GO

PRINT '======================================='
PRINT '✅ DATABASE SETUP COMPLETED SUCCESSFULLY!'
PRINT '======================================='
PRINT 'Tong so bang: 14'
PRINT 'Demo users: admin, user1, user2, mod1, user3, user4, user5'
PRINT 'Demo channels: general, random, tech, work, admin'
PRINT '======================================='
GO

--select *from users