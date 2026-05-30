package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.config.ExportConfig;
import com.freedoc.entity.Doc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DOCX转换器最终测试
 * 验证所有Markdown元素的转换功能
 * 
 * @author FreeDoc Team
 */
@SpringBootTest
class DocxConverterFinalTest {
    
    private ExportConfig exportConfig;
    private DocxConverter converter;
    
    @BeforeEach
    void setUp() {
        exportConfig = new ExportConfig();
        converter = new DocxConverter(exportConfig);
    }
    
    @Test
    void testConvertDocumentWithAllMarkdownElements() {
        // 创建包含所有Markdown元素的综合测试文档
        Doc comprehensiveDoc = new Doc();
        comprehensiveDoc.setDocId("comprehensive-test-doc");
        comprehensiveDoc.setDocTitle("Markdown元素综合测试文档");
        comprehensiveDoc.setDocContent("""
            # 一级标题 - Heading 1
            
            这是一个包含**粗体文本**、*斜体文本*和`行内代码`的段落。
            
            ## 二级标题 - Heading 2
            
            ### 三级标题 - Heading 3
            
            #### 四级标题 - Heading 4
            
            ##### 五级标题 - Heading 5
            
            ###### 六级标题 - Heading 6
            
            ## 文本格式测试
            
            这是**粗体文本**，这是*斜体文本*，这是`行内代码`。
            
            这是一个包含***粗斜体***文本的段落。
            
            ## 列表测试
            
            ### 无序列表
            - 第一项
            - 第二项
              - 嵌套项目1
              - 嵌套项目2
            - 第三项
            
            ### 有序列表
            1. 第一项
            2. 第二项
            3. 第三项
            
            ## 代码块测试
            
            ```java
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                    System.out.println("你好，世界！");
                }
            }
            ```
            
            ```python
            def hello_world():
                print("Hello, World!")
                print("你好，世界！")
            
            if __name__ == "__main__":
                hello_world()
            ```
            
            ## 引用块测试
            
            > 这是一个简单的引用块。
            
            > 这是一个包含**粗体**和*斜体*文本的引用块。
            > 
            > 引用块可以包含多个段落。
            
            ## 链接测试
            
            这是一个[内联链接](https://www.example.com)的示例。
            
            这是一个[带标题的链接](https://www.example.com "示例网站")。
            
            ## 表格测试
            
            | 列标题1 | 列标题2 | 列标题3 |
            |---------|---------|---------|
            | 行1列1  | 行1列2  | 行1列3  |
            | 行2列1  | 行2列2  | 行2列3  |
            | 中文内容 | English | 123456 |
            
            ### 复杂表格
            
            | 功能 | 支持状态 | 备注 |
            |------|----------|------|
            | **标题** | ✅ 支持 | 1-6级标题 |
            | *文本格式* | ✅ 支持 | 粗体、斜体、代码 |
            | `代码块` | ✅ 支持 | 语法高亮 |
            | 列表 | ✅ 支持 | 有序、无序 |
            | 表格 | ✅ 支持 | 基本表格功能 |
            
            ## 特殊字符测试
            
            ### 中文字符
            这是中文字符测试：你好世界！欢迎使用FreeDoc文档管理系统。
            
            ### 表情符号
            😀 😃 😄 😁 😆 😅 😂 🤣 😊 😇 🙂 🙃 😉 😌 😍 🥰 😘
            
            ### 特殊符号
            © ® ™ § ¶ † ‡ • … ‰ ′ ″ ‴ ※ ℃ ℉ Ω α β γ δ ε
            
            ### 数学符号
            ± × ÷ ∞ ∑ ∏ ∫ ∂ ∆ ∇ ∈ ∉ ∋ ∌ ⊂ ⊃ ⊆ ⊇ ∪ ∩
            
            ## 混合内容测试
            
            这是一个包含**粗体**、*斜体*、`代码`和[链接](https://example.com)的复杂段落。
            
            > 这是一个引用块，包含：
            > - **粗体列表项**
            > - *斜体列表项*
            > - `代码列表项`
            
            ## 结束
            
            这是文档的结尾部分，用于测试完整的转换流程。
            """);
        
        comprehensiveDoc.setCreateUser("test-user");
        comprehensiveDoc.setCreateTime(LocalDateTime.now());
        comprehensiveDoc.setUpdateTime(LocalDateTime.now());
        
        // 使用默认选项转换
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        byte[] result = converter.convert(comprehensiveDoc, options);
        
        // 验证结果
        assertNotNull(result, "转换结果不应为null");
        assertTrue(result.length > 0, "转换结果应有内容");
        
        // 验证DOCX文件格式
        assertTrue(result.length >= 2, "文件大小应足够包含文件头");
        assertEquals(0x50, result[0] & 0xFF, "DOCX文件应以PK开头 - 第一个字节");
        assertEquals(0x4B, result[1] & 0xFF, "DOCX文件应以PK开头 - 第二个字节");
        
        // 验证文件大小合理（应该比基本文档大）
        assertTrue(result.length > 3000, "包含丰富内容的DOCX文件应该有合理的大小");
        
        System.out.println("综合测试文档转换成功，文件大小: " + result.length + " 字节");
    }
    
    @Test
    void testConvertDocumentWithMetadataEnabled() {
        Doc doc = createTestDocument();
        
        ConversionOptions options = ConversionOptions.builder()
            .includeMetadata(true)
            .build();
        
        byte[] result = converter.convert(doc, options);
        
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // 包含元数据的文档应该比不包含元数据的文档大
        ConversionOptions optionsWithoutMetadata = ConversionOptions.builder()
            .includeMetadata(false)
            .build();
        
        byte[] resultWithoutMetadata = converter.convert(doc, optionsWithoutMetadata);
        
        assertTrue(result.length > resultWithoutMetadata.length, 
            "包含元数据的文档应该比不包含元数据的文档大");
    }
    
    @Test
    void testConvertDocumentWithTableOfContents() {
        Doc doc = createTestDocument();
        
        ConversionOptions options = ConversionOptions.builder()
            .includeTableOfContents(true)
            .build();
        
        byte[] result = converter.convert(doc, options);
        
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // 验证DOCX文件格式
        assertEquals(0x50, result[0] & 0xFF);
        assertEquals(0x4B, result[1] & 0xFF);
    }
    
    @Test
    void testConvertLargeDocument() {
        // 创建大文档
        StringBuilder largeContent = new StringBuilder();
        largeContent.append("# 大文档测试\n\n");
        
        for (int i = 1; i <= 100; i++) {
            largeContent.append("## 章节 ").append(i).append("\n\n");
            largeContent.append("这是第").append(i).append("个章节的内容。");
            largeContent.append("包含**粗体**和*斜体*文本。\n\n");
            
            largeContent.append("### 子章节 ").append(i).append(".1\n\n");
            largeContent.append("- 列表项 1\n");
            largeContent.append("- 列表项 2\n");
            largeContent.append("- 列表项 3\n\n");
            
            if (i % 10 == 0) {
                largeContent.append("```java\n");
                largeContent.append("// 代码示例 ").append(i).append("\n");
                largeContent.append("public void method").append(i).append("() {\n");
                largeContent.append("    System.out.println(\"章节 ").append(i).append("\");\n");
                largeContent.append("}\n");
                largeContent.append("```\n\n");
            }
        }
        
        Doc largeDoc = new Doc();
        largeDoc.setDocId("large-test-doc");
        largeDoc.setDocTitle("大文档测试");
        largeDoc.setDocContent(largeContent.toString());
        largeDoc.setCreateUser("test-user");
        largeDoc.setCreateTime(LocalDateTime.now());
        largeDoc.setUpdateTime(LocalDateTime.now());
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        byte[] result = converter.convert(largeDoc, options);
        
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // 验证DOCX文件格式
        assertEquals(0x50, result[0] & 0xFF);
        assertEquals(0x4B, result[1] & 0xFF);
        
        // 大文档应该有相当的大小
        System.out.println("大文档实际文件大小: " + result.length + " 字节");
        assertTrue(result.length > 3000, "大文档转换后应该有相当的文件大小，实际大小: " + result.length);
        
        System.out.println("大文档转换成功，文件大小: " + result.length + " 字节");
    }
    
    @Test
    void testConvertDocumentWithEdgeCases() {
        // 测试边界情况
        Doc edgeCaseDoc = new Doc();
        edgeCaseDoc.setDocId("edge-case-doc");
        edgeCaseDoc.setDocTitle("边界情况测试");
        edgeCaseDoc.setDocContent("""
            # 边界情况测试
            
            ## 空列表
            
            ## 空代码块
            ```
            ```
            
            ## 空引用
            >
            
            ## 空表格
            | | |
            |---|---|
            | | |
            
            ## 连续格式
            **粗体***斜体*`代码`**粗体**
            
            ## 特殊字符组合
            **_粗体斜体_**
            
            ## 嵌套引用
            > 外层引用
            >> 内层引用
            >>> 更深层引用
            
            ## 长标题测试
            # 这是一个非常非常非常非常非常非常非常非常非常非常非常非常长的标题用于测试标题处理能力
            
            ## 长段落测试
            这是一个非常长的段落，用于测试段落处理能力。这个段落包含了很多文字，目的是验证转换器能够正确处理长文本内容，包括自动换行和格式保持。这个段落还包含了**粗体文本**和*斜体文本*以及`行内代码`，用于测试在长段落中的格式处理能力。
            """);
        
        edgeCaseDoc.setCreateUser("test-user");
        edgeCaseDoc.setCreateTime(LocalDateTime.now());
        edgeCaseDoc.setUpdateTime(LocalDateTime.now());
        
        ConversionOptions options = ConversionOptions.defaultOptions();
        
        byte[] result = converter.convert(edgeCaseDoc, options);
        
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // 验证DOCX文件格式
        assertEquals(0x50, result[0] & 0xFF);
        assertEquals(0x4B, result[1] & 0xFF);
        
        System.out.println("边界情况测试转换成功，文件大小: " + result.length + " 字节");
    }
    
    private Doc createTestDocument() {
        Doc doc = new Doc();
        doc.setDocId("test-doc");
        doc.setDocTitle("测试文档");
        doc.setDocContent("""
            # 测试标题
            
            这是测试内容，包含**粗体**和*斜体*。
            
            ## 子标题
            
            - 列表项1
            - 列表项2
            
            ```java
            System.out.println("Hello World");
            ```
            """);
        doc.setCreateUser("test-user");
        doc.setCreateTime(LocalDateTime.now());
        doc.setUpdateTime(LocalDateTime.now());
        
        return doc;
    }
}