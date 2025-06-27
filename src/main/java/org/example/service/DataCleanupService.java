package org.example.service;

import org.example.entity.Resource;
import org.example.mapper.ResourceMapper;
import org.example.util.StringCleanupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据清理服务
 * 用于清理数据库中的特殊字符
 */
@Service
public class DataCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataCleanupService.class);
    
    @Autowired
    private ResourceMapper resourceMapper;
    
    /**
     * 清理数据库中所有包含反斜杠字符的资源
     * 这是一个一次性任务，用于清理历史数据
     */
    @Transactional
    public void cleanupBackslashesInDatabase() {
        logger.info("开始清理数据库中包含反斜杠字符的资源...");
        
        try {
            // 1. 查询所有包含反斜杠字符的资源
            List<Resource> resourcesWithBackslashes = resourceMapper.selectResourcesWithBackslashes();
            
            if (resourcesWithBackslashes.isEmpty()) {
                logger.info("数据库中没有包含反斜杠字符的资源，无需清理");
                return;
            }
            
            logger.info("找到 {} 条包含反斜杠字符的资源", resourcesWithBackslashes.size());
            
            // 2. 清理每个资源的字段
            List<Resource> cleanedResources = new ArrayList<>();
            int cleanedCount = 0;
            
            for (Resource resource : resourcesWithBackslashes) {
                String originalName = resource.getName();
                String originalContent = resource.getContent();
                String originalUrl = resource.getUrl();
                
                // 清理字段
                String[] cleanedFields = StringCleanupUtil.cleanResourceFields(
                    originalName, originalContent, originalUrl
                );
                
                // 检查是否有字段被修改
                boolean hasChanges = false;
                if (!equals(originalName, cleanedFields[0])) {
                    resource.setName(cleanedFields[0]);
                    hasChanges = true;
                }
                if (!equals(originalContent, cleanedFields[1])) {
                    resource.setContent(cleanedFields[1]);
                    hasChanges = true;
                }
                if (!equals(originalUrl, cleanedFields[2])) {
                    resource.setUrl(cleanedFields[2]);
                    hasChanges = true;
                }
                
                if (hasChanges) {
                    cleanedResources.add(resource);
                    cleanedCount++;
                    
                    logger.debug("清理资源 ID {}: '{}' -> '{}'", 
                        resource.getId(), originalName, cleanedFields[0]);
                }
            }
            
            // 3. 循环更新数据库
            if (!cleanedResources.isEmpty()) {
                int updatedCount = 0;
                for (Resource resource : cleanedResources) {
                    int result = resourceMapper.updateResourceFields(resource);
                    if (result > 0) {
                        updatedCount++;
                    }
                }

                logger.info("数据清理完成，共清理了 {} 条资源", updatedCount);
            } else {
                logger.info("虽然查询到包含反斜杠的资源，但清理后没有实际变化");
            }
            
        } catch (Exception e) {
            logger.error("清理数据库中的反斜杠字符时发生错误", e);
            throw e;
        }
    }
    
    /**
     * 统计数据库中包含反斜杠字符的资源数量
     */
    public int countResourcesWithBackslashes() {
        try {
            List<Resource> resources = resourceMapper.selectResourcesWithBackslashes();
            int count = resources.size();
            logger.info("数据库中包含反斜杠字符的资源数量: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("统计包含反斜杠字符的资源数量时发生错误", e);
            return -1;
        }
    }
    
    /**
     * 预览需要清理的资源（不执行实际清理）
     */
    public List<String> previewCleanup() {
        List<String> preview = new ArrayList<>();
        
        try {
            List<Resource> resourcesWithBackslashes = resourceMapper.selectResourcesWithBackslashes();
            
            for (Resource resource : resourcesWithBackslashes) {
                String originalName = resource.getName();
                String originalContent = resource.getContent();
                String originalUrl = resource.getUrl();
                
                String[] cleanedFields = StringCleanupUtil.cleanResourceFields(
                    originalName, originalContent, originalUrl
                );
                
                if (!equals(originalName, cleanedFields[0]) ||
                    !equals(originalContent, cleanedFields[1]) ||
                    !equals(originalUrl, cleanedFields[2])) {
                    
                    preview.add(String.format("ID: %d, 原名称: '%s', 清理后: '%s'", 
                        resource.getId(), originalName, cleanedFields[0]));
                }
            }
            
        } catch (Exception e) {
            logger.error("预览清理时发生错误", e);
        }
        
        return preview;
    }
    
    /**
     * 安全的字符串比较，处理null值
     */
    private boolean equals(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }
    
    /**
     * 获取清理统计信息
     */
    public String getCleanupStatistics() {
        try {
            List<Resource> resources = resourceMapper.selectResourcesWithBackslashes();
            
            int totalCount = resources.size();
            int nameCount = 0;
            int contentCount = 0;
            int urlCount = 0;
            
            for (Resource resource : resources) {
                if (StringCleanupUtil.containsBackslashes(resource.getName())) {
                    nameCount++;
                }
                if (StringCleanupUtil.containsBackslashes(resource.getContent())) {
                    contentCount++;
                }
                if (StringCleanupUtil.containsBackslashes(resource.getUrl())) {
                    urlCount++;
                }
            }
            
            return String.format(
                "清理统计: 总计 %d 条资源需要清理，其中名称字段 %d 条，内容字段 %d 条，URL字段 %d 条",
                totalCount, nameCount, contentCount, urlCount
            );
            
        } catch (Exception e) {
            logger.error("获取清理统计信息时发生错误", e);
            return "获取统计信息失败: " + e.getMessage();
        }
    }
}
