package org.example.dto;

/**
 * QQ频道爬取到的资源信息
 */
public class QQChannelResource {
    
    private String title;
    private String description;
    private String link;
    private String imageUrl;
    
    public QQChannelResource() {}
    
    public QQChannelResource(String title, String description, String link, String imageUrl) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.imageUrl = imageUrl;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
        this.link = link;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    @Override
    public String toString() {
        return "QQChannelResource{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", link='" + link + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
