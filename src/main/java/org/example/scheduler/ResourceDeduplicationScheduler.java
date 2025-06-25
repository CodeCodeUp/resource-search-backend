package org.example.scheduler;

import org.example.service.ResourceDeduplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 资源去重定时任务调度器
 * 每5分钟执行一次去重操作
 */
@Component
public class ResourceDeduplicationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceDeduplicationScheduler.class);

    @Autowired
    private ResourceDeduplicationService deduplicationService;

    /**
     * 定时执行资源去重
     * 每60分钟执行一次
     */
    @Scheduled(fixedRate = 3600000) // 5分钟 = 300000毫秒
    public void scheduledDeduplication() {
        logger.info("定时任务开始执行资源去重...");

        try {
            // 先获取统计信息
            ResourceDeduplicationService.DeduplicationStats stats = 
                deduplicationService.getDeduplicationStats();

            if (stats.getTotalDuplicateCount() == 0) {
                logger.info("未发现重复资源，跳过去重操作");
                return;
            }

            logger.info("发现重复资源 - URL重复: {} 条, 名称重复: {} 条", 
                       stats.getUrlDuplicateCount(), stats.getNameDuplicateCount());

            // 执行去重操作
            deduplicationService.deduplicateResources();

            logger.info("定时去重任务执行完成");

        } catch (Exception e) {
            logger.error("定时去重任务执行过程中发生错误: ", e);
        }
    }

    /**
     * 应用启动后延迟1分钟执行第一次去重
     * 然后每5分钟执行一次
     */
    @Scheduled(initialDelay = 60000, fixedRate = 300000) // 延迟1分钟，然后每5分钟执行
    public void scheduledDeduplicationWithDelay() {
        // 这个方法可以用来替代上面的方法，如果需要启动延迟的话
        // 目前注释掉，避免重复执行
        // scheduledDeduplication();
    }
}
