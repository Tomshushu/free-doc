package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.config.ExportConfig;
import com.freedoc.entity.Doc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * HTML格式转换器
 * 使用CommonMark库将Markdown转换为HTML，支持CSS样式和语法高亮
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HtmlConverter implements FormatConverter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final ExportConfig exportConfig;
    
    // CommonMark扩展
    private static final List<Extension> EXTENSIONS = Arrays.asList(
        TablesExtension.create(),           // 表格支持
        HeadingAnchorExtension.create()     // 标题锚点支持
    );
    
    // 懒加载的Parser和Renderer
    private volatile Parser parser;
    private volatile HtmlRenderer renderer;
    
    @Override
    public ExportFormat getSupportedFormat() {
        return ExportFormat.HTML;
    }
    
    @Override
    public byte[] convert(Doc doc, ConversionOptions options) {
        if (doc == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        
        log.debug("开始转换文档 {} 为HTML格式", doc.getDocId());
        
        try {
            // 获取或创建Parser和Renderer
            Parser markdownParser = getParser();
            HtmlRenderer htmlRenderer = getRenderer();
            
            // 构建完整的HTML文档
            StringBuilder htmlContent = new StringBuilder();
            
            // HTML文档头部
            appendHtmlHeader(htmlContent, doc, options);
            
            // 文档元数据（如果启用）
            if (options.isIncludeMetadata()) {
                appendMetadataSection(htmlContent, doc);
            }
            
            // 转换Markdown内容
            String markdownContent = doc.getDocContent();
            if (markdownContent != null && !markdownContent.trim().isEmpty()) {
                Node document = markdownParser.parse(markdownContent);
                String htmlBody = htmlRenderer.render(document);
                htmlContent.append(htmlBody);
            } else {
                // 如果内容为空，显示文档标题
                htmlContent.append("<h1>")
                          .append(escapeHtml(doc.getDocTitle() != null ? doc.getDocTitle() : "无标题文档"))
                          .append("</h1>\n");
                htmlContent.append("<p><em>此文档暂无内容</em></p>\n");
            }
            
            // HTML文档尾部
            appendHtmlFooter(htmlContent);
            
            byte[] result = htmlContent.toString().getBytes(StandardCharsets.UTF_8);
            log.debug("HTML转换完成，文档 {} 大小: {} 字节", doc.getDocId(), result.length);
            
            return result;
            
        } catch (Exception e) {
            log.error("HTML转换失败，文档ID: {}", doc.getDocId(), e);
            throw new RuntimeException("HTML conversion failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取Markdown解析器（懒加载）
     */
    private Parser getParser() {
        if (parser == null) {
            synchronized (this) {
                if (parser == null) {
                    parser = Parser.builder()
                            .extensions(EXTENSIONS)
                            .build();
                }
            }
        }
        return parser;
    }
    
    /**
     * 获取HTML渲染器（懒加载）
     */
    private HtmlRenderer getRenderer() {
        if (renderer == null) {
            synchronized (this) {
                if (renderer == null) {
                    renderer = HtmlRenderer.builder()
                            .extensions(EXTENSIONS)
                            .attributeProviderFactory(context -> new CodeBlockAttributeProvider())
                            .build();
                }
            }
        }
        return renderer;
    }
    
    /**
     * 添加HTML文档头部
     */
    private void appendHtmlHeader(StringBuilder html, Doc doc, ConversionOptions options) {
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"zh-CN\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>").append(escapeHtml(doc.getDocTitle())).append("</title>\n");
        
        // 添加CSS样式
        String cssContent = loadCssContent(options);
        if (cssContent != null && !cssContent.trim().isEmpty()) {
            html.append("    <style>\n");
            html.append(cssContent);
            html.append("    </style>\n");
        }
        
        // 添加语法高亮支持
        if (options.isSyntaxHighlight()) {
            appendSyntaxHighlightSupport(html);
        }
        
        html.append("</head>\n");
        html.append("<body>\n");
    }
    
    /**
     * 添加HTML文档尾部
     */
    private void appendHtmlFooter(StringBuilder html) {
        html.append("</body>\n");
        html.append("</html>\n");
    }
    
    /**
     * 添加文档元数据部分
     */
    private void appendMetadataSection(StringBuilder html, Doc doc) {
        html.append("<div class=\"document-metadata\">\n");
        html.append("    <h2>文档信息</h2>\n");
        html.append("    <table class=\"metadata-table\">\n");
        
        if (doc.getDocTitle() != null) {
            html.append("        <tr><td><strong>标题</strong></td><td>")
                .append(escapeHtml(doc.getDocTitle())).append("</td></tr>\n");
        }
        
        html.append("        <tr><td><strong>文档ID</strong></td><td>")
            .append(escapeHtml(doc.getDocId())).append("</td></tr>\n");
        
        if (doc.getCreateUser() != null) {
            html.append("        <tr><td><strong>创建者</strong></td><td>")
                .append(escapeHtml(doc.getCreateUser())).append("</td></tr>\n");
        }
        
        if (doc.getCreateTime() != null) {
            html.append("        <tr><td><strong>创建时间</strong></td><td>")
                .append(doc.getCreateTime().format(DATE_FORMATTER)).append("</td></tr>\n");
        }
        
        if (doc.getUpdateTime() != null) {
            html.append("        <tr><td><strong>更新时间</strong></td><td>")
                .append(doc.getUpdateTime().format(DATE_FORMATTER)).append("</td></tr>\n");
        }
        
        html.append("    </table>\n");
        html.append("</div>\n");
        html.append("<hr class=\"metadata-separator\">\n");
    }
    
    /**
     * 加载CSS样式内容
     */
    private String loadCssContent(ConversionOptions options) {
        // 如果提供了自定义CSS，优先使用
        if (options.getCssStyle() != null && !options.getCssStyle().trim().isEmpty()) {
            return options.getCssStyle();
        }
        
        try {
            // 使用默认CSS文件
            String cssPath = exportConfig.getHtml().getCssPath();
            if (cssPath.startsWith("classpath:")) {
                ClassPathResource resource = new ClassPathResource(cssPath.substring("classpath:".length()));
                if (resource.exists()) {
                    return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                }
            }
            
            log.warn("CSS文件未找到: {}", cssPath);
            return getDefaultCss();
            
        } catch (IOException e) {
            log.warn("加载CSS文件失败: {}", e.getMessage());
            return getDefaultCss();
        }
    }
    
    /**
     * 获取默认CSS样式
     */
    private String getDefaultCss() {
        return """
            body {
                font-family: 'Microsoft YaHei', Arial, sans-serif;
                font-size: 14px;
                line-height: 1.6;
                color: #333;
                max-width: 800px;
                margin: 0 auto;
                padding: 20px;
            }
            
            h1, h2, h3, h4, h5, h6 {
                color: #2c3e50;
                margin-top: 1.5em;
                margin-bottom: 0.5em;
            }
            
            h1 {
                border-bottom: 2px solid #3498db;
                padding-bottom: 0.3em;
            }
            
            h2 {
                border-bottom: 1px solid #bdc3c7;
                padding-bottom: 0.2em;
            }
            
            code {
                background-color: #f8f9fa;
                border: 1px solid #e9ecef;
                border-radius: 3px;
                padding: 0.2em 0.4em;
                font-family: 'Consolas', 'Monaco', monospace;
                color: #e74c3c;
            }
            
            pre {
                background-color: #f8f9fa;
                border: 1px solid #e9ecef;
                border-radius: 5px;
                padding: 1em;
                overflow-x: auto;
            }
            
            table {
                border-collapse: collapse;
                width: 100%;
                margin: 1em 0;
            }
            
            th, td {
                border: 1px solid #ddd;
                padding: 8px 12px;
                text-align: left;
            }
            
            th {
                background-color: #f2f2f2;
                font-weight: bold;
            }
            
            .document-metadata {
                background-color: #f8f9fa;
                border: 1px solid #e9ecef;
                border-radius: 5px;
                padding: 1em;
                margin-bottom: 2em;
            }
            
            .metadata-table {
                margin: 0;
            }
            
            .metadata-separator {
                border: none;
                border-top: 1px solid #bdc3c7;
                margin: 2em 0;
            }
            """;
    }
    
    /**
     * 添加语法高亮支持
     */
    private void appendSyntaxHighlightSupport(StringBuilder html) {
        // 添加highlight.js CDN链接
        html.append("    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/default.min.css\">\n");
        html.append("    <script src=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js\"></script>\n");
        html.append("    <script>hljs.highlightAll();</script>\n");
    }
    
    /**
     * HTML转义
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
    
    /**
     * 代码块属性提供器，为代码块添加语法高亮类
     */
    private static class CodeBlockAttributeProvider implements org.commonmark.renderer.html.AttributeProvider {
        @Override
        public void setAttributes(Node node, String tagName, java.util.Map<String, String> attributes) {
            if (node instanceof org.commonmark.node.FencedCodeBlock) {
                org.commonmark.node.FencedCodeBlock codeBlock = (org.commonmark.node.FencedCodeBlock) node;
                String info = codeBlock.getInfo();
                if (info != null && !info.trim().isEmpty()) {
                    // 添加语言类用于语法高亮
                    String language = info.trim().split("\\s+")[0];
                    attributes.put("class", "language-" + language);
                }
            }
        }
    }
}