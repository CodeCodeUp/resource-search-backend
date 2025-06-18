package org.example.controller;

import org.example.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP工具类测试控制器
 */
@RestController
@RequestMapping("/test")
public class HttpTestController {
    
    @Autowired
    private HttpClientUtil httpClientUtil;
    
    /**
     * 测试GET请求
     */
    @GetMapping("/http-get")
    public Map<String, Object> testHttpGet(@RequestParam(defaultValue = "https://httpbin.org/get") String url) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String response = httpClientUtil.get(url);
            result.put("success", true);
            result.put("response", response);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 测试POST请求
     */
    @GetMapping("/http-post")
    public Map<String, Object> testHttpPost() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String jsonData = "{\"test\": \"data\", \"timestamp\": " + System.currentTimeMillis() + "}";
            String response = httpClientUtil.postJson("https://httpbin.org/post", jsonData);
            result.put("success", true);
            result.put("response", response);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
