package org.example.scheduler;

import org.example.service.QQChannelCrawlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * QQ频道爬虫定时任务调度器
 */
@Component
public class QQChannelCrawlerScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(QQChannelCrawlerScheduler.class);
    
    @Autowired
    private QQChannelCrawlerService crawlerService;
    
    /**
     * 定时爬取QQ频道资源
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    public void scheduledCrawl() {
        logger.info("定时任务开始执行QQ频道资源爬取...");
        
        try {
            crawlerService.crawlChannelResources();
            logger.info("定时任务执行完成");
        } catch (Exception e) {
            logger.error("定时任务执行过程中发生错误: ", e);
        }
    }
    
    /**
     * 应用启动后延迟5分钟执行第一次爬取
     * 然后每小时执行一次
     */
    @Scheduled(initialDelay = 300000, fixedRate = 3600000) // 延迟5分钟，然后每小时执行
    public void scheduledCrawlWithDelay() {
        // 这个方法可以用来替代上面的方法，如果需要启动延迟的话
        // 目前注释掉，避免重复执行
        // scheduledCrawl();
    }
}
