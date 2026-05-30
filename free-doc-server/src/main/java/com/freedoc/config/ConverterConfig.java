package com.freedoc.config;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.service.converter.FormatConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 转换器配置类
 * 
 * @author FreeDoc Team
 */
@Configuration
public class ConverterConfig {
    
    /**
     * 创建转换器映射
     * 
     * @param converters 所有转换器实例
     * @return 格式到转换器的映射
     */
    @Bean
    public Map<ExportFormat, FormatConverter> converterMap(List<FormatConverter> converters) {
        return converters.stream()
                .collect(Collectors.toMap(
                        FormatConverter::getSupportedFormat,
                        Function.identity()
                ));
    }
}