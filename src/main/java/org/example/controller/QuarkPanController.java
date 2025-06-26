package org.example.controller;

import org.example.service.QuarkPanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 夸克网盘控制器
 */
@RestController
@RequestMapping("/quark")
public class QuarkPanController {
    
    private static final Logger logger = LoggerFactory.getLogger(QuarkPanController.class);
    
    @Autowired
    private QuarkPanService quarkPanService;

    /**
     * 替换文本中的夸克网盘链接
     */
    @PostMapping("/replace-urls")
    public Map<String, Object> replaceUrls(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文本内容不能为空");
                return result;
            }
            
            logger.info("开始处理文本链接替换，文本长度: {}", text.length());
            
            // 提取原始链接标识符
            List<String> originalIds = quarkPanService.extractQuarkUrlIds(text);
            logger.info("提取到 {} 个夸克网盘链接", originalIds.size());
            
            if (originalIds.isEmpty()) {
                result.put("success", true);
                result.put("message", "文本中没有找到夸克网盘链接");
                result.put("originalText", text);
                result.put("updatedText", text);
                result.put("originalUrls", originalIds);
                return result;
            }
            
            // 替换链接
            String updatedText = quarkPanService.replaceQuarkUrls(text);
            
            result.put("success", true);
            result.put("message", "链接替换完成");
            result.put("originalText", text);
            result.put("updatedText", updatedText);
            result.put("originalUrls", originalIds);
            result.put("processedCount", originalIds.size());
            
            logger.info("文本链接替换完成，处理了 {} 个链接", originalIds.size());
            
        } catch (Exception e) {
            logger.error("替换链接时发生错误", e);
            result.put("success", false);
            result.put("message", "处理过程中发生错误: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 转换单个夸克网盘链接
     */
    @PostMapping("/convert-url")
    public Map<String, Object> convertUrl(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String url = request.get("url");
            if (url == null || url.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "链接不能为空");
                return result;
            }
            
            // 提取链接标识符
            List<String> urlIds = quarkPanService.extractQuarkUrlIds(url);
            if (urlIds.isEmpty()) {
                result.put("success", false);
                result.put("message", "不是有效的夸克网盘链接");
                return result;
            }
            
            String pwdId = urlIds.get(0);
            logger.info("开始转换单个链接，pwdId: {}", pwdId);
            
            String newUrl = quarkPanService.convertQuarkUrl(pwdId);
            
            if (newUrl != null) {
                result.put("success", true);
                result.put("message", "链接转换成功");
                result.put("originalUrl", url);
                result.put("newUrl", newUrl);
                result.put("pwdId", pwdId);
                
                logger.info("单个链接转换成功，pwdId: {}", pwdId);
            } else {
                result.put("success", false);
                result.put("message", "链接转换失败");
                result.put("originalUrl", url);
                result.put("pwdId", pwdId);
                
                logger.error("单个链接转换失败，pwdId: {}", pwdId);
            }
            
        } catch (Exception e) {
            logger.error("转换单个链接时发生错误", e);
            result.put("success", false);
            result.put("message", "处理过程中发生错误: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 提取文本中的夸克网盘链接
     */
    @PostMapping("/extract-urls")
    public Map<String, Object> extractUrls(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文本内容不能为空");
                return result;
            }
            
            List<String> urlIds = quarkPanService.extractQuarkUrlIds(text);
            
            result.put("success", true);
            result.put("message", "提取完成");
            result.put("urlIds", urlIds);
            result.put("count", urlIds.size());
            
            logger.info("提取夸克网盘链接完成，找到 {} 个链接", urlIds.size());
            
        } catch (Exception e) {
            logger.error("提取链接时发生错误", e);
            result.put("success", false);
            result.put("message", "处理过程中发生错误: " + e.getMessage());
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
        result.put("service", "QuarkPanService");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
