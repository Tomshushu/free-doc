package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.config.ExportConfig;
import com.freedoc.entity.Doc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DOCX转换器测试
 * 
 * @author FreeDoc Team
 */
class DocxConverterTest {
    
    private ExportConfig exportConfig;
    private DocxConverter converter;
    private Doc testDoc;
    
    @BeforeEach
    void setUp() {
        // 使用真实的配置对象
        exportConfig = new ExportConfig();
        converter = new DocxConverter(exportConfig);
        
        // 创建测试文档
        testDoc = new Doc();
        testDoc.setDocId("test-doc-001");
        testDoc.setDocTitle("测试文档标题");
        testDoc.setDocContent("# 标题1\n\n这是一个**粗体**文本和*斜体*文本的示例。\n\n## 标题2\n\n- 列表项1\n- 列表项2\n\n```java\nSystem.out.println(\"Hello World\");\n```\n\n> 这是一个引用块");
        testDoc.setCreateUser("test-user");
        testDoc.setCreateTime(LocalDateTime.now());
        testDoc.setUpdateTime(LocalDateTime.now());
    }
    
    @Test
    void testGetSupportedFormat() {
        assertEquals(ExportFormat.DOCX, converter.getSupportedFormat());
    }
    
    @Test
    void testGetMimeType() {
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", converter.getMimeType());
    }
    
    @Test
    void testGetFileExtension() {
        assertEquals("docx", converter.getFileExtension());
    }
    
    @Test
    void testConvertBasicDocument() {
        // 使用默认选项转换文档
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        byte[] result = converter.convert(testDoc, options);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // 验证DOCX文件头（DOCX文件是ZIP格式，以PK开头）
        assertTrue(result.length >= 2);
        assertEquals(0x50, result[0] & 0xFF); // 'P'
        assertEquals(0x4B, result[1] & 0xFF); // 'K'
    }
    
    @Test
    void testConvertEmptyDocument() {
        // 创建空内容文档
        Doc emptyDoc = new Doc();
        emptyDoc.setDocId("empty-doc");
        emptyDoc.setDocTitle("空文档");
        emptyDoc.setDocContent("");
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        byte[] result = converter.convert(emptyDoc, options);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // 验证DOCX文件头
        assertTrue(result.length >= 2);
        assertEquals(0x50, result[0] & 0xFF); // 'P'
        assertEquals(0x4B, result[1] & 0xFF); // 'K'
    }
    
    @Test
    void testConvertWithMetadata() {
        // 启用元数据
        ConversionOptions options = ConversionOptions.builder()
            .includeMetadata(true)
            .build();
        
        byte[] result = converter.convert(testDoc, options);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // 验证DOCX文件头
        assertTrue(result.length >= 2);
        assertEquals(0x50, result[0] & 0xFF); // 'P'
        assertEquals(0x4B, result[1] & 0xFF); // 'K'
    }
    
    @Test
    void testConvertWithoutMetadata() {
        // 禁用元数据
        ConversionOptions options = ConversionOptions.builder()
            .includeMetadata(false)
            .build();
        
        byte[] result = converter.convert(testDoc, options);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // 验证DOCX文件头
        assertTrue(result.length >= 2);
        assertEquals(0x50, result[0] & 0xFF); // 'P'
        assertEquals(0x4B, result[1] & 0xFF); // 'K'
    }
    
    @Test
    void testConvertNullDocument() {
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        // 验证空文档抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            converter.convert(null, options);
        });
    }
    
    @Test
    void testConvertComplexMarkdown() {
        // 创建包含复杂Markdown的文档
        Doc complexDoc = new Doc();
        complexDoc.setDocId("complex-doc");
        complexDoc.setDocTitle("复杂Markdown文档");
        complexDoc.setDocContent("""
            # 主标题
            
            这是一个包含**中文字符**的段落，还有*斜体*和`行内代码`。
            
            ## 二级标题
            
            ### 三级标题
            
            #### 四级标题
            
            无序列表：
            - 项目1
            - 项目2
            - 项目3
            
            有序列表：
            1. 第一项
            2. 第二项
            3. 第三项
            
            代码块：
            ```java
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello, 世界!");
                }
            }
            ```
            
            引用块：
            > 这是一个引用块的示例
            > 可以包含多行内容
            > 支持**格式化**文本
            
            链接测试：
            [FreeDoc官网](https://freedoc.com)
            
            表格：
            | 列1 | 列2 | 列3 |
            |-----|-----|-----|
            | 数据1 | 数据2 | 数据3 |
            | 中文 | English | 123 |
            """);
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        byte[] result = converter.convert(complexDoc, options);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // 验证DOCX文件头
        assertTrue(result.length >= 2);
        assertEquals(0x50, result[0] & 0xFF); // 'P'
        assertEquals(0x4B, result[1] & 0xFF); // 'K'
    }
    
    @Test
    void testConvertDocumentWithSpecialCharacters() {
        // 创建包含特殊字符的文档
        Doc specialDoc = new Doc();
        specialDoc.setDocId("special-doc");
        specialDoc.setDocTitle("特殊字符测试");
        specialDoc.setDocContent("""
            # 特殊字符测试
            
            ## 中文字符
            你好世界！这是中文测试。
            
            ## 表情符号
            😀 😃 😄 😁 😆 😅 😂 🤣
            
            ## 特殊符号
            © ® ™ § ¶ † ‡ • … ‰ ′ ″ ‴ ※
            
            ## 数学符号
            ± × ÷ ∞ ∑ ∏ ∫ ∂ ∆ ∇ ∈ ∉ ∋ ∌
            
            ## 货币符号
            $ € £ ¥ ¢ ₹ ₽ ₩ ₪ ₫
            """);
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        byte[] result = converter.convert(specialDoc, options);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // 验证DOCX文件头
        assertTrue(result.length >= 2);
        assertEquals(0x50, result[0] & 0xFF); // 'P'
        assertEquals(0x4B, result[1] & 0xFF); // 'K'
    }
    
    @Test
    void testConvertDocumentWithOnlyTitle() {
        // 创建只有标题没有内容的文档
        Doc titleOnlyDoc = new Doc();
        titleOnlyDoc.setDocId("title-only-doc");
        titleOnlyDoc.setDocTitle("只有标题的文档");
        titleOnlyDoc.setDocContent(null);
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        byte[] result = converter.convert(titleOnlyDoc, options);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // 验证DOCX文件头
        assertTrue(result.length >= 2);
        assertEquals(0x50, result[0] & 0xFF); // 'P'
        assertEquals(0x4B, result[1] & 0xFF); // 'K'
    }
    
    @Test
    void testConvertDocumentWithWhitespaceContent() {
        // 创建只有空白字符的文档
        Doc whitespaceDoc = new Doc();
        whitespaceDoc.setDocId("whitespace-doc");
        whitespaceDoc.setDocTitle("空白字符文档");
        whitespaceDoc.setDocContent("   \n\n\t\t  \n  ");
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        byte[] result = converter.convert(whitespaceDoc, options);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // 验证DOCX文件头
        assertTrue(result.length >= 2);
        assertEquals(0x50, result[0] & 0xFF); // 'P'
        assertEquals(0x4B, result[1] & 0xFF); // 'K'
    }
}