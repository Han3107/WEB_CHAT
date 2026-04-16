package com.simplechat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer logId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(length = 50)
    private String action;
    
    @Column(length = 50)
    private String entityType;
    
    @Column
    private Integer entityId;
    
    @Column(columnDefinition = "text")
    private String description;
    
    @Column(length = 45)
    private String ipAddress;
    
    @Column(nullable = false, columnDefinition = "datetime2")
    private LocalDateTime createdAt = LocalDateTime.now();

    public ActivityLog() {}
    public ActivityLog(User user, String action, String entityType, String description) {
        this.user = user;
        this.action = action;
        this.entityType = entityType;
        this.description = description;
    }

    public Integer getLogId() { return logId; }
    public void setLogId(Integer logId) { this.logId = logId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
