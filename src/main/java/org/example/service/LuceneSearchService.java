package org.example.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * IK Analyzer 分词服务
 * 提供中文分词功能
 */
@Service
public class LuceneSearchService {

    private static final Logger logger = LoggerFactory.getLogger(LuceneSearchService.class);

    private Analyzer analyzer;

    @PostConstruct
    public void init() {
        logger.info("初始化 IK Analyzer 分词服务...");

        // 使用 IK 分词器，智能分词模式
        analyzer = new IKAnalyzer(true);

        logger.info("IK Analyzer 分词服务初始化完成");
    }

    @PreDestroy
    public void destroy() throws IOException {
        logger.info("关闭 IK Analyzer 分词服务...");

        if (analyzer != null) {
            analyzer.close();
        }

        logger.info("IK Analyzer 分词服务已关闭");
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
}
