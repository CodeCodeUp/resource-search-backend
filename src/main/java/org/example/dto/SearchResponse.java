package org.example.dto;

import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 搜索响应DTO，包含搜索结果和实际执行的搜索关键词
 */
public class SearchResponse {

    private PageInfo<ResourceResponse> pageInfo;
    
    private String originalSearchTerm; // 用户输入的原始搜索词
    
    private List<String> actualSearchTerms; // 实际执行搜索的关键词列表
    
    private List<String> words; // 实际搜索词（前端使用的字段名）
    
    private String searchStrategy; // 搜索策略：complete/tokenized/chinese_tokenized
    
    private String searchMode; // 搜索模式：multi/name
    
    private Integer level; // 层级过滤
    
    private String type; // 类型过滤

    // 默认构造函数
    public SearchResponse() {}

    // 带参构造函数
    public SearchResponse(PageInfo<ResourceResponse> pageInfo, String originalSearchTerm, 
                         List<String> actualSearchTerms, String searchStrategy, 
                         String searchMode, Integer level, String type) {
        this.pageInfo = pageInfo;
        this.originalSearchTerm = originalSearchTerm;
        this.actualSearchTerms = actualSearchTerms;
        this.words = actualSearchTerms; // words字段指向actualSearchTerms
        this.searchStrategy = searchStrategy;
        this.searchMode = searchMode;
        this.level = level;
        this.type = type;
    }

    // Getters and Setters
    public PageInfo<ResourceResponse> getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo<ResourceResponse> pageInfo) {
        this.pageInfo = pageInfo;
    }

    public String getOriginalSearchTerm() {
        return originalSearchTerm;
    }

    public void setOriginalSearchTerm(String originalSearchTerm) {
        this.originalSearchTerm = originalSearchTerm;
    }

    public List<String> getActualSearchTerms() {
        return actualSearchTerms;
    }

    public void setActualSearchTerms(List<String> actualSearchTerms) {
        this.actualSearchTerms = actualSearchTerms;
        this.words = actualSearchTerms; // 同步更新words字段
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
        this.actualSearchTerms = words; // 同步更新actualSearchTerms字段
    }

    public String getSearchStrategy() {
        return searchStrategy;
    }

    public void setSearchStrategy(String searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    public String getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(String searchMode) {
        this.searchMode = searchMode;
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

    @Override
    public String toString() {
        return "SearchResponse{" +
                "pageInfo=" + pageInfo +
                ", originalSearchTerm='" + originalSearchTerm + '\'' +
                ", actualSearchTerms=" + actualSearchTerms +
                ", words=" + words +
                ", searchStrategy='" + searchStrategy + '\'' +
                ", searchMode='" + searchMode + '\'' +
                ", level=" + level +
                ", type='" + type + '\'' +
                '}';
    }
}
