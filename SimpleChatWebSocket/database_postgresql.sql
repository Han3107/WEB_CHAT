-- POSTGRESQL SETUP SCRIPT FOR RAILWAY

-- DROP ALL TABLES (Order matters due to FK)
DROP TABLE IF EXISTS banned_users;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS user_responses;
DROP TABLE IF EXISTS task_options;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS files;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS activity_logs;
DROP TABLE IF EXISTS channel_members;
DROP TABLE IF EXISTS channels;
DROP TABLE IF EXISTS app_statistics;
DROP TABLE IF EXISTS system_settings;
DROP TABLE IF EXISTS users;

-- BANG USERS
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user' CHECK (role IN ('admin', 'moderator', 'user')),
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'banned')),
    avatar_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);
CREATE INDEX idx_role ON users(role);
CREATE INDEX idx_status ON users(status);

-- BANG CHANNELS
CREATE TABLE channels (
    channel_id SERIAL PRIMARY KEY,
    channel_name VARCHAR(100) NOT NULL,
    description TEXT,
    channel_type VARCHAR(20) DEFAULT 'public' CHECK (channel_type IN ('public', 'private', 'group')),
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    member_count INT DEFAULT 0,
    message_count INT DEFAULT 0,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);
CREATE INDEX idx_created_by ON channels(created_by);
CREATE INDEX idx_type ON channels(channel_type);

-- BANG CHANNEL_MEMBERS
CREATE TABLE channel_members (
    member_id SERIAL PRIMARY KEY,
    channel_id INT NOT NULL,
    user_id INT NOT NULL,
    role VARCHAR(20) DEFAULT 'member' CHECK (role IN ('owner', 'moderator', 'member')),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_read_at TIMESTAMP NULL,
    UNIQUE (channel_id, user_id),
    FOREIGN KEY (channel_id) REFERENCES channels(channel_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE INDEX idx_channel ON channel_members(channel_id);
CREATE INDEX idx_user ON channel_members(user_id);

-- BANG MESSAGES
CREATE TABLE messages (
    message_id SERIAL PRIMARY KEY,
    channel_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) DEFAULT 'text' CHECK (message_type IN ('text', 'image', 'file', 'vote', 'quiz', 'system')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    edited_at TIMESTAMP NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_by INT NULL,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (channel_id) REFERENCES channels(channel_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (deleted_by) REFERENCES users(user_id)
);
CREATE INDEX idx_channel_msg ON messages(channel_id);
CREATE INDEX idx_user_msg ON messages(user_id);
CREATE INDEX idx_created_at ON messages(created_at);

-- BANG TASKS
CREATE TABLE tasks (
    task_id SERIAL PRIMARY KEY,
    channel_id INT NOT NULL,
    message_id INT NOT NULL,
    task_type VARCHAR(20) NOT NULL CHECK (task_type IN ('vote', 'quiz', 'file', 'board')),
    title VARCHAR(255) NOT NULL,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('pending', 'active', 'closed')),
    FOREIGN KEY (channel_id) REFERENCES channels(channel_id) ON DELETE CASCADE,
    FOREIGN KEY (message_id) REFERENCES messages(message_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);
CREATE INDEX idx_channel_task ON tasks(channel_id);
CREATE INDEX idx_task_type ON tasks(task_type);

-- BANG TASK_OPTIONS
CREATE TABLE task_options (
    option_id SERIAL PRIMARY KEY,
    task_id INT NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    option_order INT,
    vote_count INT DEFAULT 0,
    FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE
);
CREATE INDEX idx_task_opt ON task_options(task_id);

-- BANG USER_RESPONSES
CREATE TABLE user_responses (
    response_id SERIAL PRIMARY KEY,
    task_id INT NOT NULL,
    user_id INT NOT NULL,
    option_id INT NOT NULL,
    responded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (task_id, user_id),
    FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES task_options(option_id)
);
CREATE INDEX idx_task_resp ON user_responses(task_id);

-- BANG FILES
CREATE TABLE files (
    file_id SERIAL PRIMARY KEY,
    message_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(50),
    file_path VARCHAR(500),
    uploaded_by INT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    download_count INT DEFAULT 0,
    FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id)
);
CREATE INDEX idx_message_file ON files(message_id);

-- BANG ACTIVITY_LOGS
CREATE TABLE activity_logs (
    log_id SERIAL PRIMARY KEY,
    user_id INT,
    action VARCHAR(50),
    entity_type VARCHAR(50),
    entity_id INT,
    description TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
CREATE INDEX idx_user_log ON activity_logs(user_id);
CREATE INDEX idx_action_log ON activity_logs(action);
CREATE INDEX idx_created_at_log ON activity_logs(created_at DESC);

-- BANG SYSTEM_SETTINGS
CREATE TABLE system_settings (
    setting_id SERIAL PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    setting_type VARCHAR(50) DEFAULT 'string' CHECK (setting_type IN ('string', 'int', 'boolean', 'json')),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- BANG BANNED_USERS
CREATE TABLE banned_users (
    ban_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    banned_by INT NOT NULL,
    reason TEXT,
    ban_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ban_end TIMESTAMP NULL,
    is_permanent BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (banned_by) REFERENCES users(user_id)
);
CREATE INDEX idx_user_ban ON banned_users(user_id);
CREATE INDEX idx_ban_end ON banned_users(ban_end);

-- BANG PERMISSIONS
CREATE TABLE permissions (
    permission_id SERIAL PRIMARY KEY,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- BANG ROLE_PERMISSIONS
CREATE TABLE role_permissions (
    role_permission_id SERIAL PRIMARY KEY,
    role VARCHAR(50) NOT NULL,
    permission_id INT NOT NULL,
    UNIQUE (role, permission_id),
    FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
);

-- BANG APP_STATISTICS
CREATE TABLE app_statistics (
    stat_id SERIAL PRIMARY KEY,
    stat_date DATE DEFAULT CURRENT_DATE,
    total_users INT DEFAULT 0,
    active_users INT DEFAULT 0,
    total_channels INT DEFAULT 0,
    total_messages INT DEFAULT 0,
    total_votes INT DEFAULT 0,
    total_quiz INT DEFAULT 0,
    active_sessions INT DEFAULT 0,
    UNIQUE (stat_date)
);

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

-- INSERT ADMIN ROLE PERMISSIONS
INSERT INTO role_permissions (role, permission_id) 
SELECT 'admin', permission_id FROM permissions;

-- INSERT MODERATOR PERMISSIONS
INSERT INTO role_permissions (role, permission_id) VALUES
('moderator', 3), ('moderator', 4), ('moderator', 7), ('moderator', 8);

-- INSERT USER PERMISSIONS
INSERT INTO role_permissions (role, permission_id) VALUES
('user', 1), ('user', 2);

-- INSERT DEMO CHANNELS
INSERT INTO channels (channel_name, description, channel_type, created_by) VALUES
('general', 'Kenh chung cho tat ca', 'public', 1),
('random', 'Chat tu do', 'public', 1),
('tech', 'Thao luan technology', 'group', 1),
('work', 'Cong viec', 'private', 1),
('admin', 'Kenh rieng admin', 'private', 1);

-- INSERT CHANNEL MEMBERS
INSERT INTO channel_members (channel_id, user_id, role) VALUES
(1, 1, 'owner'), (1, 2, 'member'), (1, 3, 'member'), (1, 4, 'moderator'), (1, 5, 'member'),
(2, 1, 'owner'), (2, 2, 'member'), (2, 3, 'member'), (2, 4, 'member'), (2, 5, 'member'), (2, 6, 'member'), (2, 7, 'member'),
(3, 1, 'owner'), (3, 4, 'moderator'), (3, 2, 'member'), (3, 5, 'member'),
(4, 1, 'owner'), (4, 2, 'member'),
(5, 1, 'owner'), (5, 4, 'member');

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

-- INSERT DEMO TASKS (Vote)
INSERT INTO tasks (channel_id, message_id, task_type, title, created_by) VALUES
(1, 1, 'vote', 'Chon loai database: SQL Server hay MySQL?', 1),
(2, 5, 'vote', 'Chon framework: React hay Vue?', 1);

-- INSERT TASK OPTIONS
INSERT INTO task_options (task_id, option_text, option_order) VALUES
(1, 'SQL Server', 1), (1, 'MySQL', 2), (1, 'PostgreSQL', 3),
(2, 'React', 1), (2, 'Vue.js', 2), (2, 'Angular', 3);

-- INSERT USER RESPONSES
INSERT INTO user_responses (task_id, user_id, option_id) VALUES
(1, 1, 1), (1, 2, 1), (1, 3, 2), (1, 4, 1), (1, 5, 3),
(2, 5, 4), (2, 6, 5), (2, 7, 4);

-- INSERT ACTIVITY LOGS
INSERT INTO activity_logs (user_id, action, entity_type, entity_id, description, ip_address) VALUES
(1, 'LOGIN', 'users', 1, 'Admin dang nhap', '127.0.0.1'),
(2, 'LOGIN', 'users', 2, 'User1 dang nhap', '127.0.0.1'),
(1, 'CREATE_CHANNEL', 'channels', 1, 'Tao kenh general', '127.0.0.1'),
(1, 'CREATE_MESSAGE', 'messages', 1, 'Gui tin nhan', '127.0.0.1'),
(2, 'CREATE_VOTE', 'tasks', 1, 'Tao phieu binh chon', '127.0.0.1');

-- INSERT SYSTEM SETTINGS
INSERT INTO system_settings (setting_key, setting_value, setting_type) VALUES
('app_name', 'Quan ly Chat', 'string'),
('app_version', '1.0.0', 'string'),
('max_users', '1000', 'int'),
('allow_registration', 'true', 'boolean'),
('maintenance_mode', 'false', 'boolean');

-- INSERT STATISTICS
INSERT INTO app_statistics (stat_date, total_users, active_users, total_channels, total_messages, total_votes, total_quiz) VALUES
(CURRENT_DATE, 7, 5, 5, 10, 2, 0);
