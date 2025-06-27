package org.example.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 字符串清理工具类测试
 */
public class StringCleanupUtilTest {
    
    @Test
    void testRemoveBackslashes() {
        // 测试移除反斜杠
        assertEquals("测试资源", StringCleanupUtil.removeBackslashes("测试\\资源"));
        assertEquals("游戏下载", StringCleanupUtil.removeBackslashes("游戏\\\\下载"));
        assertEquals("正常文本", StringCleanupUtil.removeBackslashes("正常文本"));
        assertNull(StringCleanupUtil.removeBackslashes(null));
        assertEquals("", StringCleanupUtil.removeBackslashes(""));
    }
    
    @Test
    void testCleanSpecialCharacters() {
        // 测试清理特殊字符
        assertEquals("测试资源", StringCleanupUtil.cleanSpecialCharacters("测试\r\n资源"));
        assertEquals("游戏下载", StringCleanupUtil.cleanSpecialCharacters("游戏\t\\下载"));
        assertEquals("多个 空格", StringCleanupUtil.cleanSpecialCharacters("多个   空格"));
        assertEquals("首尾清理", StringCleanupUtil.cleanSpecialCharacters("  首尾清理  "));
        assertNull(StringCleanupUtil.cleanSpecialCharacters(null));
    }
    
    @Test
    void testCleanResourceName() {
        // 测试清理资源名称
        assertEquals("测试资源", StringCleanupUtil.cleanResourceName("测试\\资源"));
        assertEquals("正常名称", StringCleanupUtil.cleanResourceName("正常名称"));
        assertNull(StringCleanupUtil.cleanResourceName(null));
        assertEquals("", StringCleanupUtil.cleanResourceName(""));
        
        // 测试长度限制
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append("a");
        }
        String longName = sb.toString();
        String cleanedName = StringCleanupUtil.cleanResourceName(longName);
        assertEquals(255, cleanedName.length());
    }
    
    @Test
    void testCleanResourceContent() {
        // 测试清理资源内容
        assertEquals("测试内容", StringCleanupUtil.cleanResourceContent("测试\\内容"));
        assertEquals("正常内容", StringCleanupUtil.cleanResourceContent("正常内容"));
        assertNull(StringCleanupUtil.cleanResourceContent(null));
        
        // 测试长度限制
        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < 1200; i++) {
            sb2.append("a");
        }
        String longContent = sb2.toString();
        String cleanedContent = StringCleanupUtil.cleanResourceContent(longContent);
        assertEquals(1000, cleanedContent.length());
    }
    
    @Test
    void testCleanResourceUrl() {
        // 测试清理资源URL
        assertEquals("https://example.comtest",
            StringCleanupUtil.cleanResourceUrl("https://example.com\\test"));
        assertEquals("https://pan.quark.cn/s/abc123",
            StringCleanupUtil.cleanResourceUrl("https://pan.quark.cn/s/abc123"));
        assertNull(StringCleanupUtil.cleanResourceUrl(null));
        assertEquals("", StringCleanupUtil.cleanResourceUrl(""));
    }
    
    @Test
    void testCleanResourceFields() {
        // 测试批量清理资源字段
        String[] result = StringCleanupUtil.cleanResourceFields(
            "测试\\名称", 
            "测试\\内容", 
            "https://example.com\\test"
        );
        
        assertEquals(3, result.length);
        assertEquals("测试名称", result[0]);
        assertEquals("测试内容", result[1]);
        assertEquals("https://example.comtest", result[2]);
    }
    
    @Test
    void testContainsBackslashes() {
        // 测试检查是否包含反斜杠
        assertTrue(StringCleanupUtil.containsBackslashes("测试\\资源"));
        assertTrue(StringCleanupUtil.containsBackslashes("\\开头"));
        assertTrue(StringCleanupUtil.containsBackslashes("结尾\\"));
        assertFalse(StringCleanupUtil.containsBackslashes("正常文本"));
        assertFalse(StringCleanupUtil.containsBackslashes(""));
        assertFalse(StringCleanupUtil.containsBackslashes(null));
    }
    
    @Test
    void testCountBackslashes() {
        // 测试统计反斜杠数量
        assertEquals(1, StringCleanupUtil.countBackslashes("测试\\资源"));
        assertEquals(2, StringCleanupUtil.countBackslashes("测试\\\\资源"));
        assertEquals(3, StringCleanupUtil.countBackslashes("\\测试\\资源\\"));
        assertEquals(0, StringCleanupUtil.countBackslashes("正常文本"));
        assertEquals(0, StringCleanupUtil.countBackslashes(""));
        assertEquals(0, StringCleanupUtil.countBackslashes(null));
    }
    
    @Test
    void testComplexScenarios() {
        // 测试复杂场景
        String complexInput = "  测试\r\n资源\\\\下载  \t  ";
        String expected = "测试资源下载";
        assertEquals(expected, StringCleanupUtil.cleanSpecialCharacters(complexInput));

        // 测试包含多种特殊字符的URL
        String complexUrl = "https://pan.quark.cn/s/abc123\\\\def456\r\n";
        String expectedUrl = "https://pan.quark.cn/s/abc123def456";
        assertEquals(expectedUrl, StringCleanupUtil.cleanResourceUrl(complexUrl));
    }
    
    @Test
    void testEdgeCases() {
        // 测试边界情况
        assertEquals("", StringCleanupUtil.removeBackslashes("\\\\\\"));
        assertEquals("", StringCleanupUtil.cleanSpecialCharacters("\r\n\t"));
        assertEquals("a", StringCleanupUtil.cleanSpecialCharacters("\\a\\"));

        // 测试只有空白字符的情况
        assertEquals("", StringCleanupUtil.cleanSpecialCharacters("   "));
        assertEquals("", StringCleanupUtil.cleanResourceName("   "));
    }
}
