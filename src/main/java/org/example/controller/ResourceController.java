package org.example.controller;

import com.github.pagehelper.PageInfo;
import org.example.dto.ResourceRequest;
import org.example.dto.ResourceResponse;
import org.example.dto.SearchRequest;
import org.example.dto.SearchResponse;
import org.example.service.ResourceService;
import org.example.service.ResourceDeduplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/resources")
@CrossOrigin(origins = "*")
@Validated
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ResourceDeduplicationService deduplicationService;

    /**
     * 创建新资源
     */
    @PostMapping
    public ResponseEntity<ResourceResponse> createResource(@Valid @RequestBody ResourceRequest request) {
        logger.info("API调用：创建新资源，名称: {}", request.getName());
        
        try {
            ResourceResponse response = resourceService.createResource(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("创建资源失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据ID获取资源
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResourceById(@PathVariable Integer id) {
        logger.info("API调用：获取资源，ID: {}", id);
        
        try {
            Optional<ResourceResponse> response = resourceService.getResourceById(id);
            return response.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("获取资源失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新资源
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponse> updateResource(
            @PathVariable Integer id,
            @Valid @RequestBody ResourceRequest request) {
        logger.info("API调用：更新资源，ID: {}", id);
        
        try {
            Optional<ResourceResponse> response = resourceService.updateResource(id, request);
            return response.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("更新资源失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Integer id) {
        logger.info("API调用：删除资源，ID: {}", id);
        
        try {
            boolean deleted = resourceService.deleteResource(id);
            return deleted ? ResponseEntity.noContent().build() 
                          : ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("删除资源失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }



    /**
     * 统一搜索资源接口（分页）- 使用Lucene + IK Analyzer高级搜索
     * 支持多字段搜索、模糊匹配、分词搜索和类型过滤
     * 支持按名称和内容搜索（分词搜索仅搜索名称字段）
     * 支持按层级和内容类型过滤
     * 返回包含实际搜索词信息的SearchResponse
     */
    @PostMapping("/search")
    public ResponseEntity<SearchResponse> searchResources(@Valid @RequestBody SearchRequest searchRequest) {
        logger.info("API调用：Lucene + IK高级搜索，搜索词: {}, 页码: {}, 大小: {}, 层级: {}, 类型: {}",
                   searchRequest.getSearchTerm(), searchRequest.getPage(), searchRequest.getSize(),
                   searchRequest.getLevel(), searchRequest.getType());

        try {
            SearchResponse results = resourceService.searchResourcesWithSearchInfo(searchRequest);
            logger.info("搜索完成，实际搜索词: {}, 搜索策略: {}",
                       results.getActualSearchTerms(), results.getSearchStrategy());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("搜索资源失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 手动触发资源去重
     */
    @PostMapping("/deduplicate")
    public ResponseEntity<String> deduplicateResources() {
        logger.info("API调用：手动触发资源去重");

        try {
            // 先获取统计信息
            ResourceDeduplicationService.DeduplicationStats stats =
                deduplicationService.getDeduplicationStats();

            if (stats.getTotalDuplicateCount() == 0) {
                logger.info("未发现重复资源");
                return ResponseEntity.ok("未发现重复资源，无需去重");
            }

            logger.info("发现重复资源 - URL重复: {} 条, 名称重复: {} 条",
                       stats.getUrlDuplicateCount(), stats.getNameDuplicateCount());

            // 执行去重操作
            deduplicationService.deduplicateResources();

            String message = String.format("去重操作完成，删除了 %d 条重复资源", stats.getTotalDuplicateCount());
            logger.info(message);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            logger.error("手动去重操作失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("去重操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取重复资源统计信息
     */
    @GetMapping("/duplicate-stats")
    public ResponseEntity<ResourceDeduplicationService.DeduplicationStats> getDuplicateStats() {
        logger.info("API调用：获取重复资源统计信息");

        try {
            ResourceDeduplicationService.DeduplicationStats stats =
                deduplicationService.getDeduplicationStats();

            logger.info("重复资源统计 - URL重复: {} 条, 名称重复: {} 条",
                       stats.getUrlDuplicateCount(), stats.getNameDuplicateCount());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("获取重复资源统计信息失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
