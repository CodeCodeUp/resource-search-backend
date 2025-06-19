package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.example.config.CrawlerConfig;
import org.example.dto.QQChannelResource;
import org.example.entity.Resource;
import org.example.enums.ResourceType;
import org.example.mapper.ResourceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QQ频道爬虫服务
 */
@Service
public class QQChannelCrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(QQChannelCrawlerService.class);

    // 正则表达式模式
    private static final Pattern NAME_PATTERN = Pattern.compile("(称|标题|资源)：(.*?)(?=(\\\\r|\\\\n|\\r|\\n|\")|$)", Pattern.DOTALL);
    private static final Pattern CONTENT_PATTERN = Pattern.compile(
            "描述：((?:(?![{}]).)*?)(?=\"|\\\\r|\\\\n|\\r|\\n|$)",
            Pattern.DOTALL
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https://pan\\.quark.*?(?=(\\\\r|\\\\n|\\r|\\n|\")|$))",
            Pattern.DOTALL
    );

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private CrawlerConfig crawlerConfig;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public QQChannelCrawlerService() {
        this.httpClient = createUnsafeOkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 创建忽略SSL证书验证的OkHttpClient
     */
    private OkHttpClient createUnsafeOkHttpClient() {
        try {
            // 创建信任所有证书的TrustManager
            final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
            };

            // 安装信任所有证书的TrustManager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // 创建SSLSocketFactory
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
        } catch (Exception e) {
            logger.error("创建HTTP客户端时发生错误: ", e);
            throw new RuntimeException(e);
        }
    }

    
    /**
     * 执行任务
     */
    public void crawlChannelResources() {
        logger.info("开始获取资源...");


        int pageCount = 1;
        int maxPages = crawlerConfig.getMaxPages();

        String feedAttchInfo = "";
        while (pageCount <= maxPages) {
            try {
                logger.info("第{}页...", pageCount);

                // 构建请求参数
                Map<String, Object> payload = buildRequestPayload(feedAttchInfo,pageCount);

                // 发送HTTP请求
                String responseBody = sendHttpRequest(payload);
                if (responseBody == null) {
                    logger.error("HTTP请求失败，停止爬取");
                    break;
                }

                // 解析响应
                JsonNode responseJson = objectMapper.readTree(responseBody);
                JsonNode feeds = responseJson.path("data").path("vecFeed");

                feedAttchInfo = responseJson.path("data").path("feedAttchInfo").asText();

                if (!feeds.isArray() || feeds.isEmpty()) {
                    logger.info("没有更多数据，结束");
                    break;
                }

                logger.info("第{}页获取到{}条帖子", pageCount, feeds.size());

                // 处理每个feed
                boolean foundDuplicate = false;
                for (JsonNode feed : feeds) {
                    // 从vecFeed.contents.contents[0]获取内容
                    String text = feed.toPrettyString();

                    // 获取频道信息确定类型
                    String channelName = feed.path("channelInfo").path("name").asText();
                    ResourceType resourceType = ResourceType.getByChannelName(channelName);

                    // 获取图片URL
                    String imageUrl = "";
                    JsonNode images = feed.path("images");
                    if (images.isArray() && !images.isEmpty()) {
                        imageUrl = images.get(0).path("picUrl").asText();
                    }

                    // 获取createTime时间戳
                    Integer resourceTime = null;
                    String createTimeStr = feed.path("createTime").asText();
                    try {
                        resourceTime = Integer.parseInt(createTimeStr);
                    } catch (NumberFormatException e) {
                        logger.warn("跳过，无法解析createTime: {}", createTimeStr);
                        continue;
                    }

                    QQChannelResource resource = extractInfoFromText(text, imageUrl);

                    if (resource != null && isValidResource(resource)) {

                        if (resourceMapper.existsByTime(resourceTime)) {
                            logger.info("发现重复资源: {}, 停止爬取", resource.getLink());
                            foundDuplicate = true;
                            break;
                        }

                        // 检查URL是否已存在
                        if (resourceMapper.existsByUrl(resource.getLink())) {
                            logger.info("发现重复资源: {}, 跳过", resource.getLink());
                            continue;
                        }

                        // 保存到数据库
                        saveResourceToDatabase(resource, resourceType, resourceTime);
                        logger.info("保存资源: {} (时间戳: {})", resource.getTitle(), resourceTime);
                    }
                }



                if (foundDuplicate) {
                    break;
                }

                pageCount++;

                // 页面间隔
                if (pageCount < maxPages) {
                    logger.info("等待{}秒后获取下一页...", crawlerConfig.getPageDelaySeconds());
                    Thread.sleep(crawlerConfig.getPageDelaySeconds() * 1000);
                }

            } catch (Exception e) {
                logger.error("爬取过程中发生错误: ", e);
                break;
            }
        }

        logger.info("本轮爬取完成!");
    }
    
    /**
     * 构建请求参数
     */
    private Map<String, Object> buildRequestPayload(String feedAttchInfo, int pageCount) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("count", 10);
        payload.put("from", crawlerConfig.getFromParam());
        payload.put("guild_number", crawlerConfig.getGuildNumber());
        payload.put("get_type", crawlerConfig.getGetType());
        payload.put("feedAttchInfo", feedAttchInfo);
        payload.put("sortOption", crawlerConfig.getSortOption());
        payload.put("need_channel_list", crawlerConfig.getNeedChannelList());
        payload.put("need_top_info", crawlerConfig.getNeedTopInfo());

        return payload;
    }
    
    /**
     * 发送HTTP请求
     */
    private String sendHttpRequest(Map<String, Object> payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            RequestBody body = RequestBody.create(
                jsonPayload,
                MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(crawlerConfig.getApiUrl())
                    .post(body)
                    .addHeader("x-oidb", "{\"uint32_service_type\":13}")
                    .addHeader("Cookie", crawlerConfig.getCookie())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                } else {
                    logger.error("HTTP请求失败，状态码: {}", response.code());
                    return null;
                }
            }
        } catch (IOException e) {
            logger.error("发送HTTP请求时发生错误: ", e);
            return null;
        }
    }

    /**
     * 从文本中提取资源信息（使用正则表达式）
     */
    private QQChannelResource extractInfoFromText(String text, String url) {
        try {
            String name = "";
            String description = "";
            String link = "";
            int maxLength = 0;

            // 使用正则表达式提取信息
            Matcher nameMatcher = NAME_PATTERN.matcher(text);
            if (nameMatcher.find()) {
                name = nameMatcher.group(2).trim();
            }



            Matcher contentMatcher = CONTENT_PATTERN.matcher(text);
            while (contentMatcher.find()) {
                String current = contentMatcher.group(1).trim();
                if (current.length() > maxLength) {
                    maxLength = current.length();
                    description = current;
                }
            }

            Matcher urlMatcher = URL_PATTERN.matcher(text);
            if (urlMatcher.find()) {
                link = urlMatcher.group(1).trim();
            }

            return new QQChannelResource(name, description, link, url);

        } catch (Exception e) {
            logger.error("解析文本信息时发生错误: ", e);
            return null;
        }
    }

    /**
     * 验证资源是否有效
     */
    private boolean isValidResource(QQChannelResource resource) {
        return resource.getTitle() != null && !resource.getTitle().trim().isEmpty() &&
               resource.getLink() != null && !resource.getLink().trim().isEmpty();
    }

    /**
     * 保存资源到数据库
     */
    private void saveResourceToDatabase(QQChannelResource qqResource, ResourceType resourceType, Integer resourceTime) {
        try {
            Resource resource = new Resource();
            resource.setName(qqResource.getTitle());
            resource.setContent(qqResource.getDescription());
            resource.setUrl(qqResource.getLink());
            resource.setPig(qqResource.getImageUrl());
            resource.setLevel(1); // 默认层级为1
            resource.setType(resourceType.getCode()); // 根据频道类型设置
            resource.setResourceTime(resourceTime); // 设置资源时间戳

            int result = resourceMapper.insert(resource);
            if (result > 0) {
                logger.info("成功保存资源到数据库: {} (类型: {}, 时间戳: {})", resource.getName(), resourceType.getDescription(), resourceTime);
            } else {
                logger.error("保存资源到数据库失败: {}", resource.getName());
            }
        } catch (Exception e) {
            logger.error("保存资源到数据库时发生错误: ", e);
        }
    }
}
