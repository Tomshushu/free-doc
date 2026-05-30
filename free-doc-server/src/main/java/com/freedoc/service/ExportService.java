package com.freedoc.service;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.dto.ExportResult;

/**
 * 导出服务接口
 * 
 * @author FreeDoc Team
 */
public interface ExportService {
    
    /**
     * 导出单个文档
     * 
     * @param docId 文档ID
     * @param format 导出格式
     * @param userId 用户ID
     * @return 导出结果
     */
    ExportResult exportDocument(String docId, ExportFormat format, String userId);
    
    /**
     * 导出目录（批量导出）
     * 
     * @param directoryId 目录ID
     * @param format 导出格式
     * @param recursive 是否递归导出子目录
     * @param userId 用户ID
     * @return 导出结果（ZIP文件）
     */
    ExportResult exportDirectory(String directoryId, ExportFormat format, boolean recursive, String userId);
    
    /**
     * 导出项目（所有文档）
     * 
     * @param projectId 项目ID
     * @param format 导出格式
     * @param userId 用户ID
     * @return 导出结果（ZIP文件）
     */
    ExportResult exportProject(String projectId, ExportFormat format, String userId);
    
    /**
     * 异步导出文档
     * 
     * @param docId 文档ID
     * @param format 导出格式
     * @param userId 用户ID
     * @return 导出任务ID
     */
    String exportDocumentAsync(String docId, ExportFormat format, String userId);
    
    /**
     * 异步导出目录
     * 
     * @param directoryId 目录ID
     * @param format 导出格式
     * @param recursive 是否递归导出子目录
     * @param mergeDocuments 是否合并为单个文档
     * @param userId 用户ID
     * @return 导出任务ID
     */
    String exportDirectoryAsync(String directoryId, ExportFormat format, boolean recursive, boolean mergeDocuments, String userId);
    
    /**
     * 异步导出项目
     * 
     * @param projectId 项目ID
     * @param format 导出格式
     * @param mergeDocuments 是否合并为单个文档
     * @param userId 用户ID
     * @return 导出任务ID
     */
    String exportProjectAsync(String projectId, ExportFormat format, boolean mergeDocuments, String userId);
    
    /**
     * 获取异步导出任务状态
     * 
     * @param taskId 任务ID
     * @return 任务状态
     */
    String getExportTaskStatus(String taskId);
    
    /**
     * 获取异步导出任务进度
     * 
     * @param taskId 任务ID
     * @return 进度信息 (格式: "已处理/总数")
     */
    String getExportTaskProgress(String taskId);
    
    /**
     * 获取异步导出结果
     * 
     * @param taskId 任务ID
     * @return 导出结果，如果任务未完成则返回null
     */
    ExportResult getExportTaskResult(String taskId);
}