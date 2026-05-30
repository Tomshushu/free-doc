package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.entity.Doc;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Markdown格式转换器
 * 自动修复和标准化Markdown格式，包括表格和代码块
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Component
public class MarkdownConverter implements FormatConverter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final Parser parser;
    private final Formatter formatter;
    
    public MarkdownConverter() {
        // 配置Flexmark选项
        MutableDataSet options = new MutableDataSet();
        
        // 启用扩展
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                StrikethroughExtension.create()
        ));
        
        // 表格格式化选项
        options.set(TablesExtension.COLUMN_SPANS, false);
        options.set(TablesExtension.APPEND_MISSING_COLUMNS, true);
        options.set(TablesExtension.DISCARD_EXTRA_COLUMNS, true);
        options.set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true);
        
        // Formatter基础选项
        options.set(Formatter.MAX_TRAILING_BLANK_LINES, 1);
        
        this.parser = Parser.builder(options).build();
        this.formatter = Formatter.builder(options).build();
    }
    
    @Override
    public ExportFormat getSupportedFormat() {
        return ExportFormat.MARKDOWN;
    }
    
    @Override
    public byte[] convert(Doc doc, ConversionOptions options) {
        if (doc == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        
        log.info("开始转换文档 {} 为Markdown格式", doc.getDocId());
        
        StringBuilder content = new StringBuilder();
        
        // 添加元数据（如果启用）
        if (options.isIncludeMetadata()) {
            appendMetadata(content, doc);
        }
        
        // 获取文档内容
        String docContent = doc.getDocContent();
        if (docContent != null && !docContent.trim().isEmpty()) {
            log.info("开始格式化Markdown内容，长度: {}", docContent.length());
            // 标准化Markdown格式
            String formattedContent = formatMarkdown(docContent);
            log.info("Markdown格式化完成，格式化后长度: {}", formattedContent.length());
            content.append(formattedContent);
        } else {
            // 如果内容为空，至少包含文档标题
            content.append("# ").append(doc.getDocTitle() != null ? doc.getDocTitle() : "无标题文档");
        }
        
        // 确保内容以换行符结尾
        if (content.length() > 0 && !content.toString().endsWith("\n")) {
            content.append("\n");
        }
        
        byte[] result = content.toString().getBytes(StandardCharsets.UTF_8);
        log.info("Markdown转换完成，文档 {} 大小: {} 字节", doc.getDocId(), result.length);
        
        return result;
    }
    
    /**
     * 格式化Markdown内容
     * 自动修复表格格式、代码块格式等
     * 
     * @param markdown 原始Markdown内容
     * @return 格式化后的Markdown内容
     */
    private String formatMarkdown(String markdown) {
        try {
            // 先进行后处理：修复表格和代码块格式
            markdown = postProcessTables(markdown);
            markdown = postProcessCodeBlocks(markdown);
            
            log.debug("Markdown格式化完成");
            return markdown;
            
        } catch (Exception e) {
            log.warn("Markdown格式化失败，返回原始内容: {}", e.getMessage());
            // 如果格式化失败，返回原始内容
            return markdown;
        }
    }
    
    /**
     * 后处理表格格式
     * 确保表格有正确的管道符和对齐，并计算列宽使表格整齐
     * 
     * @param content 格式化后的内容
     * @return 处理后的内容
     */
    private String postProcessTables(String content) {
        String[] lines = content.split("\n");
        StringBuilder result = new StringBuilder();
        
        int i = 0;
        while (i < lines.length) {
            String line = lines[i];
            
            // 检测是否是表格的开始（包含管道符）
            if (line.contains("|") && !line.trim().isEmpty()) {
                // 收集整个表格
                java.util.List<String> tableLines = new java.util.ArrayList<>();
                tableLines.add(line);
                
                // 继续收集后续的表格行
                int j = i + 1;
                while (j < lines.length && lines[j].contains("|") && !lines[j].trim().isEmpty()) {
                    tableLines.add(lines[j]);
                    j++;
                }
                
                // 格式化整个表格
                String formattedTable = formatTable(tableLines);
                result.append(formattedTable);
                
                // 跳过已处理的行
                i = j;
            } else {
                // 非表格行，直接添加
                result.append(line).append("\n");
                i++;
            }
        }
        
        return result.toString();
    }
    
    /**
     * 格式化单个表格
     * 计算每列的最大宽度，使表格对齐
     * 
     * @param tableLines 表格的所有行
     * @return 格式化后的表格
     */
    private String formatTable(java.util.List<String> tableLines) {
        if (tableLines.isEmpty()) {
            return "";
        }
        
        // 解析表格数据
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        int maxColumns = 0;
        boolean[] isSeparatorRow = new boolean[tableLines.size()];
        
        for (int i = 0; i < tableLines.size(); i++) {
            String line = tableLines.get(i);
            
            // 移除首尾空格和管道符
            line = line.trim();
            if (line.startsWith("|")) {
                line = line.substring(1);
            }
            if (line.endsWith("|")) {
                line = line.substring(0, line.length() - 1);
            }
            
            // 分割单元格
            String[] cells = line.split("\\|");
            for (int j = 0; j < cells.length; j++) {
                cells[j] = cells[j].trim();
            }
            
            // 检查是否是分隔行（所有单元格都是 :?-+:? 格式）
            boolean isSeparator = true;
            for (String cell : cells) {
                if (!cell.matches("^:?-+:?$")) {
                    isSeparator = false;
                    break;
                }
            }
            isSeparatorRow[i] = isSeparator;
            
            rows.add(cells);
            maxColumns = Math.max(maxColumns, cells.length);
        }
        
        // 如果第一行后面没有分隔行，自动添加一个
        if (rows.size() > 1 && !isSeparatorRow[1]) {
            String[] separatorCells = new String[maxColumns];
            for (int i = 0; i < maxColumns; i++) {
                separatorCells[i] = "---";
            }
            rows.add(1, separatorCells);
            
            // 更新分隔行标记
            boolean[] newIsSeparatorRow = new boolean[isSeparatorRow.length + 1];
            newIsSeparatorRow[0] = isSeparatorRow[0];
            newIsSeparatorRow[1] = true;
            System.arraycopy(isSeparatorRow, 1, newIsSeparatorRow, 2, isSeparatorRow.length - 1);
            isSeparatorRow = newIsSeparatorRow;
        }
        
        // 计算每列的最大宽度（考虑中文字符占2个宽度）
        int[] columnWidths = new int[maxColumns];
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            // 跳过分隔行的宽度计算
            if (isSeparatorRow[i]) {
                continue;
            }
            for (int j = 0; j < row.length; j++) {
                int width = getDisplayWidth(row[j]);
                columnWidths[j] = Math.max(columnWidths[j], width);
            }
        }
        
        // 确保每列至少有3个字符宽度（用于分隔符）
        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] = Math.max(columnWidths[i], 3);
        }
        
        // 构建格式化后的表格
        StringBuilder table = new StringBuilder();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            String[] row = rows.get(rowIndex);
            table.append("|");
            
            for (int colIndex = 0; colIndex < maxColumns; colIndex++) {
                String cell = colIndex < row.length ? row[colIndex] : "";
                
                // 检查是否是分隔行
                if (isSeparatorRow[rowIndex]) {
                    // 分隔行：使用---填充
                    String alignment = "---";
                    if (cell.startsWith(":") && cell.endsWith(":")) {
                        alignment = ":---:"; // 居中对齐
                    } else if (cell.endsWith(":")) {
                        alignment = "---:"; // 右对齐
                    } else if (cell.startsWith(":")) {
                        alignment = ":---"; // 左对齐
                    }
                    
                    // 填充到列宽
                    int paddingNeeded = columnWidths[colIndex] - alignment.length();
                    if (paddingNeeded > 0) {
                        if (alignment.equals(":---:")) {
                            // 居中对齐：中间填充
                            int leftDashes = (paddingNeeded + 1) / 2;
                            int rightDashes = paddingNeeded - leftDashes;
                            alignment = ":" + "-".repeat(1 + leftDashes) + "-".repeat(1 + rightDashes) + ":";
                        } else if (alignment.equals("---:")) {
                            // 右对齐：左边填充
                            alignment = "-".repeat(3 + paddingNeeded) + ":";
                        } else if (alignment.equals(":---")) {
                            // 左对齐：右边填充
                            alignment = ":" + "-".repeat(3 + paddingNeeded);
                        } else {
                            // 默认：右边填充
                            alignment = "-".repeat(3 + paddingNeeded);
                        }
                    }
                    
                    table.append(" ").append(alignment).append(" |");
                } else {
                    // 普通单元格：填充空格
                    int cellWidth = getDisplayWidth(cell);
                    int paddingNeeded = columnWidths[colIndex] - cellWidth;
                    
                    table.append(" ").append(cell);
                    if (paddingNeeded > 0) {
                        table.append(" ".repeat(paddingNeeded));
                    }
                    table.append(" |");
                }
            }
            
            table.append("\n");
        }
        
        return table.toString();
    }
    
    /**
     * 计算字符串的显示宽度
     * 中文字符算2个宽度，英文字符算1个宽度
     * 
     * @param text 文本
     * @return 显示宽度
     */
    private int getDisplayWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        int width = 0;
        for (char c : text.toCharArray()) {
            // 判断是否是中文字符或全角字符
            if (isCJKCharacter(c)) {
                width += 2;
            } else {
                width += 1;
            }
        }
        return width;
    }
    
    /**
     * 判断是否是CJK字符（中日韩文字）
     * 
     * @param c 字符
     * @return 是否是CJK字符
     */
    private boolean isCJKCharacter(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT
                || block == Character.UnicodeBlock.HIRAGANA
                || block == Character.UnicodeBlock.KATAKANA
                || block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO;
    }
    
    /**
     * 后处理代码块格式
     * 确保代码块使用标准的三个反引号格式
     * 
     * @param content 格式化后的内容
     * @return 处理后的内容
     */
    private String postProcessCodeBlocks(String content) {
        // 确保代码块使用```而不是~~~
        content = content.replaceAll("~~~", "```");
        
        // 确保代码块语言标识符后没有多余空格
        content = content.replaceAll("```\\s+(\\w+)", "```$1");
        
        return content;
    }
    
    /**
     * 添加文档元数据到内容开头
     * 
     * @param content 内容构建器
     * @param doc 文档实体
     */
    private void appendMetadata(StringBuilder content, Doc doc) {
        content.append("---\n");
        content.append("title: ").append(escapeYamlValue(doc.getDocTitle())).append("\n");
        content.append("doc_id: ").append(doc.getDocId()).append("\n");
        
        if (doc.getCreateUser() != null) {
            content.append("author: ").append(escapeYamlValue(doc.getCreateUser())).append("\n");
        }
        
        if (doc.getCreateTime() != null) {
            content.append("created: ").append(doc.getCreateTime().format(DATE_FORMATTER)).append("\n");
        }
        
        if (doc.getUpdateTime() != null) {
            content.append("updated: ").append(doc.getUpdateTime().format(DATE_FORMATTER)).append("\n");
        }
        
        if (doc.getProjectId() != null) {
            content.append("project_id: ").append(doc.getProjectId()).append("\n");
        }
        
        if (doc.getTeamId() != null) {
            content.append("team_id: ").append(doc.getTeamId()).append("\n");
        }
        
        content.append("---\n\n");
    }
    
    /**
     * 转义YAML值中的特殊字符
     * 
     * @param value 原始值
     * @return 转义后的值
     */
    private String escapeYamlValue(String value) {
        if (value == null) {
            return "";
        }
        
        // 如果包含特殊字符，用引号包围
        if (value.contains(":") || value.contains("\"") || value.contains("'") || 
            value.contains("\n") || value.contains("\r") || value.trim().isEmpty()) {
            return "\"" + value.replace("\"", "\\\"") + "\"";
        }
        
        return value;
    }
}