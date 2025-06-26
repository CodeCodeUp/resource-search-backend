package org.example.dto.quark;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 夸克网盘分享请求DTO
 */
public class QuarkShareRequest {
    
    @JsonProperty("fid_list")
    private List<String> fidList;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("url_type")
    private Integer urlType;
    
    @JsonProperty("expired_type")
    private Integer expiredType;

    // Constructors
    public QuarkShareRequest() {}

    public QuarkShareRequest(List<String> fidList, String title, Integer urlType, Integer expiredType) {
        this.fidList = fidList;
        this.title = title;
        this.urlType = urlType;
        this.expiredType = expiredType;
    }

    // Getters and Setters
    public List<String> getFidList() {
        return fidList;
    }

    public void setFidList(List<String> fidList) {
        this.fidList = fidList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getUrlType() {
        return urlType;
    }

    public void setUrlType(Integer urlType) {
        this.urlType = urlType;
    }

    public Integer getExpiredType() {
        return expiredType;
    }

    public void setExpiredType(Integer expiredType) {
        this.expiredType = expiredType;
    }

    @Override
    public String toString() {
        return "QuarkShareRequest{" +
                "fidList=" + fidList +
                ", title='" + title + '\'' +
                ", urlType=" + urlType +
                ", expiredType=" + expiredType +
                '}';
    }
}
