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

/**
 * 夸克网盘服务简单测试类
 */
public class QuarkPanServiceSimpleTest {
    
    @Mock
    private QuarkPanClient quarkPanClient;
    
    @Mock
    private QuarkPanConfig quarkPanConfig;
    
    @InjectMocks
    private QuarkPanService quarkPanService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
