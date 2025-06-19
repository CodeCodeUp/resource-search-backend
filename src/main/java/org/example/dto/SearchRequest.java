package org.example.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;

public class SearchRequest {

    @NotBlank(message = "Search term is required")
    private String searchTerm;

    @Min(value = 0, message = "Page number must be non-negative")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    private int size = 10;

    private String level; // Optional filter by level

    private String type; // Optional filter by content type (movie/novel/anime/shortdrama)

    private String searchMode = "multi"; // Search mode: "multi" (name+content) or "name" (name only)

    // Default constructor
    public SearchRequest() {}

    // Constructor with search term
    public SearchRequest(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    // Constructor with all fields
    public SearchRequest(String searchTerm, int page, int size, String level) {
        this.searchTerm = searchTerm;
        this.page = page;
        this.size = size;
        this.level = level;
    }

    // Constructor with all fields including type
    public SearchRequest(String searchTerm, int page, int size, String level, String type) {
        this.searchTerm = searchTerm;
        this.page = page;
        this.size = size;
        this.level = level;
        this.type = type;
    }

    // Constructor with all fields including type and search mode
    public SearchRequest(String searchTerm, int page, int size, String level, String type, String searchMode) {
        this.searchTerm = searchTerm;
        this.page = page;
        this.size = size;
        this.level = level;
        this.type = type;
        this.searchMode = searchMode;
    }

    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(String searchMode) {
        this.searchMode = searchMode;
    }

    @Override
    public String toString() {
        return "SearchRequest{" +
                "searchTerm='" + searchTerm + '\'' +
                ", page=" + page +
                ", size=" + size +
                ", level='" + level + '\'' +
                ", type='" + type + '\'' +
                ", searchMode='" + searchMode + '\'' +
                '}';
    }
}
