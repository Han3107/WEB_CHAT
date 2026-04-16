package com.simplechat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "banned_users")
public class BannedUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer banId;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "banned_by", nullable = false)
    private User bannedBy;
    
    @Column(columnDefinition = "text")
    private String reason;
    
    @Column(nullable = false, columnDefinition = "datetime2")
    private LocalDateTime banStart = LocalDateTime.now();
    
    @Column(columnDefinition = "datetime2")
    private LocalDateTime banEnd;
    
    @Column
    private Boolean isPermanent = false;

    public BannedUser() {}
    public BannedUser(User user, User bannedBy, String reason) {
        this.user = user;
        this.bannedBy = bannedBy;
        this.reason = reason;
    }

    public Integer getBanId() { return banId; }
    public void setBanId(Integer banId) { this.banId = banId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public User getBannedBy() { return bannedBy; }
    public void setBannedBy(User bannedBy) { this.bannedBy = bannedBy; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getBanStart() { return banStart; }
    public void setBanStart(LocalDateTime banStart) { this.banStart = banStart; }
    public LocalDateTime getBanEnd() { return banEnd; }
    public void setBanEnd(LocalDateTime banEnd) { this.banEnd = banEnd; }
    public Boolean getIsPermanent() { return isPermanent; }
    public void setIsPermanent(Boolean isPermanent) { this.isPermanent = isPermanent; }
}
