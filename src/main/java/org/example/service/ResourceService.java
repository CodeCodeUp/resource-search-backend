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
        resource.setResourceTime(request.getResourceTime());

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
            existingResource.setResourceTime(request.getResourceTime());

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
     * 获取所有资源（分页）
     */
    public PageInfo<ResourceResponse> getAllResourcesWithPagination(int page, int size) {
        logger.info("分页获取所有资源，页码: {}, 大小: {}", page, size);

        PageHelper.startPage(page , size); // PageHelper页码从1开始
        List<Resource> resources = resourceMapper.selectAll();

        return convertToPageInfo(resources);
    }



    /**
     * 统一搜索功能：支持多字段搜索、模糊匹配、分词搜索和类型过滤（分页）
     * 支持按名称和内容搜索（分词搜索仅搜索名称字段）
     * 支持按层级和内容类型过滤（数据库层面实现）
     * 支持搜索模式：multi（名称+内容）或 name（仅名称）
     */
    public PageInfo<ResourceResponse> searchResourcesWithPagination(SearchRequest searchRequest) {
        logger.info("执行分页统一搜索，搜索词: {}, 页码: {}, 大小: {}, 层级: {}, 类型: {}, 搜索模式: {}",
                   searchRequest.getSearchTerm(), searchRequest.getPage(), searchRequest.getSize(),
                   searchRequest.getLevel(), searchRequest.getType(), searchRequest.getSearchMode());

        String searchTerm = searchRequest.getSearchTerm() != null ? searchRequest.getSearchTerm().trim() : "";
        String searchMode = searchRequest.getSearchMode() != null ? searchRequest.getSearchMode() : "multi";
        Integer level = parseLevel(searchRequest.getLevel());
        String type = searchRequest.getType();

        // 使用PageHelper进行分页
        PageHelper.startPage(searchRequest.getPage(), searchRequest.getSize());
        List<Resource> results = performUnifiedSearch(searchTerm, level, type, searchMode);

        return convertToPageInfo(results);
    }

    /**
     * 执行统一搜索的核心逻辑（数据库层面实现过滤）
     * 支持层级和类型过滤，支持搜索模式选择
     */
    private List<Resource> performUnifiedSearch(String searchTerm, Integer level, String type, String searchMode) {
        List<Resource> results = new ArrayList<>();

        try {
            // 首先尝试完整搜索词的统一搜索
            results = resourceMapper.selectByUnifiedSearch(searchTerm, level, type, searchMode);

            // 如果没有结果且搜索词包含空格，尝试分词搜索
            if (results.isEmpty() && searchTerm != null && searchTerm.contains(" ")) {
                logger.info("完整搜索词无结果，尝试分词搜索");
                results = performUnifiedTokenizedSearch(searchTerm, level, type, searchMode);
            }

            // 如果仍然没有结果，尝试中文分词（简单实现）
            if (results.isEmpty() && searchTerm != null && searchTerm.length() > 1) {
                logger.info("尝试中文字符分词搜索");
                results = performUnifiedChineseTokenizedSearch(searchTerm, level, type, searchMode);
            }

            logger.info("统一搜索完成，找到 {} 个结果", results.size());

        } catch (Exception e) {
            logger.error("统一搜索过程中出现错误: {}", e.getMessage(), e);
            // 降级到简单搜索
            results = resourceMapper.selectByUnifiedSearch(searchTerm, level, type, searchMode);
        }

        return results;
    }







    /**
     * 解析层级字符串为整数
     */
    private Integer parseLevel(String levelStr) {
        if (levelStr == null || levelStr.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(levelStr.trim());
        } catch (NumberFormatException e) {
            logger.warn("无效的层级参数: {}", levelStr);
            return null;
        }
    }

    /**
     * 执行统一分词搜索（英文空格分词）
     */
    private List<Resource> performUnifiedTokenizedSearch(String searchTerm, Integer level, String type, String searchMode) {
        String[] terms = searchTerm.split("\\s+");
        List<Resource> results = new ArrayList<>();

        // 使用多个关键词搜索
        if (terms.length >= 1) {
            String term1 = terms.length > 0 ? terms[0] : null;
            String term2 = terms.length > 1 ? terms[1] : null;
            String term3 = terms.length > 2 ? terms[2] : null;

            results = resourceMapper.selectByUnifiedMultipleTermsSearch(term1, term2, term3, level, type, searchMode);
        }

        // 如果多词搜索无结果，尝试单个词搜索
        if (results.isEmpty()) {
            for (String term : terms) {
                if (!term.trim().isEmpty()) {
                    List<Resource> termResults = resourceMapper.selectByUnifiedSearch(term.trim(), level, type, searchMode);
                    results.addAll(termResults);
                }
            }
            // 去重
            results = results.stream().distinct().collect(Collectors.toList());
        }

        return results;
    }

    /**
     * 执行统一中文分词搜索（简单实现）
     */
    private List<Resource> performUnifiedChineseTokenizedSearch(String searchTerm, Integer level, String type, String searchMode) {
        List<Resource> results = new ArrayList<>();

        // 简单的中文分词：按2-3个字符分组
        if (searchTerm.length() >= 4) {
            // 尝试2字分词
            for (int i = 0; i <= searchTerm.length() - 2; i++) {
                String subTerm = searchTerm.substring(i, i + 2);
                List<Resource> subResults = resourceMapper.selectByUnifiedSearch(subTerm, level, type, searchMode);
                results.addAll(subResults);
            }

            // 尝试3字分词
            for (int i = 0; i <= searchTerm.length() - 3; i++) {
                String subTerm = searchTerm.substring(i, i + 3);
                List<Resource> subResults = resourceMapper.selectByUnifiedSearch(subTerm, level, type, searchMode);
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
                resource.getResourceTime(),
                resource.getCreateTime(),
                resource.getUpdateTime()
        );
    }
}
