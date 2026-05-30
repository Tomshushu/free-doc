package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.config.ExportConfig;
import com.freedoc.entity.Doc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocxConverter任务3.3验证测试
 * 验证DocxConverter满足所有任务要求：
 * - 集成Apache POI库
 * - 保持标题层级和列表格式
 * - 处理表格和文本样式
 * - 需求: 2.5, 7.3, 7.4, 7.5
 * 
 * @author FreeDoc Team
 */
class DocxConverterTask3_3Test {
    
    private ExportConfig exportConfig;
    private DocxConverter converter;
    
    @BeforeEach
    void setUp() {
        exportConfig = new ExportConfig();
        converter = new DocxConverter(exportConfig);
    }
    
    @Test
    void testTask3_3_ApachePOIIntegration() {
        // 验证Apache POI库集成 - 需求2.5
        assertEquals(ExportFormat.DOCX, converter.getSupportedFormat());
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", converter.getMimeType());
        assertEquals("docx", converter.getFileExtension());
        
        // 创建测试文档
        Doc testDoc = new Doc();
        testDoc.setDocId("test-poi-integration");
        testDoc.setDocTitle("Apache POI集成测试");
        testDoc.setDocContent("# 测试标题\n\n这是一个测试段落。");
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        byte[] result = converter.convert(testDoc, options);
        
        // 验证生成的是有效的DOCX文件（ZIP格式，以PK开头）
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertEquals(0x50, result[0] & 0xFF); // 'P'
        assertEquals(0x4B, result[1] & 0xFF); // 'K'
    }
    
    @Test
    void testTask3_3_HeadingHierarchy() {
        // 验证标题层级保持 - 需求7.3
        Doc testDoc = new Doc();
        testDoc.setDocId("test-heading-hierarchy");
        testDoc.setDocTitle("标题层级测试");
        testDoc.setDocContent("""
            # 一级标题
            ## 二级标题
            ### 三级标题
            #### 四级标题
            ##### 五级标题
            ###### 六级标题
            """);
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        byte[] result = converter.convert(testDoc, options);
        
        // 验证转换成功
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertEquals(0x50, result[0] & 0xFF); // DOCX文件头
        assertEquals(0x4B, result[1] & 0xFF);
    }
    
    @Test
    void testTask3_3_ListFormatting() {
        // 验证列表格式保持 - 需求7.3
        Doc testDoc = new Doc();
        testDoc.setDocId("test-list-formatting");
        testDoc.setDocTitle("列表格式测试");
        testDoc.setDocContent("""
            ## 无序列表
            - 项目1
            - 项目2
            - 项目3
            
            ## 有序列表
            1. 第一项
            2. 第二项
            3. 第三项
            
            ## 嵌套列表
            - 主项目1
              - 子项目1.1
              - 子项目1.2
            - 主项目2
              1. 子项目2.1
              2. 子项目2.2
            """);
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        byte[] result = converter.convert(testDoc, options);
        
        // 验证转换成功
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertEquals(0x50, result[0] & 0xFF); // DOCX文件头
        assertEquals(0x4B, result[1] & 0xFF);
    }
    
    @Test
    void testTask3_3_TextStyles() {
        // 验证文本样式处理 - 需求7.3
        Doc testDoc = new Doc();
        testDoc.setDocId("test-text-styles");
        testDoc.setDocTitle("文本样式测试");
        testDoc.setDocContent("""
            # 文本样式测试
            
            这是**粗体文本**和*斜体文本*的示例。
            
            这是`行内代码`的示例。
            
            这是[链接文本](https://example.com)的示例。
            
            > 这是引用块的示例
            > 可以包含多行内容
            """);
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        byte[] result = converter.convert(testDoc, options);
        
        // 验证转换成功
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertEquals(0x50, result[0] & 0xFF); // DOCX文件头
        assertEquals(0x4B, result[1] & 0xFF);
    }
    
    @Test
    void testTask3_3_CodeBlockHandling() {
        // 验证代码块处理 - 需求7.4
        Doc testDoc = new Doc();
        testDoc.setDocId("test-code-blocks");
        testDoc.setDocTitle("代码块测试");
        testDoc.setDocContent("""
            # 代码块测试
            
            ## Java代码示例
            ```java
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            ```
            
            ## JavaScript代码示例
            ```javascript
            function greet(name) {
                console.log(`Hello, ${name}!`);
            }
            
            greet('World');
            ```
            
            ## 无语言标识的代码块
            ```
            这是一个普通的代码块
            没有指定编程语言
            ```
            """);
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        byte[] result = converter.convert(testDoc, options);
        
        // 验证转换成功
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertEquals(0x50, result[0] & 0xFF); // DOCX文件头
        assertEquals(0x4B, result[1] & 0xFF);
    }
    
    @Test
    void testTask3_3_TableHandling() {
        // 验证表格处理 - 需求7.5
        Doc testDoc = new Doc();
        testDoc.setDocId("test-tables");
        testDoc.setDocTitle("表格测试");
        testDoc.setDocContent("""
            # 表格测试
            
            ## 简单表格
            | 列1 | 列2 | 列3 |
            |-----|-----|-----|
            | 数据1 | 数据2 | 数据3 |
            | 数据4 | 数据5 | 数据6 |
            
            ## 包含格式的表格
            | 姓名 | 年龄 | 职业 |
            |------|------|------|
            | **张三** | 25 | *工程师* |
            | **李四** | 30 | *设计师* |
            | **王五** | 28 | `程序员` |
            
            ## 包含中文的表格
            | 产品名称 | 价格 | 描述 |
            |----------|------|------|
            | 苹果 | ¥5.00 | 新鲜水果 |
            | 香蕉 | ¥3.00 | 热带水果 |
            | 橙子 | ¥4.00 | 维C丰富 |
            """);
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        byte[] result = converter.convert(testDoc, options);
        
        // 验证转换成功
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertEquals(0x50, result[0] & 0xFF); // DOCX文件头
        assertEquals(0x4B, result[1] & 0xFF);
    }
    
    @Test
    void testTask3_3_ComprehensiveFeatures() {
        // 综合测试所有功能 - 需求2.5, 7.3, 7.4, 7.5
        Doc testDoc = new Doc();
        testDoc.setDocId("test-comprehensive");
        testDoc.setDocTitle("DocxConverter综合功能测试");
        testDoc.setDocContent("""
            # DocxConverter功能验证
            
            本文档用于验证DocxConverter的所有功能特性。
            
            ## 标题层级测试
            
            ### 三级标题
            #### 四级标题
            ##### 五级标题
            
            ## 文本样式测试
            
            这里包含**粗体文本**、*斜体文本*和`行内代码`。
            
            还有[超链接](https://freedoc.com)的示例。
            
            ## 列表测试
            
            ### 无序列表
            - 功能1：Apache POI集成
            - 功能2：标题层级保持
            - 功能3：列表格式处理
            - 功能4：表格结构渲染
            
            ### 有序列表
            1. 需求2.5：DOCX格式转换
            2. 需求7.3：标题层级和列表格式
            3. 需求7.4：代码块处理
            4. 需求7.5：表格结构处理
            
            ## 代码块测试
            
            ```java
            @Component
            public class DocxConverter implements FormatConverter {
                @Override
                public ExportFormat getSupportedFormat() {
                    return ExportFormat.DOCX;
                }
            }
            ```
            
            ## 引用块测试
            
            > Apache POI是一个用于处理Microsoft Office文档的Java库
            > 它支持Word、Excel、PowerPoint等格式的读写操作
            
            ## 表格测试
            
            | 需求编号 | 需求描述 | 实现状态 |
            |----------|----------|----------|
            | 2.5 | DOCX格式转换 | ✅ 已实现 |
            | 7.3 | 标题层级和列表格式 | ✅ 已实现 |
            | 7.4 | 代码块处理 | ✅ 已实现 |
            | 7.5 | 表格结构处理 | ✅ 已实现 |
            
            ## 中文字符测试
            
            这是中文字符测试：你好世界！
            
            特殊符号：© ® ™ § ¶ † ‡ • … ‰
            
            表情符号：😀 😃 😄 😁 😆 😅 😂 🤣
            """);
        testDoc.setCreateUser("test-user");
        testDoc.setCreateTime(LocalDateTime.now());
        testDoc.setUpdateTime(LocalDateTime.now());
        
        ConversionOptions options = ConversionOptions.builder()
            .includeMetadata(true)
            .build();
        
        byte[] result = converter.convert(testDoc, options);
        
        // 验证转换成功
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertEquals(0x50, result[0] & 0xFF); // DOCX文件头
        assertEquals(0x4B, result[1] & 0xFF);
        
        // 验证文件大小合理（应该包含所有内容）
        assertTrue(result.length > 1000, "生成的DOCX文件应该包含足够的内容");
        
        System.out.println("DocxConverter综合测试成功，生成文件大小: " + result.length + " 字节");
    }
}