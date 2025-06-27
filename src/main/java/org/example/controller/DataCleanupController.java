package org.example.controller;

import org.example.service.DataCleanupService;
import org.example.task.DataCleanupTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据清理控制器
 * 提供手动触发数据清理的API接口
 */
@RestController
@RequestMapping("/api/admin/cleanup")
public class DataCleanupController {
    
    private static final Logger logger = LoggerFactory.getLogger(DataCleanupController.class);
    
    @Autowired
    private DataCleanupService dataCleanupService;
    
    @Autowired
    private DataCleanupTask dataCleanupTask;
    
    /**
     * 获取清理统计信息
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String statistics = dataCleanupService.getCleanupStatistics();
            int count = dataCleanupService.countResourcesWithBackslashes();
            
            result.put("success", true);
            result.put("statistics", statistics);
            result.put("count", count);
            result.put("message", "统计信息获取成功");
            
            logger.info("获取清理统计信息: {}", statistics);
            
        } catch (Exception e) {
            logger.error("获取清理统计信息失败", e);
            result.put("success", false);
            result.put("message", "获取统计信息失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 预览需要清理的数据
     */
    @GetMapping("/preview")
    public Map<String, Object> previewCleanup() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> preview = dataCleanupService.previewCleanup();
            
            result.put("success", true);
            result.put("preview", preview);
            result.put("count", preview.size());
            result.put("message", "预览数据获取成功");
            
            logger.info("预览清理数据，共 {} 条需要清理", preview.size());
            
        } catch (Exception e) {
            logger.error("预览清理数据失败", e);
            result.put("success", false);
            result.put("message", "预览失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 执行数据清理
     */
    @PostMapping("/execute")
    public Map<String, Object> executeCleanup() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("手动触发数据清理任务");
            
            // 获取清理前的统计
            int beforeCount = dataCleanupService.countResourcesWithBackslashes();
            
            // 执行清理
            dataCleanupService.cleanupBackslashesInDatabase();
            
            // 获取清理后的统计
            int afterCount = dataCleanupService.countResourcesWithBackslashes();
            int cleanedCount = beforeCount - afterCount;
            
            result.put("success", true);
            result.put("beforeCount", beforeCount);
            result.put("afterCount", afterCount);
            result.put("cleanedCount", cleanedCount);
            result.put("message", String.format("清理完成，共清理了 %d 条记录", cleanedCount));
            
            logger.info("数据清理完成，清理前: {} 条，清理后: {} 条，实际清理: {} 条", 
                beforeCount, afterCount, cleanedCount);
            
        } catch (Exception e) {
            logger.error("执行数据清理失败", e);
            result.put("success", false);
            result.put("message", "清理失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 检查是否需要清理
     */
    @GetMapping("/check")
    public Map<String, Object> checkNeedCleanup() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int count = dataCleanupService.countResourcesWithBackslashes();
            boolean needCleanup = count > 0;
            
            result.put("success", true);
            result.put("needCleanup", needCleanup);
            result.put("count", count);
            result.put("message", needCleanup ? 
                String.format("发现 %d 条记录需要清理", count) : "没有需要清理的记录");
            
        } catch (Exception e) {
            logger.error("检查清理需求失败", e);
            result.put("success", false);
            result.put("message", "检查失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("service", "DataCleanupService");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
