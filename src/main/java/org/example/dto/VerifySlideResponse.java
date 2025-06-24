package org.example.dto;

/**
 * 滑动验证响应DTO
 */
public class VerifySlideResponse {
    
    private Boolean verified;
    private String accessToken;
    private Long expiresAt;
    private String sessionId;
    
    public VerifySlideResponse() {}
    
    public VerifySlideResponse(Boolean verified, String accessToken, Long expiresAt, String sessionId) {
        this.verified = verified;
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
        this.sessionId = sessionId;
    }
    
    // Getters and Setters
    public Boolean getVerified() {
        return verified;
    }
    
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public Long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
