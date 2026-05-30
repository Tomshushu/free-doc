package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.config.ExportConfig;
import com.freedoc.entity.Doc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 所有格式转换器集成测试
 * 验证任务4的检查点要求：确保所有格式转换器测试通过
 * 
 * @author FreeDoc Team
 */
@SpringBootTest(classes = {
    ConverterFactory.class,
    MarkdownConverter.class,
    HtmlConverter.class,
    PdfConverter.class,
    DocxConverter.class,
    ExportConfig.class,
    com.freedoc.config.ConverterConfig.class
})
class AllConvertersIntegrationTest {
    
    @Autowired
    private ConverterFactory converterFactory;
    
    private Doc testDoc;
    
    @BeforeEach
    void setUp() {
        // 创建测试文档
        testDoc = new Doc();
        testDoc.setDocId("integration-test-doc");
        testDoc.setDocTitle("集成测试文档");
        testDoc.setDocContent(createTestContent());
        testDoc.setCreateUser("integration-test-user");
        testDoc.setCreateTime(LocalDateTime.now());
        testDoc.setUpdateTime(LocalDateTime.now());
        testDoc.setProjectId("test-project");
        testDoc.setTeamId("test-team");
    }
    
    @Test
    void testAllConvertersAreAvailable() {
        // 验证所有预期的格式转换器都可用
        assertTrue(converterFactory.isSupported(ExportFormat.MARKDOWN), "MarkdownConverter应该可用");
        assertTrue(converterFactory.isSupported(ExportFormat.HTML), "HtmlConverter应该可用");
        assertTrue(converterFactory.isSupported(ExportFormat.PDF), "PdfConverter应该可用");
        assertTrue(converterFactory.isSupported(ExportFormat.DOCX), "DocxConverter应该可用");
        
        // 验证支持的格式数量
        assertEquals(4, converterFactory.getSupportedFormats().size(), "应该支持4种格式");
    }
    
    @Test
    void testMarkdownConverterWorks() {
        FormatConverter converter = converterFactory.getConverter(ExportFormat.MARKDOWN);
        assertNotNull(converter, "MarkdownConverter不应为空");
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        byte[] result = converter.convert(testDoc, options);
        
        assertNotNull(result, "Markdown转换结果不应为空");
        assertTrue(result.length > 0, "Markdown转换结果应有内容");
        
        String content = new String(result);
        
        // 验证基本结构：应该包含元数据和内容
        assertTrue(content.contains("---"), "应包含YAML元数据分隔符");
        assertTrue(content.contains("doc_id: integration-test-doc"), "应包含文档ID");
        assertTrue(content.contains("author: integration-test-user"), "应包含作者信息");
        assertTrue(content.contains("# "), "应包含Markdown标题");
        assertTrue(content.contains("**"), "应包含粗体标记");
        assertTrue(content.contains("```"), "应包含代码块标记");
        assertTrue(content.contains("|"), "应包含表格标记");
        
        System.out.println("✅ MarkdownConverter测试通过 - 文件大小: " + result.length + " 字节");
    }
    
    @Test
    void testHtmlConverterWorks() {
        FormatConverter converter = converterFactory.getConverter(ExportFormat.HTML);
        assertNotNull(converter, "HtmlConverter不应为空");
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        byte[] result = converter.convert(testDoc, options);
        
        assertNotNull(result, "HTML转换结果不应为空");
        assertTrue(result.length > 0, "HTML转换结果应有内容");
        
        String content = new String(result);
        assertTrue(content.contains("<!DOCTYPE html>"), "应包含HTML文档类型");
        assertTrue(content.contains("<h1"), "应包含HTML标题");
        assertTrue(content.contains("<table>"), "应包含HTML表格");
        
        System.out.println("✅ HtmlConverter测试通过 - 文件大小: " + result.length + " 字节");
    }
    
    @Test
    void testPdfConverterWorks() {
        FormatConverter converter = converterFactory.getConverter(ExportFormat.PDF);
        assertNotNull(converter, "PdfConverter不应为空");
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        byte[] result = converter.convert(testDoc, options);
        
        assertNotNull(result, "PDF转换结果不应为空");
        assertTrue(result.length > 0, "PDF转换结果应有内容");
        
        // 验证PDF文件头
        String header = new String(result, 0, Math.min(4, result.length));
        assertEquals("%PDF", header, "应生成有效的PDF文件");
        
        System.out.println("✅ PdfConverter测试通过 - 文件大小: " + result.length + " 字节");
    }
    
    @Test
    void testDocxConverterWorks() {
        FormatConverter converter = converterFactory.getConverter(ExportFormat.DOCX);
        assertNotNull(converter, "DocxConverter不应为空");
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        byte[] result = converter.convert(testDoc, options);
        
        assertNotNull(result, "DOCX转换结果不应为空");
        assertTrue(result.length > 0, "DOCX转换结果应有内容");
        
        // 验证DOCX文件头（ZIP格式）
        assertTrue(result.length >= 2, "DOCX文件应有足够的长度");
        assertEquals(0x50, result[0] & 0xFF, "DOCX文件应以PK开头"); // 'P'
        assertEquals(0x4B, result[1] & 0xFF, "DOCX文件应以PK开头"); // 'K'
        
        System.out.println("✅ DocxConverter测试通过 - 文件大小: " + result.length + " 字节");
    }
    
    @Test
    void testAllConvertersWithComplexContent() {
        // 使用复杂内容测试所有转换器
        Doc complexDoc = new Doc();
        complexDoc.setDocId("complex-test-doc");
        complexDoc.setDocTitle("复杂内容测试");
        complexDoc.setDocContent(createComplexContent());
        complexDoc.setCreateUser("test-user");
        complexDoc.setCreateTime(LocalDateTime.now());
        complexDoc.setUpdateTime(LocalDateTime.now());
        
        ConversionOptions options = ConversionOptions.builder()
            .includeMetadata(true)
            .syntaxHighlight(true)
            .build();
        
        // 测试所有格式
        for (ExportFormat format : ExportFormat.values()) {
            FormatConverter converter = converterFactory.getConverter(format);
            byte[] result = converter.convert(complexDoc, options);
            
            assertNotNull(result, format + "转换结果不应为空");
            assertTrue(result.length > 0, format + "转换结果应有内容");
            
            System.out.println("✅ " + format + "复杂内容测试通过 - 文件大小: " + result.length + " 字节");
        }
    }
    
    @Test
    void testAllConvertersWithEmptyContent() {
        // 测试空内容处理
        Doc emptyDoc = new Doc();
        emptyDoc.setDocId("empty-test-doc");
        emptyDoc.setDocTitle("空内容测试");
        emptyDoc.setDocContent("");
        emptyDoc.setCreateUser("test-user");
        emptyDoc.setCreateTime(LocalDateTime.now());
        emptyDoc.setUpdateTime(LocalDateTime.now());
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        // 测试所有格式
        for (ExportFormat format : ExportFormat.values()) {
            FormatConverter converter = converterFactory.getConverter(format);
            byte[] result = converter.convert(emptyDoc, options);
            
            assertNotNull(result, format + "空内容转换结果不应为空");
            assertTrue(result.length > 0, format + "空内容转换结果应有内容");
            
            System.out.println("✅ " + format + "空内容测试通过 - 文件大小: " + result.length + " 字节");
        }
    }
    
    /**
     * 创建测试内容
     */
    private String createTestContent() {
        return """
            # 主标题
            
            这是一个测试文档，包含各种Markdown元素。
            
            ## 文本格式
            
            这里有**粗体文本**和*斜体文本*，还有`行内代码`。
            
            ## 列表
            
            无序列表：
            - 项目1
            - 项目2
            - 项目3
            
            有序列表：
            1. 第一项
            2. 第二项
            3. 第三项
            
            ## 代码块
            
            ```java
            public class Test {
                public static void main(String[] args) {
                    System.out.println("Hello World!");
                }
            }
            ```
            
            ## 表格
            
            | 列1 | 列2 | 列3 |
            |-----|-----|-----|
            | 数据1 | 数据2 | 数据3 |
            | 数据4 | 数据5 | 数据6 |
            
            ## 引用
            
            > 这是一个引用块
            > 可以包含多行内容
            """;
    }
    
    /**
     * 创建复杂内容
     */
    private String createComplexContent() {
        return """
            # 复杂内容测试文档
            
            ## 中文字符测试
            
            这是一个包含中文字符的段落：你好世界！测试各种中文标点符号，。！？；：
            
            ## 特殊字符测试
            
            表情符号：😀 😃 😄 😁 😆 😅 😂 🤣
            特殊符号：© ® ™ § ¶ † ‡ • … ‰ ′ ″ ‴ ※
            数学符号：± × ÷ ∞ ∑ ∏ ∫ ∂ ∆ ∇ ∈ ∉ ∋ ∌
            
            ## 复杂表格
            
            | 功能 | Markdown | HTML | PDF | DOCX |
            |------|----------|------|-----|------|
            | 基本文本 | ✅ | ✅ | ✅ | ✅ |
            | 标题 | ✅ | ✅ | ✅ | ✅ |
            | 列表 | ✅ | ✅ | ✅ | ✅ |
            | 表格 | ✅ | ✅ | ✅ | ✅ |
            | 代码块 | ✅ | ✅ | ✅ | ✅ |
            | 中文支持 | ✅ | ✅ | ✅ | ✅ |
            
            ## 多语言代码示例
            
            Java代码：
            ```java
            public class MultiLanguageTest {
                private String message = "多语言测试";
                
                public void printMessage() {
                    System.out.println(message);
                }
            }
            ```
            
            Python代码：
            ```python
            def test_function():
                message = "Python测试"
                print(message)
                return True
            ```
            
            JavaScript代码：
            ```javascript
            function testFunction() {
                const message = "JavaScript测试";
                console.log(message);
                return true;
            }
            ```
            
            ## 嵌套列表
            
            1. 第一级项目
               - 第二级项目A
               - 第二级项目B
                 - 第三级项目1
                 - 第三级项目2
            2. 第一级项目2
               - 第二级项目C
               - 第二级项目D
            
            ## 长引用块
            
            > 这是一个很长的引用块，用来测试各种格式转换器对于长文本的处理能力。
            > 
            > 引用块可以包含多个段落，每个段落都应该被正确地格式化和渲染。
            > 
            > 引用块中也可以包含**粗体**和*斜体*文本，以及`行内代码`。
            
            ## 混合内容
            
            这个段落包含了**粗体**、*斜体*、`行内代码`、[链接](https://example.com)等多种格式。
            
            还可以包含一些特殊的Unicode字符：α β γ δ ε ζ η θ ι κ λ μ ν ξ ο π ρ σ τ υ φ χ ψ ω
            """;
    }
}