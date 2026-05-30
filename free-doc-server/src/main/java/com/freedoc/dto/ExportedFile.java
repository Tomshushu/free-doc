package com.freedoc.dto;

import com.freedoc.common.enums.ExportFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出文件信息DTO
 * 
 * @author FreeDoc Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportedFile {
    
    /**
     * 文档ID
     */
    private String docId;
    
    /**
     * 文档标题
     */
    private String docTitle;
    
    /**
     * 原始路径（相对于根目录）
     */
    private String originalPath;
    
    /**
     * 导出后的路径（在ZIP文件中的路径）
     */
    private String exportedPath;
    
    /**
     * 文件名
     */
    private String filename;
    
    /**
     * 导出格式
     */
    private ExportFormat format;
    
    /**
     * 文件大小（字节）
     */
    private long size;
    
    /**
     * 是否导出成功
     */
    private boolean success;
    
    /**
     * 错误信息（如果导出失败）
     */
    private String errorMessage;
}