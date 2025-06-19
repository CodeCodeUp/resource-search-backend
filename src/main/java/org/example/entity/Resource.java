package org.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

public class Resource {

    private Integer id;

    @NotBlank(message = "资源名称不能为空")
    private String name;

    private String content;

    private String url;

    private String pig;

    @NotNull(message = "资源层级不能为空")
    private Integer level = 1;

    private String type;

    private Integer resourceTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 默认构造函数
    public Resource() {}

    // 带参构造函数
    public Resource(String name, String content, String url, String pig, Integer level, String type) {
        this.name = name;
        this.content = content;
        this.url = url;
        this.pig = pig;
        this.level = level;
        this.type = type;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(id, resource.id) &&
                Objects.equals(name, resource.name) &&
                Objects.equals(content, resource.content) &&
                Objects.equals(url, resource.url) &&
                Objects.equals(pig, resource.pig) &&
                Objects.equals(level, resource.level) &&
                Objects.equals(type, resource.type) &&
                Objects.equals(resourceTime, resource.resourceTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, content, url, pig, level, type, resourceTime);
    }

    @Override
    public String toString() {
        return "Resource{" +
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
