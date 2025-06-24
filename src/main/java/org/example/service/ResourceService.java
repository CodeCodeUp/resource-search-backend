package org.example.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.example.dto.ResourceRequest;
import org.example.dto.ResourceResponse;
import org.example.dto.SearchRequest;
import org.example.dto.SearchResponse;
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

    @Autowired
    private LuceneSearchService luceneSearchService;

    /**
     * 内部类：搜索结果和实际搜索词的包装
     */
    private static class SearchResultWithTerms {
        private List<Resource> results;
        private PageInfo<ResourceResponse> pageInfo;
        private List<String> actualSearchTerms;
        private String searchStrategy;

        public SearchResultWithTerms(List<Resource> results, List<String> actualSearchTerms, String searchStrategy) {
            this.results = results;
            this.actualSearchTerms = actualSearchTerms;
            this.searchStrategy = searchStrategy;
        }

        public SearchResultWithTerms(PageInfo<ResourceResponse> pageInfo, List<String> actualSearchTerms, String searchStrategy) {
            this.pageInfo = pageInfo;
            this.actualSearchTerms = actualSearchTerms;
            this.searchStrategy = searchStrategy;
        }

        public List<Resource> getResults() { return results; }
        public PageInfo<ResourceResponse> getPageInfo() { return pageInfo; }
        public List<String> getActualSearchTerms() { return actualSearchTerms; }
        public String getSearchStrategy() { return searchStrategy; }
    }

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
                request.getType(),
                request.getSource()
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
            existingResource.setSource(request.getSource());
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
     * 统一搜索功能：支持IK分词器+数据库查询、多字段搜索、模糊匹配、分词搜索和类型过滤（分页）
     * 使用IK分词器进行分词，然后直接到数据库查询
     * 支持按名称和内容搜索（分词搜索仅搜索名称字段）
     * 支持按层级和内容类型过滤（数据库层面实现）
     * 支持搜索模式：multi（名称+内容）或 name（仅名称）
     * 返回包含实际搜索词信息的SearchResponse
     */
    public SearchResponse searchResourcesWithSearchInfo(SearchRequest searchRequest) {
        logger.info("执行分页统一搜索（IK分词+数据库），搜索词: {}, 页码: {}, 大小: {}, 层级: {}, 类型: {}, 搜索模式: {}",
                   searchRequest.getSearchTerm(), searchRequest.getPage(), searchRequest.getSize(),
                   searchRequest.getLevel(), searchRequest.getType(), searchRequest.getSearchMode());

        String searchTerm = searchRequest.getSearchTerm() != null ? searchRequest.getSearchTerm().trim() : "";
        String searchMode = searchRequest.getSearchMode() != null ? searchRequest.getSearchMode() : "multi";
        Integer level = parseLevel(searchRequest.getLevel());
        String type = searchRequest.getType();

        // 执行搜索并收集实际使用的搜索词
        SearchResultWithTerms searchResult = performAdvancedSearchWithTerms(searchTerm, level, type, searchMode,
                                                                           searchRequest.getPage(), searchRequest.getSize());

        // 构建SearchResponse
        return new SearchResponse(
            searchResult.getPageInfo(),
            searchTerm, // 原始搜索词
            searchResult.getActualSearchTerms(), // 实际使用的搜索词
            searchResult.getSearchStrategy(), // 搜索策略
            searchMode, // 搜索模式
            level, // 层级过滤
            type // 类型过滤
        );
    }

    /**
     * 保持向后兼容的方法
     */
    public PageInfo<ResourceResponse> searchResourcesWithPagination(SearchRequest searchRequest) {
        SearchResponse searchResponse = searchResourcesWithSearchInfo(searchRequest);
        return searchResponse.getPageInfo();
    }

    /**
     * 执行高级搜索的核心逻辑（使用IK分词器+数据库查询）
     * 支持层级和类型过滤，支持搜索模式选择，支持分页
     * 返回搜索结果和实际使用的搜索词
     */
    private SearchResultWithTerms performAdvancedSearchWithTerms(String searchTerm, Integer level, String type,
                                                               String searchMode, int page, int size) {
        // 使用PageHelper进行分页
        PageHelper.startPage(page, size);

        // 执行IK分词+数据库搜索
        SearchResultWithTerms searchResult = performTokenizedDatabaseSearch(searchTerm, level, type, searchMode);

        // 转换为分页信息
        PageInfo<ResourceResponse> pageInfo = convertToPageInfo(searchResult.getResults());

        return new SearchResultWithTerms(pageInfo, searchResult.getActualSearchTerms(), searchResult.getSearchStrategy());
    }

    /**
     * 使用IK分词器+数据库查询进行搜索
     */
    private SearchResultWithTerms performTokenizedDatabaseSearch(String searchTerm, Integer level, String type, String searchMode) {
        List<String> allSearchTerms = new ArrayList<>();

        // 添加原始搜索词
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            allSearchTerms.add(searchTerm.trim());
        }

        // 使用IK分词器获取分词结果
        try {
            List<String> tokens = luceneSearchService.analyzeText(searchTerm);
            for (String token : tokens) {
                if (!token.equals(searchTerm) && !allSearchTerms.contains(token)) {
                    allSearchTerms.add(token);
                }
            }
            logger.info("IK分词结果: {}", tokens);
        } catch (Exception e) {
            logger.warn("IK分词失败: {}", e.getMessage());
        }

        // 一次性查询所有搜索词的结果
        List<Resource> results = resourceMapper.selectByCombinedTermsSearch(allSearchTerms, level, type, searchMode);

        logger.info("组合搜索完成，搜索词: {}, 找到 {} 个结果", allSearchTerms, results.size());

        return new SearchResultWithTerms(results, allSearchTerms, "ik_tokenized");
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
     * 转换为分页信息对象
     */
    private PageInfo<ResourceResponse> convertToPageInfo(List<Resource> resources) {
        PageInfo<Resource> pageInfo = new PageInfo<>(resources);

        // 转换为响应对象（搜索结果不包含URL）
        List<ResourceResponse> responseList = resources.stream()
                .map(this::convertToSearchResponse)
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
     * 转换为响应对象（包含URL，用于详情查看）
     */
    private ResourceResponse convertToResponse(Resource resource) {
        return convertToResponse(resource, true);
    }

    /**
     * 转换为响应对象（用于搜索，不包含URL）
     */
    private ResourceResponse convertToSearchResponse(Resource resource) {
        return convertToResponse(resource, false);
    }

    /**
     * 转换为响应对象的通用方法
     * @param resource 资源对象
     * @param includeUrl 是否包含URL
     */
    private ResourceResponse convertToResponse(Resource resource, boolean includeUrl) {
        ResourceResponse response = new ResourceResponse();
        response.setId(resource.getId());
        response.setName(resource.getName());
        response.setContent(resource.getContent());
        response.setPig(resource.getPig());
        response.setLevel(resource.getLevel());
        response.setType(resource.getType());
        response.setSource(resource.getSource());
        response.setResourceTime(resource.getResourceTime());
        response.setCreateTime(resource.getCreateTime());
        response.setUpdateTime(resource.getUpdateTime());

        // 只有在需要时才设置URL
        if (includeUrl) {
            response.setUrl(resource.getUrl());
        }
        // 如果不包含URL，则URL字段为null，前端可以通过验证系统获取

        return response;
    }
}
