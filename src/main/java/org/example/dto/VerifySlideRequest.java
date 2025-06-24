package org.example.dto;

/**
 * 滑动验证请求DTO
 */
public class VerifySlideRequest {
    
    private String challengeId;
    private String token;
    private Long timestamp;
    private String resourceId;
    private Double slidePosition;
    private Long slideTime;
    private String deviceFingerprint;
    private String userAgent;
    
    public VerifySlideRequest() {}
    
    public VerifySlideRequest(String challengeId, String token, Long timestamp, 
                             String resourceId, Double slidePosition, Long slideTime, 
                             String deviceFingerprint, String userAgent) {
        this.challengeId = challengeId;
        this.token = token;
        this.timestamp = timestamp;
        this.resourceId = resourceId;
        this.slidePosition = slidePosition;
        this.slideTime = slideTime;
        this.deviceFingerprint = deviceFingerprint;
        this.userAgent = userAgent;
    }
    
    // Getters and Setters
    public String getChallengeId() {
        return challengeId;
    }
    
    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    public Double getSlidePosition() {
        return slidePosition;
    }
    
    public void setSlidePosition(Double slidePosition) {
        this.slidePosition = slidePosition;
    }
    
    public Long getSlideTime() {
        return slideTime;
    }
    
    public void setSlideTime(Long slideTime) {
        this.slideTime = slideTime;
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
}
