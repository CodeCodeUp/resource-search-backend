package org.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 字符串清理工具类
 * 用于清理资源数据中的特殊字符
 */
public class StringCleanupUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(StringCleanupUtil.class);
    
    /**
     * 清理字符串中的反斜杠字符
     * 
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String removeBackslashes(String input) {
        if (input == null) {
            return null;
        }
        
        String original = input;
        String cleaned = input.replace("\\", "");
        
        if (!original.equals(cleaned)) {
            logger.debug("清理反斜杠字符: '{}' -> '{}'", original, cleaned);
        }
        
        return cleaned;
    }
    
    /**
     * 清理字符串中的多种特殊字符
     * 包括反斜杠、制表符、换行符等
     *
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String cleanSpecialCharacters(String input) {
        if (input == null) {
            return null;
        }

        String original = input;
        String cleaned = input
            .replace("\\", "")           // 移除反斜杠
            .replace("\r", "")           // 移除实际的回车符
            .replace("\n", "")           // 移除实际的换行符
            .replace("\t", "")           // 移除实际的制表符
            .replaceAll("\\s+", " ")     // 将多个空白字符替换为单个空格
            .trim();                     // 去除首尾空白

        if (!original.equals(cleaned)) {
            logger.debug("清理特殊字符: '{}' -> '{}'", original, cleaned);
        }

        return cleaned;
    }
    
    /**
     * 清理资源名称
     * 专门用于清理资源名称字段
     * 
     * @param name 资源名称
     * @return 清理后的名称
     */
    public static String cleanResourceName(String name) {
        if (name == null) {
            return name;
        }

        String cleaned = cleanSpecialCharacters(name);

        // 如果清理后为空，返回空字符串
        if (cleaned == null || cleaned.trim().isEmpty()) {
            return "";
        }
        
        // 限制长度，避免过长的名称
        if (cleaned.length() > 255) {
            cleaned = cleaned.substring(0, 255);
            logger.debug("资源名称过长，已截断: {}", cleaned);
        }
        
        return cleaned;
    }
    
    /**
     * 清理资源内容
     * 专门用于清理资源内容字段
     * 
     * @param content 资源内容
     * @return 清理后的内容
     */
    public static String cleanResourceContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        
        String cleaned = cleanSpecialCharacters(content);
        
        // 限制长度，避免过长的内容
        if (cleaned.length() > 1000) {
            cleaned = cleaned.substring(0, 1000);
            logger.debug("资源内容过长，已截断: {}", cleaned);
        }
        
        return cleaned;
    }
    
    /**
     * 清理资源URL
     * 专门用于清理资源URL字段
     * 
     * @param url 资源URL
     * @return 清理后的URL
     */
    public static String cleanResourceUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }
        
        // 对于URL，只移除反斜杠，保留其他字符
        String cleaned = removeBackslashes(url).trim();
        
        return cleaned;
    }
    
    /**
     * 批量清理资源字段
     * 返回清理后的字符串数组：[name, content, url]
     * 
     * @param name 资源名称
     * @param content 资源内容
     * @param url 资源URL
     * @return 清理后的字符串数组
     */
    public static String[] cleanResourceFields(String name, String content, String url) {
        return new String[] {
            cleanResourceName(name),
            cleanResourceContent(content),
            cleanResourceUrl(url)
        };
    }
    
    /**
     * 检查字符串是否包含反斜杠
     * 
     * @param input 输入字符串
     * @return 是否包含反斜杠
     */
    public static boolean containsBackslashes(String input) {
        return input != null && input.contains("\\");
    }
    
    /**
     * 统计字符串中反斜杠的数量
     * 
     * @param input 输入字符串
     * @return 反斜杠数量
     */
    public static int countBackslashes(String input) {
        if (input == null) {
            return 0;
        }
        
        int count = 0;
        for (char c : input.toCharArray()) {
            if (c == '\\') {
                count++;
            }
        }
        return count;
    }
}
