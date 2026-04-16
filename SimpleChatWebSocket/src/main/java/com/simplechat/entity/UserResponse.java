package com.simplechat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_responses")
public class UserResponse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer responseId;
    
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "option_id", nullable = false)
    private TaskOption option;
    
    @Column(nullable = false, columnDefinition = "datetime2")
    private LocalDateTime respondedAt = LocalDateTime.now();

    public UserResponse() {}
    public UserResponse(Task task, User user, TaskOption option) {
        this.task = task;
        this.user = user;
        this.option = option;
    }

    public Integer getResponseId() { return responseId; }
    public void setResponseId(Integer responseId) { this.responseId = responseId; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public TaskOption getOption() { return option; }
    public void setOption(TaskOption option) { this.option = option; }
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
}
