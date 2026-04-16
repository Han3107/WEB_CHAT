package com.simplechat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
public class SystemSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer settingId;
    
    @Column(nullable = false, unique = true, length = 100)
    private String settingKey;
    
    @Column(columnDefinition = "text")
    private String settingValue;
    
    @Column(length = 50)
    private String settingType; // string, int, boolean, json
    
    @Column(nullable = false, columnDefinition = "datetime2")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public SystemSettings() {}
    public SystemSettings(String settingKey, String settingValue, String settingType) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.settingType = settingType;
    }

    public Integer getSettingId() { return settingId; }
    public void setSettingId(Integer settingId) { this.settingId = settingId; }
    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }
    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
    public String getSettingType() { return settingType; }
    public void setSettingType(String settingType) { this.settingType = settingType; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
