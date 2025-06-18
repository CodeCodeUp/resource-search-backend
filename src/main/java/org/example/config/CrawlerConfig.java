package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 爬虫配置类
 */
@Component
@ConfigurationProperties(prefix = "crawler.qq-channel")
public class CrawlerConfig {
    
    private String apiUrl;
    private String cookie;
    private String guildNumber;
    private Integer getType;
    private Integer sortOption;
    private Boolean needChannelList;
    private Boolean needTopInfo;
    private Integer maxPages;
    private Integer pageDelaySeconds;
    private Integer fromParam;
    
    // Getters and Setters
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public String getCookie() {
        return cookie;
    }
    
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
    
    public String getGuildNumber() {
        return guildNumber;
    }
    
    public void setGuildNumber(String guildNumber) {
        this.guildNumber = guildNumber;
    }
    
    public Integer getGetType() {
        return getType;
    }
    
    public void setGetType(Integer getType) {
        this.getType = getType;
    }
    
    public Integer getSortOption() {
        return sortOption;
    }
    
    public void setSortOption(Integer sortOption) {
        this.sortOption = sortOption;
    }
    
    public Boolean getNeedChannelList() {
        return needChannelList;
    }
    
    public void setNeedChannelList(Boolean needChannelList) {
        this.needChannelList = needChannelList;
    }
    
    public Boolean getNeedTopInfo() {
        return needTopInfo;
    }
    
    public void setNeedTopInfo(Boolean needTopInfo) {
        this.needTopInfo = needTopInfo;
    }
    
    public Integer getMaxPages() {
        return maxPages;
    }
    
    public void setMaxPages(Integer maxPages) {
        this.maxPages = maxPages;
    }
    
    public Integer getPageDelaySeconds() {
        return pageDelaySeconds;
    }
    
    public void setPageDelaySeconds(Integer pageDelaySeconds) {
        this.pageDelaySeconds = pageDelaySeconds;
    }

    public Integer getFromParam() {
        return fromParam;
    }

    public void setFromParam(Integer fromParam) {
        this.fromParam = fromParam;
    }
}
