package com.simplechat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer taskId;
    
    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;
    
    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;
    
    @Column(nullable = false, length = 20)
    private String taskType; // vote, quiz, file, board
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(nullable = false, columnDefinition = "datetime2")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(length = 20)
    private String status; // pending, active, closed

    public Task() {}
    public Task(Channel channel, Message message, String taskType, String title, User createdBy) {
        this.channel = channel;
        this.message = message;
        this.taskType = taskType;
        this.title = title;
        this.createdBy = createdBy;
    }

    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }
    public Channel getChannel() { return channel; }
    public void setChannel(Channel channel) { this.channel = channel; }
    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
