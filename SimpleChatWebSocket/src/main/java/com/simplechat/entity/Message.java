package com.simplechat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageId;
    
    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, columnDefinition = "text")
    private String content;
    
    @Column(length = 20)
    private String messageType; // text, image, file, vote, quiz, system
    
    @Column(nullable = false, columnDefinition = "datetime2")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(columnDefinition = "datetime2")
    private LocalDateTime editedAt;
    
    @Column
    private Boolean isDeleted = false;
    
    @ManyToOne
    @JoinColumn(name = "deleted_by")
    private User deletedBy;
    
    @Column(columnDefinition = "datetime2")
    private LocalDateTime deletedAt;

    public Message() {}

    public Message(Channel channel, User user, String content, String messageType) {
        this.channel = channel;
        this.user = user;
        this.content = content;
        this.messageType = messageType;
    }

    public Integer getMessageId() { return messageId; }
    public void setMessageId(Integer messageId) { this.messageId = messageId; }

    public Channel getChannel() { return channel; }
    public void setChannel(Channel channel) { this.channel = channel; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getEditedAt() { return editedAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public User getDeletedBy() { return deletedBy; }
    public void setDeletedBy(User deletedBy) { this.deletedBy = deletedBy; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
