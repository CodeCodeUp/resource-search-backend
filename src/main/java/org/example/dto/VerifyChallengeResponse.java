package org.example.dto;

/**
 * 验证挑战响应DTO
 */
public class VerifyChallengeResponse {
    
    private String challengeId;
    private String backgroundImage;
    private PuzzlePosition puzzlePosition;
    private Integer tolerance;
    private Long expiresAt;
    private String serverSignature;
    
    public VerifyChallengeResponse() {}
    
    public VerifyChallengeResponse(String challengeId, String backgroundImage, 
                                 PuzzlePosition puzzlePosition, Integer tolerance, 
                                 Long expiresAt, String serverSignature) {
        this.challengeId = challengeId;
        this.backgroundImage = backgroundImage;
        this.puzzlePosition = puzzlePosition;
        this.tolerance = tolerance;
        this.expiresAt = expiresAt;
        this.serverSignature = serverSignature;
    }
    
    // Getters and Setters
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
    
    public PuzzlePosition getPuzzlePosition() {
        return puzzlePosition;
    }
    
    public void setPuzzlePosition(PuzzlePosition puzzlePosition) {
        this.puzzlePosition = puzzlePosition;
    }
    
    public Integer getTolerance() {
        return tolerance;
    }
    
    public void setTolerance(Integer tolerance) {
        this.tolerance = tolerance;
    }
    
    public Long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getServerSignature() {
        return serverSignature;
    }
    
    public void setServerSignature(String serverSignature) {
        this.serverSignature = serverSignature;
    }
    
    /**
     * 拼图位置内部类
     */
    public static class PuzzlePosition {
        private Integer x;
        private Integer y;
        
        public PuzzlePosition() {}
        
        public PuzzlePosition(Integer x, Integer y) {
            this.x = x;
            this.y = y;
        }
        
        public Integer getX() {
            return x;
        }
        
        public void setX(Integer x) {
            this.x = x;
        }
        
        public Integer getY() {
            return y;
        }
        
        public void setY(Integer y) {
            this.y = y;
        }
    }
}
