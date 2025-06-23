package org.example.service;

import org.example.mapper.ResourceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 资源去重服务
 * 负责清理重复的资源数据
 */
@Service
public class ResourceDeduplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceDeduplicationService.class);

    @Autowired
    private ResourceMapper resourceMapper;

    /**
     * 执行资源去重操作
     * 删除URL相同或name相同的重复资源，保留resourceTime最新的一条
     */
    @Transactional
    public void deduplicateResources() {
        logger.info("开始执行资源去重操作...");

        try {
            // 去重URL相同的资源
            int urlDuplicatesRemoved = deduplicateByUrl();
            
            // 去重名称相同的资源
            int nameDuplicatesRemoved = deduplicateByName();

            logger.info("资源去重操作完成，删除URL重复资源: {} 条，删除名称重复资源: {} 条", 
                       urlDuplicatesRemoved, nameDuplicatesRemoved);

        } catch (Exception e) {
            logger.error("资源去重操作失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 去重URL相同的资源
     */
    private int deduplicateByUrl() {
        logger.info("开始去重URL相同的资源...");
        
        List<Integer> duplicateIds = resourceMapper.findDuplicateIdsByUrl();
        
        if (duplicateIds.isEmpty()) {
            logger.info("未发现URL重复的资源");
            return 0;
        }

        logger.info("发现 {} 条URL重复的资源，准备删除", duplicateIds.size());
        
        // 批量删除重复资源
        int deletedCount = resourceMapper.deleteByIds(duplicateIds);
        
        logger.info("成功删除 {} 条URL重复的资源", deletedCount);
        return deletedCount;
    }

    /**
     * 去重名称相同的资源
     */
    private int deduplicateByName() {
        logger.info("开始去重名称相同的资源...");
        
        List<Integer> duplicateIds = resourceMapper.findDuplicateIdsByName();
        
        if (duplicateIds.isEmpty()) {
            logger.info("未发现名称重复的资源");
            return 0;
        }

        logger.info("发现 {} 条名称重复的资源，准备删除", duplicateIds.size());
        
        // 批量删除重复资源
        int deletedCount = resourceMapper.deleteByIds(duplicateIds);
        
        logger.info("成功删除 {} 条名称重复的资源", deletedCount);
        return deletedCount;
    }

    /**
     * 获取去重统计信息
     */
    public DeduplicationStats getDeduplicationStats() {
        List<Integer> urlDuplicates = resourceMapper.findDuplicateIdsByUrl();
        List<Integer> nameDuplicates = resourceMapper.findDuplicateIdsByName();
        
        return new DeduplicationStats(urlDuplicates.size(), nameDuplicates.size());
    }

    /**
     * 去重统计信息
     */
    public static class DeduplicationStats {
        private final int urlDuplicateCount;
        private final int nameDuplicateCount;

        public DeduplicationStats(int urlDuplicateCount, int nameDuplicateCount) {
            this.urlDuplicateCount = urlDuplicateCount;
            this.nameDuplicateCount = nameDuplicateCount;
        }

        public int getUrlDuplicateCount() {
            return urlDuplicateCount;
        }

        public int getNameDuplicateCount() {
            return nameDuplicateCount;
        }

        public int getTotalDuplicateCount() {
            return urlDuplicateCount + nameDuplicateCount;
        }
    }
}
