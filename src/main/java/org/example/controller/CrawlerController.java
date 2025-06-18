package org.example.controller;

import org.example.service.QQChannelCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 爬虫控制器 - 用于手动触发爬虫任务
 */
@RestController
@RequestMapping("/crawler")
public class CrawlerController {
    
    @Autowired
    private QQChannelCrawlerService crawlerService;
    
    /**
     * 手动触发QQ频道爬虫
     */
    @PostMapping("/qq-channel/start")
    public Map<String, Object> startQQChannelCrawler() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 在新线程中执行爬虫任务，避免阻塞HTTP请求
            new Thread(() -> {
                crawlerService.crawlChannelResources();
            }).start();
            
            result.put("success", true);
            result.put("message", "QQ频道爬虫任务已启动");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "启动爬虫任务失败: " + e.getMessage());
        }
        
        return result;
    }
}
