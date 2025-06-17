package org.example.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.dto.ResourceRequest;
import org.example.dto.ResourceResponse;
import org.example.dto.SearchRequest;
import org.example.entity.Resource;
import org.example.mapper.ResourceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    @Autowired
    private ResourceMapper resourceMapper;

    /**
     * 创建新资源
     */
    public ResourceResponse createResource(ResourceRequest request) {
        logger.info("创建新资源，名称: {}", request.getName());

        Resource resource = new Resource(
                request.getName(),
                request.getContent(),
                request.getUrl(),
                request.getPig(),
                request.getLevel(),
                request.getType()
        );

        int result = resourceMapper.insert(resource);
        if (result > 0) {
            logger.info("资源创建成功，ID: {}", resource.getId());
            return convertToResponse(resource);
        } else {
            throw new RuntimeException("创建资源失败");
        }
    }

    /**
     * 根据ID获取资源
     */
    public Optional<ResourceResponse> getResourceById(Integer id) {
        logger.info("获取资源，ID: {}", id);

        Resource resource = resourceMapper.selectById(id);
        return resource != null ? Optional.of(convertToResponse(resource)) : Optional.empty();
    }

    /**
     * 更新资源
     */
    public Optional<ResourceResponse> updateResource(Integer id, ResourceRequest request) {
        logger.info("更新资源，ID: {}", id);

        Resource existingResource = resourceMapper.selectById(id);
        if (existingResource != null) {
            existingResource.setName(request.getName());
            existingResource.setContent(request.getContent());
            existingResource.setUrl(request.getUrl());
            existingResource.setPig(request.getPig());
            existingResource.setLevel(request.getLevel());
            existingResource.setType(request.getType());

            int result = resourceMapper.updateById(existingResource);
            if (result > 0) {
                logger.info("资源更新成功，ID: {}", existingResource.getId());
                return Optional.of(convertToResponse(existingResource));
            }
        }

        logger.warn("未找到资源，ID: {}", id);
        return Optional.empty();
    }

    /**
     * 删除资源
     */
    public boolean deleteResource(Integer id) {
        logger.info("删除资源，ID: {}", id);

        if (resourceMapper.existsById(id)) {
            int result = resourceMapper.deleteById(id);
            if (result > 0) {
                logger.info("资源删除成功，ID: {}", id);
                return true;
            }
        }

        logger.warn("未找到要删除的资源，ID: {}", id);
        return false;
    }

    /**
     * 获取所有资源（不分页）
     */
    public List<ResourceResponse> getAllResources() {
        logger.info("获取所有资源");

        List<Resource> resources = resourceMapper.selectAll();
        return resources.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有资源（分页）
     */
    public PageInfo<ResourceResponse> getAllResourcesWithPagination(int page, int size) {
        logger.info("分页获取所有资源，页码: {}, 大小: {}", page, size);

        PageHelper.startPage(page , size); // PageHelper页码从1开始
        List<Resource> resources = resourceMapper.selectAll();

        return convertToPageInfo(resources);
    }

    /**
     * 高级搜索功能：支持模糊匹配和分词搜索（不分页）
     * 例如：搜索"四六级答案"，如果没有完全匹配，会尝试匹配"四六级"、"答案"等分词
     */
    public List<ResourceResponse> searchResources(SearchRequest searchRequest) {
        logger.info("执行高级搜索，搜索词: {}", searchRequest.getSearchTerm());

        String searchTerm = searchRequest.getSearchTerm().trim();
        List<Resource> results = performAdvancedSearch(searchTerm, searchRequest.getLevel());

        return results.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 高级搜索功能：支持模糊匹配和分词搜索（分页）
     */
    public PageInfo<ResourceResponse> searchResourcesWithPagination(SearchRequest searchRequest) {
        logger.info("执行分页高级搜索，搜索词: {}, 页码: {}, 大小: {}",
                   searchRequest.getSearchTerm(), searchRequest.getPage(), searchRequest.getSize());

        String searchTerm = searchRequest.getSearchTerm().trim();

        // 使用PageHelper进行分页
        PageHelper.startPage(searchRequest.getPage() , searchRequest.getSize());
        List<Resource> results = performAdvancedSearch(searchTerm, searchRequest.getLevel());

        return convertToPageInfo(results);
    }

    /**
     * 执行高级搜索的核心逻辑
     */
    private List<Resource> performAdvancedSearch(String searchTerm, String levelFilter) {
        List<Resource> results = new ArrayList<>();

        try {
            // 首先尝试完整搜索词的高级搜索
            results = resourceMapper.selectByAdvancedSearch(searchTerm);

            // 如果没有结果且搜索词包含空格，尝试分词搜索
            if (results.isEmpty() && searchTerm.contains(" ")) {
                logger.info("完整搜索词无结果，尝试分词搜索");
                results = performTokenizedSearch(searchTerm);
            }

            // 如果仍然没有结果，尝试中文分词（简单实现）
            if (results.isEmpty() && searchTerm.length() > 1) {
                logger.info("尝试中文字符分词搜索");
                results = performChineseTokenizedSearch(searchTerm);
            }

            // 根据层级过滤
            if (levelFilter != null && !levelFilter.trim().isEmpty()) {
                results = results.stream()
                        .filter(resource -> resource.getLevel().equals(Integer.parseInt(levelFilter)))
                        .collect(Collectors.toList());
            }

            logger.info("搜索完成，找到 {} 个结果", results.size());

        } catch (Exception e) {
            logger.error("搜索过程中出现错误: {}", e.getMessage(), e);
            // 降级到简单搜索
            results = resourceMapper.selectByNameOrContentLike(searchTerm, searchTerm);
        }

        return results;
    }



    /**
     * 根据层级获取资源（不分页）
     */
    public List<ResourceResponse> getResourcesByLevel(Integer level) {
        logger.info("获取层级 {} 的资源", level);

        List<Resource> resources = resourceMapper.selectByLevel(level);
        return resources.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据层级获取资源（分页）
     */
    public PageInfo<ResourceResponse> getResourcesByLevelWithPagination(Integer level, int page, int size) {
        logger.info("分页获取层级 {} 的资源，页码: {}, 大小: {}", level, page, size);

        PageHelper.startPage(page , size);
        List<Resource> resources = resourceMapper.selectByLevel(level);

        return convertToPageInfo(resources);
    }

    /**
     * 根据类型获取资源（不分页）
     */
    public List<ResourceResponse> getResourcesByType(String type) {
        logger.info("获取类型 {} 的资源", type);

        List<Resource> resources = resourceMapper.selectByType(type);
        return resources.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据类型获取资源（分页）
     */
    public PageInfo<ResourceResponse> getResourcesByTypeWithPagination(String type, int page, int size) {
        logger.info("分页获取类型 {} 的资源，页码: {}, 大小: {}", type, page, size);

        PageHelper.startPage(page , size);
        List<Resource> resources = resourceMapper.selectByType(type);

        return convertToPageInfo(resources);
    }

    /**
     * 执行分词搜索（英文空格分词）
     */
    private List<Resource> performTokenizedSearch(String searchTerm) {
        String[] terms = searchTerm.split("\\s+");
        List<Resource> results = new ArrayList<>();

        // 使用多个关键词搜索
        if (terms.length >= 1) {
            String term1 = terms.length > 0 ? terms[0] : null;
            String term2 = terms.length > 1 ? terms[1] : null;
            String term3 = terms.length > 2 ? terms[2] : null;

            results = resourceMapper.selectByMultipleTerms(term1, term2, term3);
        }

        // 如果多词搜索无结果，尝试单个词搜索
        if (results.isEmpty()) {
            for (String term : terms) {
                if (term.trim().length() > 0) {
                    List<Resource> termResults = resourceMapper.selectByAdvancedSearch(term.trim());
                    results.addAll(termResults);
                }
            }
            // 去重
            results = results.stream().distinct().collect(Collectors.toList());
        }

        return results;
    }

    /**
     * 执行中文分词搜索（简单实现）
     */
    private List<Resource> performChineseTokenizedSearch(String searchTerm) {
        List<Resource> results = new ArrayList<>();

        // 简单的中文分词：按2-3个字符分组
        if (searchTerm.length() >= 4) {
            // 尝试2字分词
            for (int i = 0; i <= searchTerm.length() - 2; i++) {
                String subTerm = searchTerm.substring(i, i + 2);
                List<Resource> subResults = resourceMapper.selectByAdvancedSearch(subTerm);
                results.addAll(subResults);
            }

            // 尝试3字分词
            for (int i = 0; i <= searchTerm.length() - 3; i++) {
                String subTerm = searchTerm.substring(i, i + 3);
                List<Resource> subResults = resourceMapper.selectByAdvancedSearch(subTerm);
                results.addAll(subResults);
            }
        }

        // 去重
        return results.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 转换为分页信息对象
     */
    private PageInfo<ResourceResponse> convertToPageInfo(List<Resource> resources) {
        PageInfo<Resource> pageInfo = new PageInfo<>(resources);

        // 转换为响应对象
        List<ResourceResponse> responseList = resources.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        PageInfo<ResourceResponse> responsePageInfo = new PageInfo<>();
        responsePageInfo.setList(responseList);
        responsePageInfo.setTotal(pageInfo.getTotal());
        responsePageInfo.setPageNum(pageInfo.getPageNum());
        responsePageInfo.setPageSize(pageInfo.getPageSize());
        responsePageInfo.setPages(pageInfo.getPages());
        responsePageInfo.setHasNextPage(pageInfo.isHasNextPage());
        responsePageInfo.setHasPreviousPage(pageInfo.isHasPreviousPage());
        responsePageInfo.setIsFirstPage(pageInfo.isIsFirstPage());
        responsePageInfo.setIsLastPage(pageInfo.isIsLastPage());
        responsePageInfo.setNavigatePages(pageInfo.getNavigatePages());
        responsePageInfo.setNavigatepageNums(pageInfo.getNavigatepageNums());
        responsePageInfo.setNavigateFirstPage(pageInfo.getNavigateFirstPage());
        responsePageInfo.setNavigateLastPage(pageInfo.getNavigateLastPage());

        return responsePageInfo;
    }

    /**
     * 转换为响应对象
     */
    private ResourceResponse convertToResponse(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getContent(),
                resource.getUrl(),
                resource.getPig(),
                resource.getLevel(),
                resource.getType(),
                resource.getCreateTime(),
                resource.getUpdateTime()
        );
    }
}
