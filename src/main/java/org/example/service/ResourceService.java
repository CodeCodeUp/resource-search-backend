package org.example.service;

import com.github.pagehelper.Page;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    @Autowired
    private ResourceMapper resourceMapper;

    /**
     * 内部类：搜索结果和实际搜索词的包装
     */
    private static class SearchResultWithTerms {
        private List<Resource> results;
        private List<String> actualSearchTerms;
        private String searchStrategy;

        public SearchResultWithTerms(List<Resource> results, List<String> actualSearchTerms, String searchStrategy) {
            this.results = results;
            this.actualSearchTerms = actualSearchTerms;
            this.searchStrategy = searchStrategy;
        }

        public List<Resource> getResults() { return results; }
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
     * 统一搜索功能：支持多字段搜索、模糊匹配、分词搜索和类型过滤（分页）
     * 支持按名称和内容搜索（分词搜索仅搜索名称字段）
     * 支持按层级和内容类型过滤（数据库层面实现）
     * 支持搜索模式：multi（名称+内容）或 name（仅名称）
     * 返回包含实际搜索词信息的SearchResponse
     */
    public SearchResponse searchResourcesWithSearchInfo(SearchRequest searchRequest) {
        logger.info("执行分页统一搜索，搜索词: {}, 页码: {}, 大小: {}, 层级: {}, 类型: {}, 搜索模式: {}",
                   searchRequest.getSearchTerm(), searchRequest.getPage(), searchRequest.getSize(),
                   searchRequest.getLevel(), searchRequest.getType(), searchRequest.getSearchMode());

        String searchTerm = searchRequest.getSearchTerm() != null ? searchRequest.getSearchTerm().trim() : "";
        String searchMode = searchRequest.getSearchMode() != null ? searchRequest.getSearchMode() : "multi";
        Integer level = parseLevel(searchRequest.getLevel());
        String type = searchRequest.getType();

        // 使用PageHelper进行分页
        PageHelper.startPage(searchRequest.getPage(), searchRequest.getSize());

        // 执行搜索并收集实际使用的搜索词
        SearchResultWithTerms searchResult = performUnifiedSearchWithTerms(searchTerm, level, type, searchMode);

        // 转换为分页信息
        PageInfo<ResourceResponse> pageInfo = convertToPageInfo(searchResult.getResults());

        // 构建SearchResponse
        return new SearchResponse(
            pageInfo,
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
     * 执行统一搜索的核心逻辑（数据库层面实现过滤）
     * 支持层级和类型过滤，支持搜索模式选择
     * 返回搜索结果和实际使用的搜索词
     */
    private SearchResultWithTerms performUnifiedSearchWithTerms(String searchTerm, Integer level, String type, String searchMode) {
        List<Resource> results = new ArrayList<>();
        List<String> actualSearchTerms = new ArrayList<>();
        String searchStrategy = "complete";

        try {
            // 首先尝试完整搜索词的统一搜索
            results = resourceMapper.selectByUnifiedSearch(searchTerm, level, type, searchMode);
            if (!results.isEmpty()) {
                actualSearchTerms.add(searchTerm);
                searchStrategy = "complete";
                logger.info("完整搜索词找到 {} 个结果", results.size());
            }

            // 如果没有结果且搜索词包含空格，尝试分词搜索
            if (results.isEmpty() && searchTerm != null && searchTerm.contains(" ")) {
                logger.info("完整搜索词无结果，尝试分词搜索");
                SearchResultWithTerms tokenizedResult = performUnifiedTokenizedSearchWithTerms(searchTerm, level, type, searchMode);
                results = tokenizedResult.getResults();
                actualSearchTerms = tokenizedResult.getActualSearchTerms();
                searchStrategy = "tokenized";
            }

            // 如果仍然没有结果，尝试中文分词（简单实现）
            if (results.isEmpty() && searchTerm != null && searchTerm.length() > 1) {
                logger.info("尝试中文字符分词搜索");
                SearchResultWithTerms chineseResult = performUnifiedChineseTokenizedSearchWithTerms(searchTerm, level, type, searchMode);
                results = chineseResult.getResults();
                actualSearchTerms = chineseResult.getActualSearchTerms();
                searchStrategy = "chinese_tokenized";
            }

            logger.info("统一搜索完成，找到 {} 个结果，实际搜索词: {}", results.size(), actualSearchTerms);

        } catch (Exception e) {
            logger.error("统一搜索过程中出现错误: {}", e.getMessage(), e);
            // 降级到简单搜索
            results = resourceMapper.selectByUnifiedSearch(searchTerm, level, type, searchMode);
            actualSearchTerms.add(searchTerm);
            searchStrategy = "fallback";
        }

        return new SearchResultWithTerms(results, actualSearchTerms, searchStrategy);
    }

    /**
     * 保持向后兼容的方法
     */
    private List<Resource> performUnifiedSearch(String searchTerm, Integer level, String type, String searchMode) {
        SearchResultWithTerms result = performUnifiedSearchWithTerms(searchTerm, level, type, searchMode);
        return result.getResults();
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
     * 执行统一分词搜索（英文空格分词）- 带搜索词跟踪
     */
    private SearchResultWithTerms performUnifiedTokenizedSearchWithTerms(String searchTerm, Integer level, String type, String searchMode) {
        String[] terms = searchTerm.split("\\s+");
        List<Resource> results = new ArrayList<>();
        List<String> actualSearchTerms = new ArrayList<>();

        // 使用多个关键词搜索
        if (terms.length >= 1) {
            String term1 = terms.length > 0 ? terms[0] : null;
            String term2 = terms.length > 1 ? terms[1] : null;
            String term3 = terms.length > 2 ? terms[2] : null;

            results = resourceMapper.selectByUnifiedMultipleTermsSearch(term1, term2, term3, level, type, searchMode);

            if (!results.isEmpty()) {
                // 记录实际使用的多词搜索
                if (term1 != null) actualSearchTerms.add(term1);
                if (term2 != null) actualSearchTerms.add(term2);
                if (term3 != null) actualSearchTerms.add(term3);
            }
        }

        // 如果多词搜索无结果，尝试单个词搜索
        if (results.isEmpty()) {
            for (String term : terms) {
                if (!term.trim().isEmpty()) {
                    List<Resource> termResults = resourceMapper.selectByUnifiedSearch(term.trim(), level, type, searchMode);
                    if (!termResults.isEmpty()) {
                        results.addAll(termResults);
                        actualSearchTerms.add(term.trim());
                    }
                }
            }
            // 去重
            results = results.stream().distinct().collect(Collectors.toList());
        }

        return new SearchResultWithTerms(results, actualSearchTerms, "tokenized");
    }

    /**
     * 保持向后兼容的方法
     */
    private List<Resource> performUnifiedTokenizedSearch(String searchTerm, Integer level, String type, String searchMode) {
        SearchResultWithTerms result = performUnifiedTokenizedSearchWithTerms(searchTerm, level, type, searchMode);
        return result.getResults();
    }

    /**
     * 执行统一中文分词搜索（简单实现）- 带搜索词跟踪
     */
    private SearchResultWithTerms performUnifiedChineseTokenizedSearchWithTerms(String searchTerm, Integer level, String type, String searchMode) {
        List<Resource> results = new ArrayList<>();
        List<String> actualSearchTerms = new ArrayList<>();

        // 简单的中文分词：按2-3个字符分组
        if (searchTerm.length() >= 4) {
            // 尝试2字分词
            for (int i = 0; i <= searchTerm.length() - 2; i++) {
                String subTerm = searchTerm.substring(i, i + 2);
                List<Resource> subResults = resourceMapper.selectByUnifiedSearch(subTerm, level, type, searchMode);
                if (!subResults.isEmpty()) {
                    results.addAll(subResults);
                    actualSearchTerms.add(subTerm);
                }
            }

            // 尝试3字分词
            for (int i = 0; i <= searchTerm.length() - 3; i++) {
                String subTerm = searchTerm.substring(i, i + 3);
                List<Resource> subResults = resourceMapper.selectByUnifiedSearch(subTerm, level, type, searchMode);
                if (!subResults.isEmpty()) {
                    results.addAll(subResults);
                    actualSearchTerms.add(subTerm);
                }
            }
        }

        // 去重
        results = results.stream().distinct().collect(Collectors.toList());
        actualSearchTerms = actualSearchTerms.stream().distinct().collect(Collectors.toList());

        return new SearchResultWithTerms(results, actualSearchTerms, "chinese_tokenized");
    }

    /**
     * 保持向后兼容的方法
     */
    private List<Resource> performUnifiedChineseTokenizedSearch(String searchTerm, Integer level, String type, String searchMode) {
        SearchResultWithTerms result = performUnifiedChineseTokenizedSearchWithTerms(searchTerm, level, type, searchMode);
        return result.getResults();
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
                resource.getSource(),
                resource.getResourceTime(),
                resource.getCreateTime(),
                resource.getUpdateTime()
        );
    }
}
