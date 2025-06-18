package org.example.util;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP客户端工具类
 * 封装OkHttp客户端，提供通用的HTTP请求功能
 */
@Component
public class HttpClientUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    
    private final OkHttpClient httpClient;
    
    public HttpClientUtil() {
        this.httpClient = createUnsafeOkHttpClient();
    }
    
    /**
     * 发送GET请求
     */
    public String get(String url) {
        return get(url, null);
    }
    
    /**
     * 发送GET请求（带请求头）
     */
    public String get(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url);
        
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        
        Request request = builder.build();
        return executeRequest(request);
    }
    
    /**
     * 发送POST请求（JSON格式）
     */
    public String postJson(String url, String jsonBody) {
        return postJson(url, jsonBody, null);
    }
    
    /**
     * 发送POST请求（JSON格式，带请求头）
     */
    public String postJson(String url, String jsonBody, Map<String, String> headers) {
        RequestBody body = RequestBody.create(
            jsonBody, 
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);
        
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        
        Request request = builder.build();
        return executeRequest(request);
    }
    
    /**
     * 发送POST请求（表单格式）
     */
    public String postForm(String url, Map<String, String> formData) {
        return postForm(url, formData, null);
    }
    
    /**
     * 发送POST请求（表单格式，带请求头）
     */
    public String postForm(String url, Map<String, String> formData, Map<String, String> headers) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (formData != null) {
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        
        RequestBody body = formBuilder.build();
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);
        
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        
        Request request = builder.build();
        return executeRequest(request);
    }
    
    /**
     * 执行HTTP请求
     */
    private String executeRequest(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                logger.error("HTTP请求失败，URL: {}, 状态码: {}", request.url(), response.code());
                return null;
            }
        } catch (IOException e) {
            logger.error("发送HTTP请求时发生错误，URL: {}, 错误: ", request.url(), e);
            return null;
        }
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
     * 获取OkHttpClient实例（如果需要更复杂的操作）
     */
    public OkHttpClient getHttpClient() {
        return httpClient;
    }
}
