package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 夸克网盘配置类
 */
@Configuration
@ConfigurationProperties(prefix = "quark.pan")
public class QuarkPanConfig {
    
    /**
     * 夸克网盘Cookie
     */
    private String cookie = "";
    
    /**
     * 默认保存目录ID
     */
    private String defaultSaveDirectoryId = "";
    
    /**
     * 请求间隔时间（毫秒）
     */
    private int requestInterval = 2000;
    
    /**
     * 最大重试次数
     */
    private int maxRetryCount = 10;
    
    /**
     * 任务超时时间（毫秒）
     */
    private int taskTimeout = 300000; // 5分钟
    
    /**
     * 单次转存文件数量限制
     */
    private int maxFilesPerSave = 40;

    // Getters and Setters
    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getDefaultSaveDirectoryId() {
        return defaultSaveDirectoryId;
    }

    public void setDefaultSaveDirectoryId(String defaultSaveDirectoryId) {
        this.defaultSaveDirectoryId = defaultSaveDirectoryId;
    }

    public int getRequestInterval() {
        return requestInterval;
    }

    public void setRequestInterval(int requestInterval) {
        this.requestInterval = requestInterval;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public int getTaskTimeout() {
        return taskTimeout;
    }

    public void setTaskTimeout(int taskTimeout) {
        this.taskTimeout = taskTimeout;
    }

    public int getMaxFilesPerSave() {
        return maxFilesPerSave;
    }

    public void setMaxFilesPerSave(int maxFilesPerSave) {
        this.maxFilesPerSave = maxFilesPerSave;
    }
}
