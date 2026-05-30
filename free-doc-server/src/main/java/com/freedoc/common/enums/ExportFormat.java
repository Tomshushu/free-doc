package com.freedoc.common.enums;

import lombok.Getter;

/**
 * 导出格式枚举
 * 
 * @author FreeDoc Team
 */
@Getter
public enum ExportFormat {
    
    MARKDOWN("md", "text/markdown", "Markdown"),
    HTML("html", "text/html", "HTML"),
    PDF("pdf", "application/pdf", "PDF"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Word Document");
    
    private final String extension;
    private final String mimeType;
    private final String displayName;
    
    ExportFormat(String extension, String mimeType, String displayName) {
        this.extension = extension;
        this.mimeType = mimeType;
        this.displayName = displayName;
    }
    
    /**
     * 根据扩展名获取导出格式
     * 
     * @param extension 文件扩展名
     * @return 导出格式，如果不支持则返回null
     */
    public static ExportFormat fromExtension(String extension) {
        if (extension == null) {
            return null;
        }
        
        String normalizedExt = extension.toLowerCase().trim();
        if (normalizedExt.startsWith(".")) {
            normalizedExt = normalizedExt.substring(1);
        }
        
        for (ExportFormat format : values()) {
            if (format.extension.equals(normalizedExt)) {
                return format;
            }
        }
        return null;
    }
    
    /**
     * 根据格式名称获取导出格式（不区分大小写）
     * 
     * @param name 格式名称
     * @return 导出格式，如果不支持则返回null
     */
    public static ExportFormat fromName(String name) {
        if (name == null) {
            return null;
        }
        
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * 检查是否为支持的格式
     * 
     * @param format 格式字符串
     * @return 是否支持
     */
    public static boolean isSupported(String format) {
        return fromName(format) != null || fromExtension(format) != null;
    }
}