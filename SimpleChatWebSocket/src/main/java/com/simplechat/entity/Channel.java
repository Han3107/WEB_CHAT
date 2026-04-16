package com.simplechat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "channels")
public class Channel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer channelId;
    
    @Column(nullable = false, length = 100)
    private String channelName;
    
    @Column(columnDefinition = "text")
    private String description;
    
    @Column(length = 20)
    private String channelType; // public, private, group
    
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(nullable = false, columnDefinition = "datetime2")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column
    private Boolean isActive = true;
    
    @Column
    private Integer memberCount = 0;
    
    @Column
    private Integer messageCount = 0;

    public Channel() {}

    public Channel(String channelName, String channelType, User createdBy) {
        this.channelName = channelName;
        this.channelType = channelType;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Integer getChannelId() { return channelId; }
    public void setChannelId(Integer channelId) { this.channelId = channelId; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }

    public Integer getMessageCount() { return messageCount; }
    public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }
}
