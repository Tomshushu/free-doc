package com.freedoc.service.converter;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 转换选项
 * 
 * @author FreeDoc Team
 */
@Data
@Builder
public class ConversionOptions {
    
    /**
     * 是否包含元数据
     */
    @Builder.Default
    private boolean includeMetadata = true;
    
    /**
     * 是否保持格式
     */
    @Builder.Default
    private boolean preserveFormatting = true;
    
    /**
     * 是否启用语法高亮
     */
    @Builder.Default
    private boolean syntaxHighlight = true;
    
    /**
     * 字体设置
     */
    private String fontFamily;
    
    /**
     * 字体大小
     */
    private Integer fontSize;
    
    /**
     * 页面设置
     */
    private String pageSize;
    
    /**
     * 页边距设置
     */
    private Float marginTop;
    private Float marginBottom;
    private Float marginLeft;
    private Float marginRight;
    
    /**
     * 是否包含页眉
     */
    @Builder.Default
    private boolean includeHeader = false;
    
    /**
     * 是否包含页脚
     */
    @Builder.Default
    private boolean includeFooter = true;
    
    /**
     * 是否包含目录
     */
    @Builder.Default
    private boolean includeTableOfContents = false;
    
    /**
     * CSS样式
     */
    private String cssStyle;
    
    /**
     * 自定义选项
     */
    private Map<String, Object> customOptions;
    
    /**
     * 创建默认选项
     * 
     * @return 默认转换选项
     */
    public static ConversionOptions defaultOptions() {
        return ConversionOptions.builder().build();
    }
}