package org.example.service;

import org.example.entity.Resource;
import org.example.mapper.ResourceMapper;
import org.example.util.TextSimilarityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
     * 删除URL相同、name相同、或相似度超过阈值的重复资源，保留resourceTime最新的一条
     */
    @Transactional
    public void deduplicateResources() {
        logger.info("开始执行资源去重操作...");

        try {
            // 去重URL相同的资源
            int urlDuplicatesRemoved = deduplicateByUrl();

            // 去重名称相同的资源
            int nameDuplicatesRemoved = deduplicateByName();

            // 去重相似度超过阈值的资源
            //int similarityDuplicatesRemoved = deduplicateBySimilarity();

            logger.info("资源去重操作完成，删除URL重复: {} 条，删除名称重复: {} 条，删除相似度重复: {} 条",
                       urlDuplicatesRemoved, nameDuplicatesRemoved, 0);

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
        int similarityDuplicates = estimateSimilarityDuplicates();

        return new DeduplicationStats(urlDuplicates.size(), nameDuplicates.size(), similarityDuplicates);
    }

    /**
     * 估算相似度重复资源数量（采样检测，避免全量计算）
     */
    private int estimateSimilarityDuplicates() {
        try {
            // 获取最近的1000条资源进行采样检测
            List<Resource> sampleResources = resourceMapper.selectAllForSimilarityCheck();
            if (sampleResources.size() > 1000) {
                sampleResources = sampleResources.subList(0, 1000);
            }

            if (sampleResources.size() < 2) {
                return 0;
            }

            Set<Integer> duplicateIds = new HashSet<>();

            // 只检测前100个资源与其他资源的相似度
            int checkLimit = Math.min(100, sampleResources.size());
            for (int i = 0; i < checkLimit; i++) {
                Resource resource1 = sampleResources.get(i);

                if (duplicateIds.contains(resource1.getId())) {
                    continue;
                }

                for (int j = i + 1; j < sampleResources.size(); j++) {
                    Resource resource2 = sampleResources.get(j);

                    if (duplicateIds.contains(resource2.getId())) {
                        continue;
                    }

                    if (isSimilarResource(resource1, resource2)) {
                        Resource toDelete = selectResourceToDelete(resource1, resource2);
                        duplicateIds.add(toDelete.getId());
                    }
                }
            }

            return duplicateIds.size();

        } catch (Exception e) {
            logger.warn("估算相似度重复资源数量失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 去重相似度超过阈值的资源
     */
    private int deduplicateBySimilarity() {
        logger.info("开始去重相似度超过阈值的资源...");

        // 获取所有资源进行相似度检测
        List<Resource> allResources = resourceMapper.selectAllForSimilarityCheck();

        if (allResources.size() < 2) {
            logger.info("资源数量不足，跳过相似度去重");
            return 0;
        }

        Set<Integer> duplicateIds = new HashSet<>();
        int totalComparisons = 0;

        // 使用双重循环进行相似度比较
        for (int i = 0; i < allResources.size(); i++) {
            Resource resource1 = allResources.get(i);

            // 如果当前资源已被标记为重复，跳过
            if (duplicateIds.contains(resource1.getId())) {
                continue;
            }

            for (int j = i + 1; j < allResources.size(); j++) {
                Resource resource2 = allResources.get(j);
                totalComparisons++;

                // 如果resource2已被标记为重复，跳过
                if (duplicateIds.contains(resource2.getId())) {
                    continue;
                }

                // 检查相似度
                if (isSimilarResource(resource1, resource2)) {
                    // 保留resourceTime更新的资源，删除较旧的
                    Resource toDelete = selectResourceToDelete(resource1, resource2);
                    duplicateIds.add(toDelete.getId());

                    logger.debug("发现相似资源: [{}] {} 与 [{}] {}",
                               resource1.getId(), resource1.getName(),
                               resource2.getId(), resource2.getName());
                }
            }

            // 每处理1000个资源输出一次进度
            if ((i + 1) % 1000 == 0) {
                logger.info("相似度检测进度: {}/{}, 已发现重复: {} 条",
                           i + 1, allResources.size(), duplicateIds.size());
            }
        }

        logger.info("相似度检测完成，总比较次数: {}, 发现重复资源: {} 条", totalComparisons, duplicateIds.size());

        if (duplicateIds.isEmpty()) {
            logger.info("未发现相似度重复的资源");
            return 0;
        }

        // 批量删除重复资源
        List<Integer> duplicateIdList = new ArrayList<>(duplicateIds);
        int deletedCount = resourceMapper.deleteByIds(duplicateIdList);

        logger.info("成功删除 {} 条相似度重复的资源", deletedCount);
        return deletedCount;
    }

    /**
     * 判断两个资源是否相似
     * name相似度 > 90% 或 content相似度 > 50%
     */
    private boolean isSimilarResource(Resource resource1, Resource resource2) {
        // 检查name相似度
        String name1 = resource1.getName();
        String name2 = resource2.getName();

        if (name1 != null && name2 != null) {
            double nameSimilarity = TextSimilarityUtil.calculateComprehensiveSimilarity(name1, name2);
            if (nameSimilarity > 0.9) {
                logger.debug("名称相似度: {:.3f} ({})", nameSimilarity, "超过90%阈值");
                return true;
            }
        }

        // 检查content相似度
        String content1 = resource1.getContent();
        String content2 = resource2.getContent();

        if (content1 != null && content2 != null &&
            content1.trim().length() > 10 && content2.trim().length() > 10) {
            double contentSimilarity = TextSimilarityUtil.calculateComprehensiveSimilarity(content1, content2);
            if (contentSimilarity > 0.5) {
                logger.debug("内容相似度: {:.3f} ({})", contentSimilarity, "超过50%阈值");
                return true;
            }
        }

        return false;
    }

    /**
     * 选择要删除的资源（保留resourceTime更新的）
     */
    private Resource selectResourceToDelete(Resource resource1, Resource resource2) {
        Integer time1 = resource1.getResourceTime();
        Integer time2 = resource2.getResourceTime();

        // 如果resourceTime相同或为空，保留ID较大的（通常是后插入的）
        if (time1 == null && time2 == null) {
            return resource1.getId() < resource2.getId() ? resource1 : resource2;
        }

        if (time1 == null) {
            return resource1; // 删除时间为空的
        }

        if (time2 == null) {
            return resource2; // 删除时间为空的
        }

        // 删除resourceTime较小的（较旧的）
        if (time1.equals(time2)) {
            return resource1.getId() < resource2.getId() ? resource1 : resource2;
        }

        return time1 < time2 ? resource1 : resource2;
    }

    /**
     * 去重统计信息
     */
    public static class DeduplicationStats {
        private final int urlDuplicateCount;
        private final int nameDuplicateCount;
        private final int similarityDuplicateCount;

        public DeduplicationStats(int urlDuplicateCount, int nameDuplicateCount, int similarityDuplicateCount) {
            this.urlDuplicateCount = urlDuplicateCount;
            this.nameDuplicateCount = nameDuplicateCount;
            this.similarityDuplicateCount = similarityDuplicateCount;
        }

        public int getUrlDuplicateCount() {
            return urlDuplicateCount;
        }

        public int getNameDuplicateCount() {
            return nameDuplicateCount;
        }

        public int getSimilarityDuplicateCount() {
            return similarityDuplicateCount;
        }

        public int getTotalDuplicateCount() {
            return urlDuplicateCount + nameDuplicateCount + similarityDuplicateCount;
        }
    }
}
