package org.example.controller;

import org.example.service.QuarkPanService;
import org.example.service.ResourceService;
import org.example.service.SecurityMonitorService;
import org.example.service.SlideVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

/**
 * 验证控制器夸克网盘集成测试
 */
public class VerificationControllerQuarkTest {
    
    @Mock
    private SlideVerificationService verificationService;
    
    @Mock
    private SecurityMonitorService securityMonitorService;
    
    @Mock
    private ResourceService resourceService;
    
    @Mock
    private QuarkPanService quarkPanService;
    
    @InjectMocks
    private VerificationController verificationController;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testProcessResourceUrl_QuarkPanLink() throws Exception {
        // 准备测试数据
        String originalUrl = "https://pan.quark.cn/s/abc123def456";
        String convertedUrl = "https://pan.quark.cn/s/xyz789uvw012";
        
        // Mock夸克网盘服务
        when(quarkPanService.replaceQuarkUrls(originalUrl)).thenReturn(convertedUrl);
        
        // 使用反射调用私有方法
        String result = (String) ReflectionTestUtils.invokeMethod(
            verificationController, "processResourceUrl", originalUrl
        );
        
        // 验证结果
        assertEquals(convertedUrl, result);
        verify(quarkPanService).replaceQuarkUrls(originalUrl);
    }
    
    @Test
    void testProcessResourceUrl_NonQuarkPanLink() throws Exception {
        // 准备测试数据
        String originalUrl = "https://example.com/resource.mp4";
        
        // 使用反射调用私有方法
        String result = (String) ReflectionTestUtils.invokeMethod(
            verificationController, "processResourceUrl", originalUrl
        );
        
        // 验证结果
        assertEquals(originalUrl, result);
        // 验证没有调用夸克网盘服务
        verify(quarkPanService, never()).replaceQuarkUrls(anyString());
    }
    
    @Test
    void testProcessResourceUrl_EmptyUrl() throws Exception {
        // 准备测试数据
        String originalUrl = "";
        
        // 使用反射调用私有方法
        String result = (String) ReflectionTestUtils.invokeMethod(
            verificationController, "processResourceUrl", originalUrl
        );
        
        // 验证结果
        assertEquals(originalUrl, result);
        verify(quarkPanService, never()).replaceQuarkUrls(anyString());
    }
    
    @Test
    void testProcessResourceUrl_NullUrl() throws Exception {
        // 准备测试数据
        String originalUrl = null;
        
        // 使用反射调用私有方法
        String result = (String) ReflectionTestUtils.invokeMethod(
            verificationController, "processResourceUrl", originalUrl
        );
        
        // 验证结果
        assertNull(result);
        verify(quarkPanService, never()).replaceQuarkUrls(anyString());
    }
    
    @Test
    void testProcessResourceUrl_ConversionFailed() throws Exception {
        // 准备测试数据
        String originalUrl = "https://pan.quark.cn/s/abc123def456";
        
        // Mock夸克网盘服务返回null（转换失败）
        when(quarkPanService.replaceQuarkUrls(originalUrl)).thenReturn(null);
        
        // 使用反射调用私有方法
        String result = (String) ReflectionTestUtils.invokeMethod(
            verificationController, "processResourceUrl", originalUrl
        );
        
        // 验证结果 - 应该返回原URL
        assertEquals(originalUrl, result);
        verify(quarkPanService).replaceQuarkUrls(originalUrl);
    }
    
    @Test
    void testProcessResourceUrl_ConversionReturnsSameUrl() throws Exception {
        // 准备测试数据
        String originalUrl = "https://pan.quark.cn/s/abc123def456";
        
        // Mock夸克网盘服务返回相同的URL（转换失败）
        when(quarkPanService.replaceQuarkUrls(originalUrl)).thenReturn(originalUrl);
        
        // 使用反射调用私有方法
        String result = (String) ReflectionTestUtils.invokeMethod(
            verificationController, "processResourceUrl", originalUrl
        );
        
        // 验证结果 - 应该返回原URL
        assertEquals(originalUrl, result);
        verify(quarkPanService).replaceQuarkUrls(originalUrl);
    }
    
    @Test
    void testProcessResourceUrl_ExceptionHandling() throws Exception {
        // 准备测试数据
        String originalUrl = "https://pan.quark.cn/s/abc123def456";
        
        // Mock夸克网盘服务抛出异常
        when(quarkPanService.replaceQuarkUrls(originalUrl))
            .thenThrow(new RuntimeException("转换服务异常"));
        
        // 使用反射调用私有方法
        String result = (String) ReflectionTestUtils.invokeMethod(
            verificationController, "processResourceUrl", originalUrl
        );
        
        // 验证结果 - 应该返回原URL
        assertEquals(originalUrl, result);
        verify(quarkPanService).replaceQuarkUrls(originalUrl);
    }
    
    @Test
    void testProcessResourceUrl_MultipleQuarkLinks() throws Exception {
        // 准备测试数据 - 包含多个夸克网盘链接的文本
        String originalUrl = "资源1: https://pan.quark.cn/s/abc123 资源2: https://pan.quark.cn/s/def456";
        String convertedUrl = "资源1: https://pan.quark.cn/s/xyz789 资源2: https://pan.quark.cn/s/uvw012";
        
        // Mock夸克网盘服务
        when(quarkPanService.replaceQuarkUrls(originalUrl)).thenReturn(convertedUrl);
        
        // 使用反射调用私有方法
        String result = (String) ReflectionTestUtils.invokeMethod(
            verificationController, "processResourceUrl", originalUrl
        );
        
        // 验证结果
        assertEquals(convertedUrl, result);
        verify(quarkPanService).replaceQuarkUrls(originalUrl);
    }
}
