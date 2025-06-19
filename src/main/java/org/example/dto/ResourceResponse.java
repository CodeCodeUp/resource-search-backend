package org.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class ResourceResponse {

    private Integer id;
    private String name;
    private String content;
    private String url;
    private String pig;
    private Integer level;
    private String type;
    private Integer resourceTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // Default constructor
    public ResourceResponse() {}

    // Constructor with all fields
    public ResourceResponse(Integer id, String name, String content, String url, String pig,
                           Integer level, String type, Integer resourceTime, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.url = url;
        this.pig = pig;
        this.level = level;
        this.type = type;
        this.resourceTime = resourceTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPig() {
        return pig;
    }

    public void setPig(String pig) {
        this.pig = pig;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getResourceTime() {
        return resourceTime;
    }

    public void setResourceTime(Integer resourceTime) {
        this.resourceTime = resourceTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "ResourceResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                ", url='" + url + '\'' +
                ", pig='" + pig + '\'' +
                ", level=" + level +
                ", type='" + type + '\'' +
                ", resourceTime=" + resourceTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
