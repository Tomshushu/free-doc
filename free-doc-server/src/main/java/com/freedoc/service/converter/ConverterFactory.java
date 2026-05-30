package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.common.exception.ConversionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 格式转换器工厂
 * 
 * @author FreeDoc Team
 */
@Component
@RequiredArgsConstructor
public class ConverterFactory {
    
    private final Map<ExportFormat, FormatConverter> converters;
    
    /**
     * 获取指定格式的转换器
     * 
     * @param format 导出格式
     * @return 格式转换器
     * @throws ConversionException 如果不支持该格式
     */
    public FormatConverter getConverter(ExportFormat format) {
        FormatConverter converter = converters.get(format);
        if (converter == null) {
            throw new ConversionException("Unsupported export format: " + format);
        }
        return converter;
    }
    
    /**
     * 检查是否支持指定格式
     * 
     * @param format 导出格式
     * @return 是否支持
     */
    public boolean isSupported(ExportFormat format) {
        return converters.containsKey(format);
    }
    
    /**
     * 获取所有支持的格式
     * 
     * @return 支持的格式列表
     */
    public java.util.List<ExportFormat> getSupportedFormats() {
        return new java.util.ArrayList<>(converters.keySet());
    }
}