package com.simplechat.service;

import com.simplechat.entity.ActivityLog;
import com.simplechat.repository.ActivityLogRepository;
import com.simplechat.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ActivityLogService {
    
    @Autowired
    private ActivityLogRepository activityLogRepository;
    
    // Tao activity log
    public ActivityLog createLog(ActivityLog log) {
        log.setCreatedAt(LocalDateTime.now());
        return activityLogRepository.save(log);
    }
    
    // Lay log theo ID
    public Optional<ActivityLog> getLogById(Integer logId) {
        return activityLogRepository.findById(logId);
    }
    
    // Lay tat ca log cua user
    public List<ActivityLog> getLogsByUser(Integer userId) {
        return activityLogRepository.findByUser_UserId(userId);
    }
    
    // Lay log theo action
    public List<ActivityLog> getLogsByAction(String action) {
        return activityLogRepository.findByAction(action);
    }
    
    // Lay log gan day (7 ngay)
    public List<ActivityLog> getRecentLogs() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return activityLogRepository.findRecentActivities(sevenDaysAgo);
    }
    
    // Lay log gan day (custom days)
    public List<ActivityLog> getRecentLogsByDays(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return activityLogRepository.findRecentActivities(startDate);
    }
    
    // Log login
    public ActivityLog logLogin(User user, String ipAddress) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setAction("DANG_NHAP");
        log.setEntityType("users");
        log.setEntityId(user.getUserId());
        log.setDescription("User dang nhap vao he thong");
        log.setIpAddress(ipAddress);
        return createLog(log);
    }
    
    // Log logout
    public ActivityLog logLogout(User user, String ipAddress) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setAction("DANG_XUAT");
        log.setEntityType("users");
        log.setEntityId(user.getUserId());
        log.setDescription("User dang xuat khoi he thong");
        log.setIpAddress(ipAddress);
        return createLog(log);
    }
    
    // Log tao kenh
    public ActivityLog logChannelCreation(User user, Integer channelId, String channelName, String ipAddress) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setAction("TAO_KENH");
        log.setEntityType("channels");
        log.setEntityId(channelId);
        log.setDescription("Tao kenh: " + channelName);
        log.setIpAddress(ipAddress);
        return createLog(log);
    }
    
    // Log xoa kenh
    public ActivityLog logChannelDeletion(User user, Integer channelId, String channelName, String ipAddress) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setAction("XOA_KENH");
        log.setEntityType("channels");
        log.setEntityId(channelId);
        log.setDescription("Xoa kenh: " + channelName);
        log.setIpAddress(ipAddress);
        return createLog(log);
    }
    
    // Log tao tin nhan
    public ActivityLog logMessageCreation(User user, Integer messageId, String channelName, String ipAddress) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setAction("TAO_TIN_NHAN");
        log.setEntityType("messages");
        log.setEntityId(messageId);
        log.setDescription("Tao tin nhan o kenh: " + channelName);
        log.setIpAddress(ipAddress);
        return createLog(log);
    }
    
    // Log xoa tin nhan
    public ActivityLog logMessageDeletion(User user, Integer messageId, String ipAddress) {
        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setAction("XOA_TIN_NHAN");
        log.setEntityType("messages");
        log.setEntityId(messageId);
        log.setDescription("Xoa tin nhan");
        log.setIpAddress(ipAddress);
        return createLog(log);
    }
    
    // Log cam trai user
    public ActivityLog logUserBan(User adminUser, Integer userId, String reason, String ipAddress) {
        ActivityLog log = new ActivityLog();
        log.setUser(adminUser);
        log.setAction("CAM_TRAI_USER");
        log.setEntityType("users");
        log.setEntityId(userId);
        log.setDescription("Cam trai user. Ly do: " + reason);
        log.setIpAddress(ipAddress);
        return createLog(log);
    }
    
    // Lay tat ca log
    public List<ActivityLog> getAllLogs() {
        return activityLogRepository.findAll();
    }
}
