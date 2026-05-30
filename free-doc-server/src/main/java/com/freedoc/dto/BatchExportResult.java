package com.freedoc.dto;

import com.freedoc.common.enums.ExportFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量导出结果DTO
 * 
 * @author FreeDoc Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchExportResult {
    
    /**
     * ZIP文件名
     */
    private String zipFilename;
    
    /**
     * ZIP文件内容
     */
    private byte[] zipContent;
    
    /**
     * 导出的文件列表
     */
    private List<ExportedFile> files;
    
    /**
     * 总文件数
     */
    private int totalFiles;
    
    /**
     * 总大小（字节）
     */
    private long totalSize;
    
    /**
     * 导出格式
     */
    private ExportFormat format;
    
    /**
     * 是否递归导出
     */
    private boolean recursive;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}