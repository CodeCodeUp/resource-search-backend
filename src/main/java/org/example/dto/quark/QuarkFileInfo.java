package org.example.dto.quark;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 夸克网盘文件信息DTO
 */
public class QuarkFileInfo {
    
    @JsonProperty("fid")
    private String fid;
    
    @JsonProperty("share_fid_token")
    private String shareFidToken;
    
    @JsonProperty("file_name")
    private String fileName;
    
    @JsonProperty("dir")
    private Boolean isDirectory;
    
    @JsonProperty("size")
    private Long size;
    
    @JsonProperty("updated_at")
    private Long updatedAt;

    // Constructors
    public QuarkFileInfo() {}

    public QuarkFileInfo(String fid, String shareFidToken, String fileName, Boolean isDirectory) {
        this.fid = fid;
        this.shareFidToken = shareFidToken;
        this.fileName = fileName;
        this.isDirectory = isDirectory;
    }

    // Getters and Setters
    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getShareFidToken() {
        return shareFidToken;
    }

    public void setShareFidToken(String shareFidToken) {
        this.shareFidToken = shareFidToken;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getIsDirectory() {
        return isDirectory;
    }

    public void setIsDirectory(Boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "QuarkFileInfo{" +
                "fid='" + fid + '\'' +
                ", shareFidToken='" + shareFidToken + '\'' +
                ", fileName='" + fileName + '\'' +
                ", isDirectory=" + isDirectory +
                ", size=" + size +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
