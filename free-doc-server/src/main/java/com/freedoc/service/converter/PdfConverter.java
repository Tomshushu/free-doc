package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.config.ExportConfig;
import com.freedoc.entity.Doc;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * PDF格式转换器
 * 使用 Flexmark PDF Converter（基于 OpenHTMLToPDF）
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PdfConverter implements FormatConverter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final ExportConfig exportConfig;
    
    // 懒加载的 Parser
    private volatile Parser parser;
    
    @Override
    public ExportFormat getSupportedFormat() {
        return ExportFormat.PDF;
    }
    
    @Override
    public byte[] convert(Doc doc, ConversionOptions options) {
        if (doc == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        
        log.debug("开始转换文档 {} 为PDF格式", doc.getDocId());
        
        try {
            // 1. 构建完整的 Markdown 内容（包含元数据）
            String fullMarkdown = buildFullMarkdown(doc, options);
            
            // 2. 使用 Flexmark PDF Converter 直接转换
            byte[] pdfBytes = convertMarkdownToPdf(fullMarkdown);
            
            log.debug("PDF转换完成，文档 {} 大小: {} 字节", doc.getDocId(), pdfBytes.length);
            return pdfBytes;
            
        } catch (Exception e) {
            log.error("PDF转换失败，文档ID: {}", doc.getDocId(), e);
            throw new RuntimeException("PDF conversion failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建完整的 Markdown 内容
     */
    private String buildFullMarkdown(Doc doc, ConversionOptions options) {
        StringBuilder markdown = new StringBuilder();
        
        // 添加文档标题
        if (doc.getDocTitle() != null && !doc.getDocTitle().trim().isEmpty()) {
            markdown.append("# ").append(doc.getDocTitle()).append("\n\n");
        }
        
        // 添加元数据（如果启用）
        if (options.isIncludeMetadata()) {
            markdown.append("---\n\n");
            markdown.append("## 文档信息\n\n");
            
            if (doc.getDocTitle() != null) {
                markdown.append("**标题:** ").append(doc.getDocTitle()).append("\n\n");
            }
            markdown.append("**文档ID:** ").append(doc.getDocId()).append("\n\n");
            
            if (doc.getCreateUser() != null) {
                markdown.append("**创建者:** ").append(doc.getCreateUser()).append("\n\n");
            }
            if (doc.getCreateTime() != null) {
                markdown.append("**创建时间:** ").append(doc.getCreateTime().format(DATE_FORMATTER)).append("\n\n");
            }
            if (doc.getUpdateTime() != null) {
                markdown.append("**更新时间:** ").append(doc.getUpdateTime().format(DATE_FORMATTER)).append("\n\n");
            }
            
            markdown.append("---\n\n");
        }
        
        // 添加文档内容
        String content = doc.getDocContent();
        if (content != null && !content.trim().isEmpty()) {
            markdown.append(content);
        } else {
            markdown.append("*此文档暂无内容*");
        }
        
        return markdown.toString();
    }
    
    /**
     * 使用 Flexmark PDF Converter 将 Markdown 转换为 PDF
     */
    private byte[] convertMarkdownToPdf(String markdown) {
        // 打印 Markdown 内容用于调试
        log.debug("Markdown 内容:\n{}", markdown);
        
        try {
            // 创建临时文件来存储 PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            String html = convertMarkdownToHtmlWithWrapper(markdown);
            
            log.debug("HTML 内容:\n{}", html);
            
            // 使用 PdfRendererBuilder 来支持中文字体
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            builder.useFastMode();
            
            // 重要：必须在 withHtmlContent 之前加载字体
            // 尝试加载中文字体
            boolean fontLoaded = false;
            try {
                // 首选：黑体
                java.io.File simheiFont = new java.io.File("C:\\Windows\\Fonts\\simhei.ttf");
                if (simheiFont.exists()) {
                    builder.useFont(simheiFont, "SimHei");
                    log.debug("成功加载中文字体: SimHei");
                    fontLoaded = true;
                }
            } catch (Exception e) {
                log.warn("无法加载 SimHei 字体: {}", e.getMessage());
            }
            
            if (!fontLoaded) {
                try {
                    // 备选：微软雅黑
                    java.io.File msyhFont = new java.io.File("C:\\Windows\\Fonts\\msyh.ttc");
                    if (msyhFont.exists()) {
                        builder.useFont(msyhFont, "Microsoft YaHei");
                        log.debug("成功加载中文字体: Microsoft YaHei");
                        fontLoaded = true;
                    }
                } catch (Exception e) {
                    log.warn("无法加载 Microsoft YaHei 字体: {}", e.getMessage());
                }
            }
            
            if (!fontLoaded) {
                try {
                    // 备选：宋体
                    java.io.File simsunFont = new java.io.File("C:\\Windows\\Fonts\\simsun.ttc");
                    if (simsunFont.exists()) {
                        builder.useFont(simsunFont, "SimSun");
                        log.debug("成功加载中文字体: SimSun");
                        fontLoaded = true;
                    }
                } catch (Exception e) {
                    log.warn("无法加载 SimSun 字体: {}", e.getMessage());
                }
            }
            
            if (!fontLoaded) {
                log.warn("未能加载任何中文字体，PDF 中的中文可能显示为方框");
            }
            
            // 设置HTML内容和输出流
            builder.withHtmlContent(html, "");
            builder.toStream(outputStream);
            
            // 执行转换
            builder.run();
            
            log.debug("PDF 转换完成，大小: {} 字节", outputStream.size());
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("PDF 转换失败", e);
            throw new RuntimeException("PDF conversion failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将 Markdown 转换为带样式的完整 HTML
     */
    private String convertMarkdownToHtmlWithWrapper(String markdown) {
        // 获取 Parser 和 Renderer
        Parser markdownParser = getParser();
        HtmlRenderer htmlRenderer = HtmlRenderer.builder(getParserOptions()).build();
        
        // 解析并渲染 Markdown
        Node document = markdownParser.parse(markdown);
        String bodyHtml = htmlRenderer.render(document);
        
        // 构建完整的 HTML 文档
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\"/>\n");
        html.append("<style>\n");
        html.append(getCssStyles());
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append(bodyHtml);
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * 获取解析器选项
     */
    private MutableDataSet getParserOptions() {
        MutableDataSet options = new MutableDataSet();
        
        // 配置扩展
        options.set(Parser.EXTENSIONS, Arrays.asList(
            TablesExtension.create(),
            StrikethroughExtension.create(),
            TaskListExtension.create(),
            AutolinkExtension.create()
        ));
        
        // 配置 HTML 渲染选项
        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
        
        return options;
    }
    
    /**
     * 获取 CSS 样式
     */
    private String getCssStyles() {
        return """
            @page {
                size: A4;
                margin: 20mm;
            }
            
            body {
                font-family: 'SimHei', 'Microsoft YaHei', 'SimSun', sans-serif;
                font-size: 12pt;
                line-height: 1.6;
                color: #333;
                max-width: 100%;
            }
            
            h1 {
                font-size: 24pt;
                font-weight: bold;
                margin-top: 0;
                margin-bottom: 20px;
                text-align: center;
                color: #000;
                page-break-after: avoid;
            }
            
            h2 {
                font-size: 18pt;
                font-weight: bold;
                margin-top: 20px;
                margin-bottom: 12px;
                color: #000;
                page-break-after: avoid;
            }
            
            h3 {
                font-size: 16pt;
                font-weight: bold;
                margin-top: 18px;
                margin-bottom: 10px;
                color: #000;
                page-break-after: avoid;
            }
            
            h4 {
                font-size: 14pt;
                font-weight: bold;
                margin-top: 16px;
                margin-bottom: 8px;
                color: #000;
                page-break-after: avoid;
            }
            
            h5, h6 {
                font-size: 12pt;
                font-weight: bold;
                margin-top: 14px;
                margin-bottom: 6px;
                color: #000;
                page-break-after: avoid;
            }
            
            p {
                margin: 10px 0;
                text-align: justify;
            }
            
            code {
                font-family: 'Courier New', 'SimHei', monospace;
                font-size: 10pt;
                background-color: #f5f5f5;
                padding: 2px 5px;
                border-radius: 3px;
                border: 1px solid #e0e0e0;
            }
            
            pre {
                background-color: #f5f5f5;
                border: 1px solid #ddd;
                border-radius: 5px;
                padding: 12px;
                margin: 15px 0;
                overflow-x: auto;
                page-break-inside: avoid;
            }
            
            pre code {
                font-family: 'Courier New', 'SimHei', monospace;
                font-size: 9pt;
                background-color: transparent;
                padding: 0;
                border: none;
                white-space: pre;
                line-height: 1.5;
                display: block;
            }
            
            blockquote {
                margin: 15px 0;
                padding: 10px 20px;
                border-left: 4px solid #0066cc;
                background-color: #f9f9f9;
                font-style: italic;
                page-break-inside: avoid;
            }
            
            ul, ol {
                margin: 10px 0;
                padding-left: 30px;
            }
            
            li {
                margin: 5px 0;
            }
            
            /* 任务列表样式 */
            .task-list-item {
                list-style-type: none;
            }
            
            .task-list-item input[type="checkbox"] {
                margin-right: 5px;
            }
            
            /* 表格样式 */
            table {
                width: 100%;
                border-collapse: collapse;
                margin: 15px 0;
                page-break-inside: avoid;
            }
            
            table th {
                background-color: #f0f0f0;
                font-weight: bold;
                padding: 10px;
                border: 1px solid #ddd;
                text-align: left;
            }
            
            table td {
                padding: 8px 10px;
                border: 1px solid #ddd;
            }
            
            table tr:nth-child(even) {
                background-color: #f9f9f9;
            }
            
            /* 删除线 */
            del {
                text-decoration: line-through;
                color: #888;
            }
            
            hr {
                border: none;
                border-top: 2px solid #ddd;
                margin: 25px 0;
            }
            
            a {
                color: #0066cc;
                text-decoration: underline;
            }
            
            strong {
                font-weight: bold;
            }
            
            em {
                font-style: italic;
            }
            
            img {
                max-width: 100%;
                height: auto;
                display: block;
                margin: 15px auto;
            }
            """;
    }
    
    /**
     * 获取 Markdown 解析器（懒加载）
     */
    private Parser getParser() {
        if (parser == null) {
            synchronized (this) {
                if (parser == null) {
                    parser = Parser.builder(getParserOptions()).build();
                }
            }
        }
        return parser;
    }
}
