package org.example.enums;

/**
 * 资源类型枚举
 */
public enum ResourceType {
    
    MOVIE("movie", "电影"),
    TV_SERIES("movie", "电视剧"), 
    DOCUMENTARY("movie", "纪录片"),
    VARIETY("movie", "综艺"),
    NOVEL("novel", "小说"),
    ANIME("anime", "动漫"),
    SHORT_DRAMA("shortdrama", "短剧"),
    TOOL("study","资料"),
    DOCUMENT("study","PPT"),
    GUIDE("study","教程"),
    GAME("game","游戏"),
    WALLPAPER("wallpaper","壁纸"),
    OTHER("其他","其他类型")

            ;
    private final String code;
    private final String description;
    
    ResourceType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据频道名称获取资源类型
     */
    public static ResourceType getByChannelName(String channelName) {
        if (channelName == null) {
            return MOVIE; // 默认类型
        }
        
        String name = channelName.toLowerCase();
        
        for (ResourceType type : ResourceType.values()) {
            if (name.contains(type.getDescription().toLowerCase())) {
                return type;
            }
        }
        
        return MOVIE; // 默认类型
    }
}
