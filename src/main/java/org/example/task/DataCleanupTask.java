package org.example.task;

import org.example.service.DataCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 数据清理一次性任务
 * 在应用启动时执行，清理数据库中包含反斜杠字符的资源
 */
@Component
@Order(1) // 设置执行顺序，确保在其他任务之前执行
public class DataCleanupTask implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataCleanupTask.class);
    
    @Autowired
    private DataCleanupService dataCleanupService;
    
    // 控制是否执行清理任务的开关
    private static final boolean ENABLE_CLEANUP = false;
    
    // 控制是否只预览不执行的开关
    private static final boolean PREVIEW_ONLY = false;
    
    @Override
    public void run(String... args) throws Exception {
        if (!ENABLE_CLEANUP) {
            logger.info("数据清理任务已禁用，跳过执行");
            return;
        }
        
        logger.info("=== 开始执行数据清理一次性任务 ===");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 统计需要清理的数据
            String statistics = dataCleanupService.getCleanupStatistics();
            logger.info(statistics);
            
            int count = dataCleanupService.countResourcesWithBackslashes();
            if (count == 0) {
                logger.info("数据库中没有需要清理的数据，任务结束");
                return;
            }
            
            if (PREVIEW_ONLY) {
                // 2. 预览模式：只显示需要清理的数据，不执行实际清理
                logger.info("=== 预览模式：显示需要清理的数据 ===");
                List<String> preview = dataCleanupService.previewCleanup();
                
                if (preview.isEmpty()) {
                    logger.info("没有需要清理的数据");
                } else {
                    logger.info("需要清理的数据预览（前10条）:");
                    for (int i = 0; i < Math.min(10, preview.size()); i++) {
                        logger.info("  {}", preview.get(i));
                    }
                    if (preview.size() > 10) {
                        logger.info("  ... 还有 {} 条数据需要清理", preview.size() - 10);
                    }
                }
            } else {
                // 3. 执行实际清理
                logger.info("=== 开始执行数据清理 ===");
                dataCleanupService.cleanupBackslashesInDatabase();
                
                // 4. 验证清理结果
                int remainingCount = dataCleanupService.countResourcesWithBackslashes();
                if (remainingCount == 0) {
                    logger.info("数据清理完成，所有反斜杠字符已清理");
                } else {
                    logger.warn("数据清理完成，但仍有 {} 条记录包含反斜杠字符", remainingCount);
                }
            }
            
            long endTime = System.currentTimeMillis();
            logger.info("=== 数据清理任务执行完成，耗时: {} ms ===", (endTime - startTime));
            
        } catch (Exception e) {
            logger.error("=== 数据清理任务执行失败: {} ===", e.getMessage(), e);
            // 不抛出异常，避免影响应用启动
        }
    }
    
    /**
     * 手动触发清理任务（用于测试或手动执行）
     */
    public void manualCleanup() {
        logger.info("=== 手动触发数据清理任务 ===");
        try {
            run();
        } catch (Exception e) {
            logger.error("手动执行数据清理任务失败", e);
        }
    }
    
    /**
     * 手动触发预览任务
     */
    public List<String> manualPreview() {
        logger.info("=== 手动触发数据清理预览 ===");
        try {
            return dataCleanupService.previewCleanup();
        } catch (Exception e) {
            logger.error("手动执行数据清理预览失败", e);
            return Arrays.asList("预览失败: " + e.getMessage());
        }
    }
}
