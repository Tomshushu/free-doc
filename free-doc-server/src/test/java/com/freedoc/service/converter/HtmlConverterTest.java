package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.config.ExportConfig;
import com.freedoc.entity.Doc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HtmlConverter测试类
 */
class HtmlConverterTest {
    
    private ExportConfig exportConfig;
    private HtmlConverter converter;
    private Doc testDoc;
    
    @BeforeEach
    void setUp() {
        // 使用真实的配置对象
        exportConfig = new ExportConfig();
        converter = new HtmlConverter(exportConfig);
        
        // 创建测试文档
        testDoc = new Doc();
        testDoc.setDocId("test-doc-001");
        testDoc.setDocTitle("Test Document");
        testDoc.setDocContent("# Title\n\nThis is a **test** document.\n\n```java\npublic class Test {\n    // code example\n}\n```\n\n| Col1 | Col2 |\n|------|------|\n| Val1 | Val2 |");
        testDoc.setCreateUser("Test User");
        testDoc.setCreateTime(LocalDateTime.now());
        testDoc.setUpdateTime(LocalDateTime.now());
        testDoc.setProjectId("project-001");
        testDoc.setTeamId("team-001");
    }
    
    @Test
    void testGetSupportedFormat() {
        assertEquals(ExportFormat.HTML, converter.getSupportedFormat());
    }
    
    @Test
    void testConvertBasicMarkdown() {
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        byte[] result = converter.convert(testDoc, options);
        
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        String html = new String(result);
        
        // 验证HTML结构
        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("<html lang=\"zh-CN\">"));
        assertTrue(html.contains("<head>"));
        assertTrue(html.contains("<body>"));
        assertTrue(html.contains("</html>"));
        
        // 验证标题转换
        assertTrue(html.contains("<title>Test Document</title>"));
        assertTrue(html.contains("<h1"));
        assertTrue(html.contains("Title"));
        
        // 验证粗体文本
        assertTrue(html.contains("<strong>test</strong>"));
        
        // 验证代码块
        assertTrue(html.contains("<pre"));
        assertTrue(html.contains("public class Test"));
        
        // 验证表格
        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("Col1"));
        assertTrue(html.contains("Val1"));
    }
    
    @Test
    void testConvertWithMetadata() {
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(true)
                .build();
        
        byte[] result = converter.convert(testDoc, options);
        String html = new String(result);
        
        // 验证元数据部分
        assertTrue(html.contains("document-metadata"));
        assertTrue(html.contains("Test Document"));
        assertTrue(html.contains("test-doc-001"));
        assertTrue(html.contains("Test User"));
    }
    
    @Test
    void testConvertWithoutMetadata() {
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(false)
                .build();
        
        byte[] result = converter.convert(testDoc, options);
        String html = new String(result);
        
        // 验证没有元数据部分
        assertFalse(html.contains("document-metadata"));
    }
    
    @Test
    void testConvertWithSyntaxHighlight() {
        ConversionOptions options = ConversionOptions.builder()
                .syntaxHighlight(true)
                .build();
        
        byte[] result = converter.convert(testDoc, options);
        String html = new String(result);
        
        // 验证语法高亮支持
        assertTrue(html.contains("highlight.js"));
        assertTrue(html.contains("hljs.highlightAll()"));
    }
    
    @Test
    void testConvertWithoutSyntaxHighlight() {
        ConversionOptions options = ConversionOptions.builder()
                .syntaxHighlight(false)
                .build();
        
        byte[] result = converter.convert(testDoc, options);
        String html = new String(result);
        
        // 验证没有语法高亮
        assertFalse(html.contains("highlight.js"));
        assertFalse(html.contains("hljs.highlightAll()"));
    }
    
    @Test
    void testConvertEmptyContent() {
        Doc emptyDoc = new Doc();
        emptyDoc.setDocId("empty-doc");
        emptyDoc.setDocTitle("Empty Document");
        emptyDoc.setDocContent("");
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        byte[] result = converter.convert(emptyDoc, options);
        String html = new String(result);
        
        // 验证空文档处理
        assertTrue(html.contains("<h1>Empty Document</h1>"));
        assertTrue(html.contains("<em>") && html.contains("</em>"));  // 检查斜体标签而不是具体中文
    }
    
    @Test
    void testConvertNullContent() {
        Doc nullDoc = new Doc();
        nullDoc.setDocId("null-doc");
        nullDoc.setDocTitle("Null Content Document");
        nullDoc.setDocContent(null);
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        byte[] result = converter.convert(nullDoc, options);
        String html = new String(result);
        
        // 验证null内容处理
        assertTrue(html.contains("<h1>Null Content Document</h1>"));
        assertTrue(html.contains("<p>"));
        assertTrue(html.contains("<em>"));
    }
    
    @Test
    void testConvertWithCustomCss() {
        String customCss = "body { background-color: red; }";
        ConversionOptions options = ConversionOptions.builder()
                .cssStyle(customCss)
                .build();
        
        byte[] result = converter.convert(testDoc, options);
        String html = new String(result);
        
        // 验证自定义CSS
        assertTrue(html.contains(customCss));
    }
    
    @Test
    void testConvertNullDoc() {
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        assertThrows(IllegalArgumentException.class, () -> {
            converter.convert(null, options);
        });
    }
    
    @Test
    void testGetMimeType() {
        assertEquals("text/html", converter.getMimeType());
    }
    
    @Test
    void testGetFileExtension() {
        assertEquals("html", converter.getFileExtension());
    }
}