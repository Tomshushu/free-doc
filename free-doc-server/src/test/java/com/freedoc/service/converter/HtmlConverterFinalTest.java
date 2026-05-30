package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.config.ExportConfig;
import com.freedoc.entity.Doc;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HtmlConverter最终验证测试
 * 验证任务2.3的所有要求：
 * - 集成CommonMark库进行Markdown到HTML转换
 * - 添加CSS样式和语法高亮支持
 * - 处理代码块和表格渲染
 */
class HtmlConverterFinalTest {
    
    @Test
    void testTask2_3_Requirements() {
        // 创建HtmlConverter实例
        ExportConfig exportConfig = new ExportConfig();
        HtmlConverter converter = new HtmlConverter(exportConfig);
        
        // 验证支持的格式
        assertEquals(ExportFormat.HTML, converter.getSupportedFormat());
        assertEquals("text/html", converter.getMimeType());
        assertEquals("html", converter.getFileExtension());
        
        // 创建包含各种Markdown元素的测试文档
        Doc testDoc = new Doc();
        testDoc.setDocId("test-doc-001");
        testDoc.setDocTitle("HTML转换测试文档");
        testDoc.setDocContent(createComplexMarkdownContent());
        testDoc.setCreateUser("测试用户");
        testDoc.setCreateTime(LocalDateTime.now());
        testDoc.setUpdateTime(LocalDateTime.now());
        
        // 执行转换
        ConversionOptions options = ConversionOptions.builder()
                .includeMetadata(true)
                .syntaxHighlight(true)
                .build();
        
        byte[] result = converter.convert(testDoc, options);
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        String html = new String(result);
        
        // 验证HTML基本结构
        assertTrue(html.contains("<!DOCTYPE html>"), "应包含HTML5文档类型声明");
        assertTrue(html.contains("<html lang=\"zh-CN\">"), "应包含中文语言标识");
        assertTrue(html.contains("<meta charset=\"UTF-8\">"), "应包含UTF-8编码声明");
        
        // 验证CSS样式支持
        assertTrue(html.contains("<style>"), "应包含CSS样式");
        assertTrue(html.contains("font-family"), "CSS应包含字体设置");
        assertTrue(html.contains("table"), "CSS应包含表格样式");
        assertTrue(html.contains("code"), "CSS应包含代码样式");
        
        // 验证语法高亮支持
        assertTrue(html.contains("highlight.js"), "应包含highlight.js语法高亮库");
        assertTrue(html.contains("hljs.highlightAll()"), "应包含语法高亮初始化代码");
        
        // 验证Markdown到HTML转换（CommonMark库功能）
        assertTrue(html.contains("<h1"), "应正确转换一级标题");
        assertTrue(html.contains("<h2"), "应正确转换二级标题");
        assertTrue(html.contains("<p>"), "应正确转换段落");
        assertTrue(html.contains("<strong>"), "应正确转换粗体文本");
        assertTrue(html.contains("<em>"), "应正确转换斜体文本");
        assertTrue(html.contains("<ul>"), "应正确转换无序列表");
        assertTrue(html.contains("<ol>"), "应正确转换有序列表");
        assertTrue(html.contains("<li>"), "应正确转换列表项");
        
        // 验证代码块渲染
        assertTrue(html.contains("<pre"), "应正确渲染代码块");
        assertTrue(html.contains("<code"), "应正确渲染行内代码");
        assertTrue(html.contains("class=\"language-java\""), "应为代码块添加语言类");
        
        // 验证表格渲染
        assertTrue(html.contains("<table>"), "应正确渲染表格");
        assertTrue(html.contains("<thead>"), "应正确渲染表格头部");
        assertTrue(html.contains("<tbody>"), "应正确渲染表格主体");
        assertTrue(html.contains("<th>"), "应正确渲染表格标题单元格");
        assertTrue(html.contains("<td>"), "应正确渲染表格数据单元格");
        
        // 验证元数据支持
        assertTrue(html.contains("document-metadata"), "应包含文档元数据");
        
        System.out.println("✅ HtmlConverter任务2.3所有要求验证通过：");
        System.out.println("   ✓ 集成CommonMark库进行Markdown到HTML转换");
        System.out.println("   ✓ 添加CSS样式支持");
        System.out.println("   ✓ 添加语法高亮支持");
        System.out.println("   ✓ 处理代码块渲染");
        System.out.println("   ✓ 处理表格渲染");
    }
    
    /**
     * 创建包含各种Markdown元素的复杂内容
     */
    private String createComplexMarkdownContent() {
        return """
            # 一级标题
            
            ## 二级标题
            
            这是一个包含**粗体**和*斜体*文本的段落。
            
            ### 代码示例
            
            行内代码：`System.out.println("Hello World")`
            
            代码块：
            ```java
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            ```
            
            ### 列表
            
            无序列表：
            - 项目1
            - 项目2
            - 项目3
            
            有序列表：
            1. 第一项
            2. 第二项
            3. 第三项
            
            ### 表格
            
            | 列1 | 列2 | 列3 |
            |-----|-----|-----|
            | 数据1 | 数据2 | 数据3 |
            | 数据4 | 数据5 | 数据6 |
            
            ### 引用
            
            > 这是一个引用块
            > 可以包含多行内容
            """;
    }
}