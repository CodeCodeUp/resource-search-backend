package org.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 验证挑战实体类
 */
public class VerifyChallenge {
    
    private String id;
    private String challengeId;
    private String backgroundImage;
    private String puzzlePosition; // JSON格式存储位置信息
    private Integer tolerance;
    private String deviceFingerprint;
    private String userAgent;
    private String ipAddress;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime usedAt;
    
    private String status; // pending, verified, expired, failed
    
    public VerifyChallenge() {}
    
    public VerifyChallenge(String challengeId, String backgroundImage, String puzzlePosition, 
                          Integer tolerance, String deviceFingerprint, String userAgent, String ipAddress) {
        this.challengeId = challengeId;
        this.backgroundImage = backgroundImage;
        this.puzzlePosition = puzzlePosition;
        this.tolerance = tolerance;
        this.deviceFingerprint = deviceFingerprint;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.status = "pending";
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(5); // 5分钟过期
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getChallengeId() {
        return challengeId;
    }
    
    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }
    
    public String getBackgroundImage() {
        return backgroundImage;
    }
    
    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }
    
    public String getPuzzlePosition() {
        return puzzlePosition;
    }
    
    public void setPuzzlePosition(String puzzlePosition) {
        this.puzzlePosition = puzzlePosition;
    }
    
    public Integer getTolerance() {
        return tolerance;
    }
    
    public void setTolerance(Integer tolerance) {
        this.tolerance = tolerance;
    }
    
    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }
    
    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getUsedAt() {
        return usedAt;
    }
    
    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
    
    public boolean isUsed() {
        return this.usedAt != null;
    }
}
