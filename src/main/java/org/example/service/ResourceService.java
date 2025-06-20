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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

            // 添加到Lucene索引
            try {
                luceneSearchService.indexResource(resource);
                logger.info("资源已添加到Lucene索引，ID: {}", resource.getId());
            } catch (Exception e) {
                logger.warn("添加资源到Lucene索引失败，ID: {}, 错误: {}", resource.getId(), e.getMessage());
            }

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

                // 更新Lucene索引
                try {
                    luceneSearchService.indexResource(existingResource);
                    logger.info("资源Lucene索引已更新，ID: {}", existingResource.getId());
                } catch (Exception e) {
                    logger.warn("更新资源Lucene索引失败，ID: {}, 错误: {}", existingResource.getId(), e.getMessage());
                }

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

                // 从Lucene索引中删除
                try {
                    luceneSearchService.deleteResourceFromIndex(id);
                    logger.info("资源已从Lucene索引中删除，ID: {}", id);
                } catch (Exception e) {
                    logger.warn("从Lucene索引删除资源失败，ID: {}, 错误: {}", id, e.getMessage());
                }

                return true;
            }
        }

        logger.warn("未找到要删除的资源，ID: {}", id);
        return false;
    }





    /**
     * 统一搜索功能：支持Lucene + IK Analyzer高级搜索、多字段搜索、模糊匹配、分词搜索和类型过滤（分页）
     * 优先使用Lucene搜索，降级到数据库搜索
     * 支持按名称和内容搜索（分词搜索仅搜索名称字段）
     * 支持按层级和内容类型过滤（数据库层面实现）
     * 支持搜索模式：multi（名称+内容）或 name（仅名称）
     * 返回包含实际搜索词信息的SearchResponse
     */
    public SearchResponse searchResourcesWithSearchInfo(SearchRequest searchRequest) {
        logger.info("执行分页统一搜索（Lucene + IK），搜索词: {}, 页码: {}, 大小: {}, 层级: {}, 类型: {}, 搜索模式: {}",
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
     * 执行高级搜索的核心逻辑（优先使用Lucene，降级到数据库搜索）
     * 支持层级和类型过滤，支持搜索模式选择，支持分页
     * 返回搜索结果和实际使用的搜索词
     */
    private SearchResultWithTerms performAdvancedSearchWithTerms(String searchTerm, Integer level, String type,
                                                               String searchMode, int page, int size) {
        List<String> actualSearchTerms = new ArrayList<>();
        String searchStrategy = "lucene_ik";
        PageInfo<ResourceResponse> pageInfo = null;

//        try {
//            // 首先尝试使用Lucene + IK Analyzer搜索
//            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
//                logger.info("尝试使用Lucene + IK Analyzer搜索");
//
//                LuceneSearchService.LuceneSearchResult luceneResult = luceneSearchService.searchWithPagination(
//                    searchTerm, searchMode, level, type, page, size);
//
//                if (!luceneResult.getResourceIds().isEmpty()) {
//                    // 根据Lucene返回的ID列表获取完整的资源信息
//                    List<Resource> resources = getResourcesByIds(luceneResult.getResourceIds());
//                    pageInfo = createPageInfoFromLuceneResult(resources, luceneResult, page, size);
//                    actualSearchTerms = luceneResult.getAnalyzedTerms();
//                    searchStrategy = "lucene_ik";
//
//                    logger.info("Lucene搜索成功，找到 {} 个结果，分析词汇: {}",
//                              luceneResult.getTotalHits(), actualSearchTerms);
//
//                    return new SearchResultWithTerms(pageInfo, actualSearchTerms, searchStrategy);
//                }
//            }
//
//            logger.info("Lucene搜索无结果，降级到数据库搜索");
//
//        } catch (Exception e) {
//            logger.warn("Lucene搜索失败，降级到数据库搜索: {}", e.getMessage());
//        }

        // 降级到原有的数据库搜索逻辑
        return performDatabaseSearchWithPagination(searchTerm, level, type, searchMode, page, size);
    }

    /**
     * 根据ID列表获取资源信息（批量查询优化）
     */
    private List<Resource> getResourcesByIds(List<Integer> resourceIds) {
        if (resourceIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用批量查询提高性能
        return resourceMapper.selectByIds(resourceIds);
    }

    /**
     * 从Lucene搜索结果创建PageInfo对象
     */
    private PageInfo<ResourceResponse> createPageInfoFromLuceneResult(List<Resource> resources,
                                                                     LuceneSearchService.LuceneSearchResult luceneResult,
                                                                     int page, int size) {
        // 转换为响应对象
        List<ResourceResponse> responseList = resources.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        PageInfo<ResourceResponse> pageInfo = new PageInfo<>();
        pageInfo.setList(responseList);
        pageInfo.setTotal(luceneResult.getTotalHits());
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(size);
        pageInfo.setPages((int) Math.ceil((double) luceneResult.getTotalHits() / size));
        pageInfo.setHasNextPage(page < pageInfo.getPages());
        pageInfo.setHasPreviousPage(page > 1);
        pageInfo.setIsFirstPage(page == 1);
        pageInfo.setIsLastPage(page >= pageInfo.getPages());

        return pageInfo;
    }

    /**
     * 数据库搜索（带分页）
     */
    private SearchResultWithTerms performDatabaseSearchWithPagination(String searchTerm, Integer level, String type,
                                                                     String searchMode, int page, int size) {
        // 使用PageHelper进行分页
        PageHelper.startPage(page, size);

        // 执行原有的数据库搜索逻辑
        SearchResultWithTerms searchResult = performUnifiedSearchWithTerms(searchTerm, level, type, searchMode);

        // 转换为分页信息
        PageInfo<ResourceResponse> pageInfo = convertToPageInfo(searchResult.getResults());

        return new SearchResultWithTerms(pageInfo, searchResult.getActualSearchTerms(),
                                       "database_" + searchResult.getSearchStrategy());
    }

    /**
     * 重建Lucene索引
     */
    public void rebuildLuceneIndex() {
        try {
            luceneSearchService.rebuildIndex();
            logger.info("Lucene索引重建成功");
        } catch (Exception e) {
            logger.error("Lucene索引重建失败: {}", e.getMessage(), e);
            throw new RuntimeException("重建索引失败", e);
        }
    }

    /**
     * 执行统一搜索的核心逻辑（精准+分词组合搜索）
     * 支持层级和类型过滤，支持搜索模式选择
     * 返回搜索结果和实际使用的搜索词
     * 搜索策略：完整词 + 分词结果，按优先级排序
     */
    private SearchResultWithTerms performUnifiedSearchWithTerms(String searchTerm, Integer level, String type, String searchMode) {
        List<Resource> results = new ArrayList<>();
        List<String> actualSearchTerms = new ArrayList<>();
        String searchStrategy = "combined";

        try {
            // 使用组合搜索策略：精准 + 分词
            SearchResultWithTerms combinedResult = performCombinedSearch(searchTerm, level, type, searchMode);
            results = combinedResult.getResults();
            actualSearchTerms = combinedResult.getActualSearchTerms();
            searchStrategy = combinedResult.getSearchStrategy();

            logger.info("组合搜索完成，找到 {} 个结果，实际搜索词: {}", results.size(), actualSearchTerms);

        } catch (Exception e) {
            logger.error("组合搜索过程中出现错误: {}", e.getMessage(), e);
            // 降级到简单搜索
            results = resourceMapper.selectByUnifiedSearch(searchTerm, level, type, searchMode);
            actualSearchTerms.add(searchTerm);
            searchStrategy = "fallback";
        }

        return new SearchResultWithTerms(results, actualSearchTerms, searchStrategy);
    }

    /**
     * 执行组合搜索：精准搜索 + 分词搜索，按优先级排序
     * 搜索顺序：1. 完整搜索词 2. IK分词结果 3. 英文分词结果
     */
    private SearchResultWithTerms performCombinedSearch(String searchTerm, Integer level, String type, String searchMode) {
        List<Resource> finalResults = new ArrayList<>();
        List<String> actualSearchTerms = new ArrayList<>();
        Set<Integer> addedResourceIds = new HashSet<>(); // 用于去重
        String searchStrategy = "combined";

        try {
            // 第一优先级：完整搜索词精准匹配
            List<Resource> exactResults = resourceMapper.selectByUnifiedSearch(searchTerm, level, type, searchMode);
            if (!exactResults.isEmpty()) {
                for (Resource resource : exactResults) {
                    if (!addedResourceIds.contains(resource.getId())) {
                        finalResults.add(resource);
                        addedResourceIds.add(resource.getId());
                    }
                }
                actualSearchTerms.add(searchTerm);
                logger.info("精准搜索找到 {} 个结果", exactResults.size());
            }

            // 第二优先级：使用 Lucene + IK Analyzer 进行中文分词搜索
            List<String> ikTokens = new ArrayList<>();
            try {
                ikTokens = luceneSearchService.analyzeText(searchTerm);
                logger.info("IK分词结果: {}", ikTokens);

                // 对每个分词结果进行搜索
                for (String token : ikTokens) {
                    if (!token.equals(searchTerm) && token.trim().length() > 0) { // 避免重复搜索完整词
                        List<Resource> tokenResults = resourceMapper.selectByUnifiedSearch(token, level, type, searchMode);
                        for (Resource resource : tokenResults) {
                            if (!addedResourceIds.contains(resource.getId())) {
                                finalResults.add(resource);
                                addedResourceIds.add(resource.getId());
                            }
                        }
                        if (!tokenResults.isEmpty()) {
                            actualSearchTerms.add(token);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("IK分词失败，跳过中文分词搜索: {}", e.getMessage());
            }

            // 第三优先级：英文空格分词搜索（如果包含空格）
            if (searchTerm.contains(" ")) {
                String[] englishTokens = searchTerm.split("\\s+");
                for (String token : englishTokens) {
                    if (!token.equals(searchTerm) && token.trim().length() > 0) { // 避免重复搜索完整词
                        List<Resource> tokenResults = resourceMapper.selectByUnifiedSearch(token.trim(), level, type, searchMode);
                        for (Resource resource : tokenResults) {
                            if (!addedResourceIds.contains(resource.getId())) {
                                finalResults.add(resource);
                                addedResourceIds.add(resource.getId());
                            }
                        }
                        if (!tokenResults.isEmpty() && !actualSearchTerms.contains(token.trim())) {
                            actualSearchTerms.add(token.trim());
                        }
                    }
                }
            }

            // 如果没有找到任何结果，尝试简单的字符分词作为最后的降级策略
            if (finalResults.isEmpty() && searchTerm.length() > 1) {
                logger.info("尝试简单字符分词作为降级策略");
                SearchResultWithTerms fallbackResult = performSimpleChineseTokenizedSearch(searchTerm, level, type, searchMode);
                finalResults = fallbackResult.getResults();
                actualSearchTerms.addAll(fallbackResult.getActualSearchTerms());
                searchStrategy = "fallback_tokenized";
            }

        } catch (Exception e) {
            logger.error("组合搜索出错: {}", e.getMessage(), e);
            // 最终降级：只返回精准搜索结果
            finalResults = resourceMapper.selectByUnifiedSearch(searchTerm, level, type, searchMode);
            actualSearchTerms.add(searchTerm);
            searchStrategy = "exact_only";
        }

        return new SearchResultWithTerms(finalResults, actualSearchTerms, searchStrategy);
    }

    /**
     * 简单的中文分词搜索（降级方案）
     */
    private SearchResultWithTerms performSimpleChineseTokenizedSearch(String searchTerm, Integer level, String type, String searchMode) {
        List<Resource> results = new ArrayList<>();
        List<String> actualSearchTerms = new ArrayList<>();

        // 简单的中文分词：按2-3个字符分组
        if (searchTerm.length() >= 2) {
            // 尝试2字分词
            for (int i = 0; i <= searchTerm.length() - 2; i++) {
                String subTerm = searchTerm.substring(i, i + 2);
                List<Resource> subResults = resourceMapper.selectByUnifiedSearch(subTerm, level, type, searchMode);
                if (!subResults.isEmpty()) {
                    results.addAll(subResults);
                    actualSearchTerms.add(subTerm);
                }
            }

            // 尝试3字分词（如果长度足够）
            if (searchTerm.length() >= 3) {
                for (int i = 0; i <= searchTerm.length() - 3; i++) {
                    String subTerm = searchTerm.substring(i, i + 3);
                    List<Resource> subResults = resourceMapper.selectByUnifiedSearch(subTerm, level, type, searchMode);
                    if (!subResults.isEmpty()) {
                        results.addAll(subResults);
                        actualSearchTerms.add(subTerm);
                    }
                }
            }
        }

        // 去重
        results = results.stream().distinct().collect(Collectors.toList());
        actualSearchTerms = actualSearchTerms.stream().distinct().collect(Collectors.toList());

        return new SearchResultWithTerms(results, actualSearchTerms, "simple_chinese_tokenized");
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
