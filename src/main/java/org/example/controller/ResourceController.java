package org.example.controller;

import com.github.pagehelper.PageInfo;
import org.example.dto.ResourceRequest;
import org.example.dto.ResourceResponse;
import org.example.dto.SearchRequest;
import org.example.service.ResourceService;
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
     * 获取所有资源（分页）
     */
    @GetMapping("/page")
    public ResponseEntity<PageInfo<ResourceResponse>> getAllResourcesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("API调用：分页获取所有资源，页码: {}, 大小: {}", page, size);

        try {
            PageInfo<ResourceResponse> resources = resourceService.getAllResourcesWithPagination(page, size);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            logger.error("分页获取所有资源失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * 统一搜索资源接口（分页）
     * 支持多字段搜索、模糊匹配、分词搜索和类型过滤
     * 支持按名称和内容搜索（分词搜索仅搜索名称字段）
     * 支持按层级和内容类型过滤
     */
    @PostMapping("/search")
    public ResponseEntity<PageInfo<ResourceResponse>> searchResources(@Valid @RequestBody SearchRequest searchRequest) {
        logger.info("API调用：统一搜索资源，搜索词: {}, 页码: {}, 大小: {}, 层级: {}, 类型: {}",
                   searchRequest.getSearchTerm(), searchRequest.getPage(), searchRequest.getSize(),
                   searchRequest.getLevel(), searchRequest.getType());

        try {
            PageInfo<ResourceResponse> results = resourceService.searchResourcesWithPagination(searchRequest);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("搜索资源失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }






    /**
     * 根据层级获取资源（分页）- 使用统一搜索接口
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<PageInfo<ResourceResponse>> getResourcesByLevel(
            @PathVariable Integer level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("API调用：分页根据层级获取资源，层级: {}, 页码: {}, 大小: {}", level, page, size);

        try {
            // 使用统一搜索接口，空搜索词表示获取所有该层级的资源
            SearchRequest searchRequest = new SearchRequest("", page, size, level.toString(), null);
            PageInfo<ResourceResponse> resources = resourceService.searchResourcesWithPagination(searchRequest);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            logger.error("分页根据层级获取资源失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * 根据类型获取资源（分页）- 使用统一搜索接口
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<PageInfo<ResourceResponse>> getResourcesByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("API调用：分页根据类型获取资源，类型: {}, 页码: {}, 大小: {}", type, page, size);

        try {
            // 使用统一搜索接口，空搜索词表示获取所有该类型的资源
            SearchRequest searchRequest = new SearchRequest("", page, size, null, type);
            PageInfo<ResourceResponse> resources = resourceService.searchResourcesWithPagination(searchRequest);
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            logger.error("分页根据类型获取资源失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
