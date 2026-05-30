package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.entity.Doc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MarkdownConverter单元测试
 * 
 * @author FreeDoc Team
 */
class MarkdownConverterTest {
    
    private MarkdownConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new MarkdownConverter();
    }
    
    @Test
    void testGetSupportedFormat() {
        assertEquals(ExportFormat.MARKDOWN, converter.getSupportedFormat());
    }
    
    @Test
    void testGetMimeType() {
        assertEquals("text/markdown", converter.getMimeType());
    }
    
    @Test
    void testGetFileExtension() {
        assertEquals("md", converter.getFileExtension());
    }
    
    @Test
    void testConvertWithNullDoc() {
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        assertThrows(IllegalArgumentException.class, () -> {
            converter.convert(null, options);
        });
    }
    
    @Test
    void testConvertWithBasicContent() {
        Doc doc = createTestDoc();
        doc.setDocContent("# 测试标题\n\n这是测试内容。");
        
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(false)
                .build();
        
        byte[] result = converter.convert(doc, options);
        String content = new String(result, StandardCharsets.UTF_8);
        
        assertEquals("# 测试标题\n\n这是测试内容。\n", content);
    }
    
    @Test
    void testConvertWithMetadata() {
        Doc doc = createTestDoc();
        doc.setDocContent("# 测试标题\n\n这是测试内容。");
        
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(true)
                .build();
        
        byte[] result = converter.convert(doc, options);
        String content = new String(result, StandardCharsets.UTF_8);
        
        assertTrue(content.startsWith("---\n"));
        assertTrue(content.contains("title: 测试文档"));
        assertTrue(content.contains("doc_id: test-doc-id"));
        assertTrue(content.contains("author: test-user"));
        assertTrue(content.contains("created: "));
        assertTrue(content.contains("updated: "));
        assertTrue(content.contains("project_id: test-project"));
        assertTrue(content.contains("team_id: test-team"));
        assertTrue(content.contains("---\n\n"));
        assertTrue(content.contains("# 测试标题\n\n这是测试内容。"));
    }
    
    @Test
    void testConvertWithEmptyContent() {
        Doc doc = createTestDoc();
        doc.setDocContent("");
        
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(false)
                .build();
        
        byte[] result = converter.convert(doc, options);
        String content = new String(result, StandardCharsets.UTF_8);
        
        assertEquals("# 测试文档\n", content);
    }
    
    @Test
    void testConvertWithNullContent() {
        Doc doc = createTestDoc();
        doc.setDocContent(null);
        
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(false)
                .build();
        
        byte[] result = converter.convert(doc, options);
        String content = new String(result, StandardCharsets.UTF_8);
        
        assertEquals("# 测试文档\n", content);
    }
    
    @Test
    void testConvertWithNullTitle() {
        Doc doc = createTestDoc();
        doc.setDocTitle(null);
        doc.setDocContent("测试内容");
        
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(false)
                .build();
        
        byte[] result = converter.convert(doc, options);
        String content = new String(result, StandardCharsets.UTF_8);
        
        assertEquals("测试内容\n", content);
    }
    
    @Test
    void testConvertWithNullTitleAndEmptyContent() {
        Doc doc = createTestDoc();
        doc.setDocTitle(null);
        doc.setDocContent("");
        
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(false)
                .build();
        
        byte[] result = converter.convert(doc, options);
        String content = new String(result, StandardCharsets.UTF_8);
        
        assertEquals("# 无标题文档\n", content);
    }
    
    @Test
    void testConvertWithSpecialCharactersInTitle() {
        Doc doc = createTestDoc();
        doc.setDocTitle("测试文档: \"特殊字符\" & 符号");
        doc.setDocContent("测试内容");
        
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(true)
                .build();
        
        byte[] result = converter.convert(doc, options);
        String content = new String(result, StandardCharsets.UTF_8);
        
        assertTrue(content.contains("title: \"测试文档: \\\"特殊字符\\\" & 符号\""));
    }
    
    @Test
    void testConvertWithSpecialCharactersInAuthor() {
        Doc doc = createTestDoc();
        doc.setCreateUser("用户名: \"特殊\"");
        doc.setDocContent("测试内容");
        
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(true)
                .build();
        
        byte[] result = converter.convert(doc, options);
        String content = new String(result, StandardCharsets.UTF_8);
        
        assertTrue(content.contains("author: \"用户名: \\\"特殊\\\"\""));
    }
    
    @Test
    void testConvertWithComplexMarkdownContent() {
        Doc doc = createTestDoc();
        doc.setDocContent("# 主标题\n\n## 子标题\n\n- 列表项1\n- 列表项2\n\n```java\nSystem.out.println(\"Hello\");\n```\n\n| 表格 | 内容 |\n|------|------|\n| 行1  | 数据1 |\n");
        
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(false)
                .build();
        
        byte[] result = converter.convert(doc, options);
        String content = new String(result, StandardCharsets.UTF_8);
        
        // 验证Markdown内容被保持（表格会被格式化对齐）
        assertTrue(content.contains("# 主标题"));
        assertTrue(content.contains("## 子标题"));
        assertTrue(content.contains("- 列表项1"));
        assertTrue(content.contains("```java"));
        assertTrue(content.contains("System.out.println"));
        // 表格会被格式化，所以检查表格标题是否存在
        assertTrue(content.contains("| 表格"));
        assertTrue(content.contains("| 内容"));
        assertTrue(content.contains("| 行1"));
        assertTrue(content.contains("| 数据1"));
    }
    
    @Test
    void testConvertEnsuresNewlineAtEnd() {
        Doc doc = createTestDoc();
        doc.setDocContent("内容没有换行符");
        
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(false)
                .build();
        
        byte[] result = converter.convert(doc, options);
        String content = new String(result, StandardCharsets.UTF_8);
        
        assertTrue(content.endsWith("\n"));
        assertEquals("内容没有换行符\n", content);
    }
    
    @Test
    void testConvertPreservesExistingNewline() {
        Doc doc = createTestDoc();
        doc.setDocContent("内容已有换行符\n");
        
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(false)
                .build();
        
        byte[] result = converter.convert(doc, options);
        String content = new String(result, StandardCharsets.UTF_8);
        
        assertEquals("内容已有换行符\n", content);
    }
    
    /**
     * 创建测试用的Doc对象
     */
    private Doc createTestDoc() {
        Doc doc = new Doc();
        doc.setDocId("test-doc-id");
        doc.setDocTitle("测试文档");
        doc.setCreateUser("test-user");
        doc.setCreateTime(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        doc.setUpdateTime(LocalDateTime.of(2024, 1, 15, 15, 45, 0));
        doc.setProjectId("test-project");
        doc.setTeamId("test-team");
        return doc;
    }
}