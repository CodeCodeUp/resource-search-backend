package org.example.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ResourceRequest {

    @NotBlank(message = "资源名称不能为空")
    @Size(max = 100, message = "资源名称不能超过100个字符")
    private String name;

    @Size(max = 10000, message = "资源内容不能超过10000个字符")
    private String content;

    @Size(max = 255, message = "资源URL不能超过255个字符")
    private String url;

    @Size(max = 50, message = "pig字段不能超过50个字符")
    private String pig;

    @NotNull(message = "资源层级不能为空")
    private Integer level = 1;

    @Size(max = 50, message = "资源类型不能超过50个字符")
    private String type;

    private Integer resourceTime;

    // Default constructor
    public ResourceRequest() {}

    // Constructor with all fields
    public ResourceRequest(String name, String content, String url, String pig, Integer level, String type, Integer resourceTime) {
        this.name = name;
        this.content = content;
        this.url = url;
        this.pig = pig;
        this.level = level;
        this.type = type;
        this.resourceTime = resourceTime;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "ResourceRequest{" +
                "name='" + name + '\'' +
                ", content='" + content + '\'' +
                ", url='" + url + '\'' +
                ", pig='" + pig + '\'' +
                ", level=" + level +
                ", type='" + type + '\'' +
                ", resourceTime=" + resourceTime +
                '}';
    }
}
