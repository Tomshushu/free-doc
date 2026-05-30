package com.freedoc.config;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.common.i18n.I18nUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * ExportFormat参数转换器
 * 支持多种格式输入：
 * - 枚举名称：MARKDOWN, HTML, PDF, DOCX（不区分大小写）
 * - 文件扩展名：md, html, pdf, docx（不区分大小写）
 * 
 * @author FreeDoc Team
 */
@Component
public class ExportFormatConverter implements Converter<String, ExportFormat> {
    
    @Override
    public ExportFormat convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException(I18nUtil.getMessage("error.export.formatEmpty"));
        }
        
        String normalized = source.trim();
        
        // 1. 尝试按枚举名称匹配（MARKDOWN, HTML, PDF, DOCX）
        ExportFormat format = ExportFormat.fromName(normalized);
        if (format != null) {
            return format;
        }
        
        // 2. 尝试按文件扩展名匹配（md, html, pdf, docx）
        format = ExportFormat.fromExtension(normalized);
        if (format != null) {
            return format;
        }
        
        // 3. 都不匹配，抛出异常
        throw new IllegalArgumentException(
                I18nUtil.getMessage("error.export.unsupportedFormatWithDetail", source));
    }
}
