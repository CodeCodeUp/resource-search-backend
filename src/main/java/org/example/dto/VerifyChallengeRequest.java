package org.example.dto;

/**
 * 获取验证挑战请求DTO
 */
public class VerifyChallengeRequest {
    
    private String deviceFingerprint;
    private String userAgent;
    private Long timestamp;
    
    public VerifyChallengeRequest() {}
    
    public VerifyChallengeRequest(String deviceFingerprint, String userAgent, Long timestamp) {
        this.deviceFingerprint = deviceFingerprint;
        this.userAgent = userAgent;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
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
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
