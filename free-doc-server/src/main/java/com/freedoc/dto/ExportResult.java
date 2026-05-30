package com.freedoc.dto;

import com.freedoc.common.enums.ExportFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 导出结果DTO
 * 
 * @author FreeDoc Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResult {
    
    /**
     * 文件名
     */
    private String filename;
    
    /**
     * 文件内容
     */
    private byte[] content;
    
    /**
     * MIME类型
     */
    private String mimeType;
    
    /**
     * 文件大小（字节）
     */
    private long size;
    
    /**
     * 导出格式
     */
    private ExportFormat format;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 是否为批量导出（ZIP文件）
     */
    private boolean batch;
    
    /**
     * 批量导出时的文件数量
     */
    private Integer fileCount;
}