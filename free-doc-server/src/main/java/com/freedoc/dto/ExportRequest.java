package com.freedoc.dto;

import com.freedoc.common.enums.ExportFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 导出请求DTO
 * 
 * @author FreeDoc Team
 */
@Data
public class ExportRequest {
    
    /**
     * 目标ID（文档ID或目录ID）
     */
    @NotBlank(message = "{validation.targetId.notBlank}")
    private String targetId;
    
    /**
     * 导出格式
     */
    @NotNull(message = "{validation.exportFormat.notNull}")
    private ExportFormat format;
    
    /**
     * 是否递归导出（仅目录导出时有效）
     */
    private boolean recursive = false;
    
    /**
     * 导出选项
     */
    private Map<String, Object> options;
    
    /**
     * 导出类型：DOCUMENT（单文档）或 DIRECTORY（目录）
     */
    @NotNull(message = "{validation.exportType.notNull}")
    private ExportType exportType;
    
    /**
     * 导出类型枚举
     */
    public enum ExportType {
        DOCUMENT,
        DIRECTORY
    }
}