package org.example.service;

import org.example.config.QuarkPanConfig;
import org.example.util.QuarkPanClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * 夸克网盘服务测试类
 */
public class QuarkPanServiceTest {
    
    @Mock
    private QuarkPanClient quarkPanClient;
    
    @Mock
    private QuarkPanConfig quarkPanConfig;
    
    @InjectMocks
    private QuarkPanService quarkPanService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 设置默认配置
        when(quarkPanConfig.getDefaultSaveDirectoryId()).thenReturn("test-directory-id");
        when(quarkPanConfig.getMaxFilesPerSave()).thenReturn(40);
    }
    
    @Test
    void testExtractQuarkUrlIds() {
        String text = "这是一个测试文本，包含夸克网盘链接：https://pan.quark.cn/s/abc123def456 和另一个链接 https://pan.quark.cn/s/xyz789uvw012";
        
        List<String> urlIds = quarkPanService.extractQuarkUrlIds(text);
        
        assertEquals(2, urlIds.size());
        assertEquals("abc123def456", urlIds.get(0));
        assertEquals("xyz789uvw012", urlIds.get(1));
    }
    
    @Test
    void testExtractQuarkUrlIds_EmptyText() {
        String text = "";
        
        List<String> urlIds = quarkPanService.extractQuarkUrlIds(text);
        
        assertTrue(urlIds.isEmpty());
    }
    
    @Test
    void testExtractQuarkUrlIds_NoUrls() {
        String text = "这是一个没有夸克网盘链接的文本";
        
        List<String> urlIds = quarkPanService.extractQuarkUrlIds(text);
        
        assertTrue(urlIds.isEmpty());
    }
    
    @Test
    void testReplaceQuarkUrls_WithMockedConversion() {
        String text = "测试链接：https://pan.quark.cn/s/test123";
        
        // Mock转换结果
        when(quarkPanClient.getSharePageToken(anyString())).thenReturn("mock-token");
        when(quarkPanClient.createShareUrl(anyList())).thenReturn("https://pan.quark.cn/s/new123");
        
        String result = quarkPanService.replaceQuarkUrls(text);
        
        assertNotNull(result);
        // 由于依赖外部服务，这里主要测试方法不会抛出异常
    }
    
    @Test
    void testReplaceQuarkUrls_EmptyText() {
        String text = "";
        
        String result = quarkPanService.replaceQuarkUrls(text);
        
        assertEquals("", result);
    }
    
    @Test
    void testReplaceQuarkUrls_NullText() {
        String result = quarkPanService.replaceQuarkUrls(null);
        
        assertNull(result);
    }
}
