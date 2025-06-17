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

    @Override
    public String toString() {
        return "SearchRequest{" +
                "searchTerm='" + searchTerm + '\'' +
                ", page=" + page +
                ", size=" + size +
                ", level='" + level + '\'' +
                '}';
    }
}
