package org.example.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.example.entity.Resource;
import org.example.mapper.ResourceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Lucene + IK Analyzer 高级搜索服务
 * 提供基于Lucene的全文搜索功能，支持中文分词和高级查询
 */
@Service
public class LuceneSearchService {

    private static final Logger logger = LoggerFactory.getLogger(LuceneSearchService.class);

    @Autowired
    private ResourceMapper resourceMapper;

    private Directory directory;
    private Analyzer analyzer;
    private IndexWriter indexWriter;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;

    /**
     * Lucene搜索结果包装类
     */
    public static class LuceneSearchResult {
        private List<Integer> resourceIds;
        private List<String> analyzedTerms;
        private String searchStrategy;
        private int totalHits;

        public LuceneSearchResult(List<Integer> resourceIds, List<String> analyzedTerms, String searchStrategy, int totalHits) {
            this.resourceIds = resourceIds;
            this.analyzedTerms = analyzedTerms;
            this.searchStrategy = searchStrategy;
            this.totalHits = totalHits;
        }

        // Getters
        public List<Integer> getResourceIds() { return resourceIds; }
        public List<String> getAnalyzedTerms() { return analyzedTerms; }
        public String getSearchStrategy() { return searchStrategy; }
        public int getTotalHits() { return totalHits; }
    }

    @PostConstruct
    public void init() throws IOException {
        logger.info("初始化 Lucene + IK Analyzer 搜索服务...");
        
        // 使用内存目录（生产环境可以改为文件系统目录）
        directory = new RAMDirectory();
        
        // 使用 IK 分词器，智能分词模式
        analyzer = new IKAnalyzer(true);
        
        // 创建索引写入器配置
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        
        // 创建索引写入器
        indexWriter = new IndexWriter(directory, config);
        
        // 初始化索引
        buildInitialIndex();
        
        logger.info("Lucene + IK Analyzer 搜索服务初始化完成");
    }

    @PreDestroy
    public void destroy() throws IOException {
        logger.info("关闭 Lucene 搜索服务...");
        
        if (indexWriter != null) {
            indexWriter.close();
        }
        if (indexReader != null) {
            indexReader.close();
        }
        if (directory != null) {
            directory.close();
        }
        if (analyzer != null) {
            analyzer.close();
        }
        
        logger.info("Lucene 搜索服务已关闭");
    }

    /**
     * 构建初始索引
     */
    private void buildInitialIndex() throws IOException {
        logger.info("开始构建初始索引...");
        
        List<Resource> allResources = resourceMapper.selectAll();
        for (Resource resource : allResources) {
            indexResource(resource);
        }
        
        indexWriter.commit();
        refreshSearcher();
        
        logger.info("初始索引构建完成，共索引 {} 个资源", allResources.size());
    }

    /**
     * 添加或更新资源到索引
     */
    public void indexResource(Resource resource) throws IOException {
        // 先删除已存在的文档
        indexWriter.deleteDocuments(new Term("id", String.valueOf(resource.getId())));
        
        Document doc = new Document();
        
        // 添加字段到文档
        doc.add(new StringField("id", String.valueOf(resource.getId()), Field.Store.YES));
        doc.add(new TextField("name", resource.getName() != null ? resource.getName() : "", Field.Store.YES));
        doc.add(new TextField("content", resource.getContent() != null ? resource.getContent() : "", Field.Store.YES));
        doc.add(new StringField("url", resource.getUrl() != null ? resource.getUrl() : "", Field.Store.YES));
        doc.add(new StringField("type", resource.getType() != null ? resource.getType() : "", Field.Store.YES));
        doc.add(new StringField("level", String.valueOf(resource.getLevel() != null ? resource.getLevel() : 0), Field.Store.YES));
        doc.add(new StringField("source", String.valueOf(resource.getSource() != null ? resource.getSource() :  0), Field.Store.YES));
        
        indexWriter.addDocument(doc);
        indexWriter.commit();
        
        // 刷新搜索器
        refreshSearcher();
    }

    /**
     * 从索引中删除资源
     */
    public void deleteResourceFromIndex(Integer resourceId) throws IOException {
        indexWriter.deleteDocuments(new Term("id", String.valueOf(resourceId)));
        indexWriter.commit();
        refreshSearcher();
    }

    /**
     * 刷新搜索器
     */
    private void refreshSearcher() throws IOException {
        if (indexReader != null) {
            indexReader.close();
        }
        indexReader = DirectoryReader.open(directory);
        indexSearcher = new IndexSearcher(indexReader);
    }

    /**
     * 分析文本，获取分词结果
     */
    public List<String> analyzeText(String text) throws IOException {
        List<String> terms = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return terms;
        }
        
        try (TokenStream tokenStream = analyzer.tokenStream("", new StringReader(text))) {
            CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            
            while (tokenStream.incrementToken()) {
                String term = termAttribute.toString();
                if (!term.trim().isEmpty()) {
                    terms.add(term);
                }
            }
            
            tokenStream.end();
        }
        
        return terms;
    }

    /**
     * 高级搜索功能（支持分页）
     */
    public LuceneSearchResult searchWithPagination(String searchTerm, String searchMode, Integer level, String type, 
                                                  int page, int size) throws IOException, ParseException {
        if (indexSearcher == null) {
            refreshSearcher();
        }
        
        // 分析搜索词获取实际分词结果
        List<String> analyzedTerms = analyzeText(searchTerm);
        
        // 构建查询
        Query query = buildQuery(searchTerm, searchMode, level, type);
        
        // 计算分页参数
        int start = (page - 1) * size;
        int maxResults = start + size;
        
        // 执行搜索
        TopDocs topDocs = indexSearcher.search(query, maxResults);
        
        // 收集结果
        List<Integer> resourceIds = new ArrayList<>();
        int actualStart = Math.min(start, topDocs.scoreDocs.length);
        int actualEnd = Math.min(maxResults, topDocs.scoreDocs.length);
        
        for (int i = actualStart; i < actualEnd; i++) {
            Document doc = indexSearcher.doc(topDocs.scoreDocs[i].doc);
            resourceIds.add(Integer.valueOf(doc.get("id")));
        }
        
        return new LuceneSearchResult(resourceIds, analyzedTerms, "lucene_ik", (int) topDocs.totalHits.value);
    }

    /**
     * 构建 Lucene 查询
     */
    private Query buildQuery(String searchTerm, String searchMode, Integer level, String type) throws ParseException {
        BooleanQuery.Builder mainQueryBuilder = new BooleanQuery.Builder();
        
        // 构建文本搜索查询
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            // 根据搜索模式选择搜索字段
            String[] fields;
            if ("name".equals(searchMode)) {
                fields = new String[]{"name"};
            } else {
                fields = new String[]{"name", "content"};
            }
            
            // 创建多字段查询解析器
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
            parser.setDefaultOperator(MultiFieldQueryParser.Operator.OR);
            
            Query textQuery = parser.parse(searchTerm);
            mainQueryBuilder.add(textQuery, BooleanClause.Occur.MUST);
        }
        
        // 添加过滤条件
        if (level != null) {
            Query levelQuery = new TermQuery(new Term("level", String.valueOf(level)));
            mainQueryBuilder.add(levelQuery, BooleanClause.Occur.MUST);
        }
        
        if (type != null && !type.trim().isEmpty()) {
            Query typeQuery = new TermQuery(new Term("type", type));
            mainQueryBuilder.add(typeQuery, BooleanClause.Occur.MUST);
        }
        
        BooleanQuery query = mainQueryBuilder.build();
        
        // 如果没有任何查询条件，返回匹配所有文档的查询
        if (query.clauses().isEmpty()) {
            return new MatchAllDocsQuery();
        }
        
        return query;
    }

    /**
     * 重建整个索引
     */
    public void rebuildIndex() throws IOException {
        logger.info("开始重建索引...");
        
        // 清空现有索引
        indexWriter.deleteAll();
        
        // 重新构建索引
        buildInitialIndex();
        
        logger.info("索引重建完成");
    }
}
