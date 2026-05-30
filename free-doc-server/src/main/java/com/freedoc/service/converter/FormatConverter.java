package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.entity.Doc;

/**
 * 格式转换器接口
 * 
 * @author FreeDoc Team
 */
public interface FormatConverter {
    
    /**
     * 转换文档到指定格式
     * 
     * @param doc 文档对象
     * @param options 转换选项
     * @return 转换后的字节数组
     */
    byte[] convert(Doc doc, ConversionOptions options);
    
    /**
     * 获取支持的导出格式
     * 
     * @return 导出格式
     */
    ExportFormat getSupportedFormat();
    
    /**
     * 获取支持的MIME类型
     * 
     * @return MIME类型
     */
    default String getMimeType() {
        return getSupportedFormat().getMimeType();
    }
    
    /**
     * 获取文件扩展名
     * 
     * @return 文件扩展名
     */
    default String getFileExtension() {
        return getSupportedFormat().getExtension();
    }
}