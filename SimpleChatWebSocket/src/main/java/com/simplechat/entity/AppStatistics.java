package com.simplechat.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "app_statistics")
public class AppStatistics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer statId;
    
    @Column(nullable = false, unique = true)
    private LocalDate statDate = LocalDate.now();
    
    @Column
    private Integer totalUsers = 0;
    
    @Column
    private Integer activeUsers = 0;
    
    @Column
    private Integer totalChannels = 0;
    
    @Column
    private Integer totalMessages = 0;
    
    @Column
    private Integer totalVotes = 0;
    
    @Column
    private Integer totalQuiz = 0;
    
    @Column
    private Integer activeSessions = 0;

    public AppStatistics() {}
    public AppStatistics(LocalDate statDate) {
        this.statDate = statDate;
    }

    public Integer getStatId() { return statId; }
    public void setStatId(Integer statId) { this.statId = statId; }
    public LocalDate getStatDate() { return statDate; }
    public void setStatDate(LocalDate statDate) { this.statDate = statDate; }
    public Integer getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
    public Integer getActiveUsers() { return activeUsers; }
    public void setActiveUsers(Integer activeUsers) { this.activeUsers = activeUsers; }
    public Integer getTotalChannels() { return totalChannels; }
    public void setTotalChannels(Integer totalChannels) { this.totalChannels = totalChannels; }
    public Integer getTotalMessages() { return totalMessages; }
    public void setTotalMessages(Integer totalMessages) { this.totalMessages = totalMessages; }
    public Integer getTotalVotes() { return totalVotes; }
    public void setTotalVotes(Integer totalVotes) { this.totalVotes = totalVotes; }
    public Integer getTotalQuiz() { return totalQuiz; }
    public void setTotalQuiz(Integer totalQuiz) { this.totalQuiz = totalQuiz; }
    public Integer getActiveSessions() { return activeSessions; }
    public void setActiveSessions(Integer activeSessions) { this.activeSessions = activeSessions; }
}
