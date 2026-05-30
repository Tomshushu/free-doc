package com.freedoc.service.impl;

import com.freedoc.common.constants.Constants;
import com.freedoc.common.enums.ExportFormat;
import com.freedoc.common.exception.BusinessException;
import com.freedoc.common.exception.ConversionException;
import com.freedoc.common.exception.ExportException;
import com.freedoc.common.exception.ExportLimitExceededException;
import com.freedoc.common.i18n.I18nUtil;
import com.freedoc.common.util.ExportUtil;
import com.freedoc.common.util.SnowflakeIdUtil;
import com.freedoc.common.validator.ExportSecurityValidator;
import com.freedoc.config.ExportConfig;
import com.freedoc.dto.BatchExportResult;
import com.freedoc.dto.ExportResult;
import com.freedoc.dto.ExportedFile;
import com.freedoc.entity.Doc;
import com.freedoc.service.*;
import com.freedoc.service.converter.ConversionOptions;
import com.freedoc.service.converter.ConverterFactory;
import com.freedoc.service.converter.FormatConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 导出服务实现类
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {
    
    private final DocService docService;
    private final DirectoryService directoryService;
    private final DirectoryExportService directoryExportService;
    private final DocumentAccessService documentAccessService;
    private final ExportCacheService exportCacheService;
    private final ExportSecurityValidator exportSecurityValidator;
    private final ConverterFactory converterFactory;
    private final ExportConfig exportConfig;
    
    // 异步任务状态管理
    private final ConcurrentMap<String, String> taskStatusMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ExportResult> taskResultMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ExportProgress> taskProgressMap = new ConcurrentHashMap<>();
    
    /**
     * 导出进度信息
     */
    private static class ExportProgress {
        private int totalFiles;
        private int processedFiles;
        private String currentFile;
        
        public ExportProgress(int totalFiles) {
            this.totalFiles = totalFiles;
            this.processedFiles = 0;
        }
        
        public void incrementProcessed(String filename) {
            this.processedFiles++;
            this.currentFile = filename;
        }
        
        public int getTotalFiles() {
            return totalFiles;
        }
        
        public int getProcessedFiles() {
            return processedFiles;
        }
        
        public String getCurrentFile() {
            return currentFile;
        }
        
        public int getProgress() {
            if (totalFiles == 0) return 0;
            return (int) ((processedFiles * 100.0) / totalFiles);
        }
    }
    
    @Override
    public ExportResult exportDocument(String docId, ExportFormat format, String userId) {
        log.info("开始导出文档: docId={}, format={}, userId={}", docId, format, userId);
        
        try {
            // 1. 权限验证
            validateDocumentAccess(docId, userId);
            
            // 2. 检查缓存
            byte[] cachedContent = exportCacheService.getCachedExport(docId, format, userId);
            if (cachedContent != null) {
                log.debug("使用缓存的导出结果: docId={}, format={}", docId, format);
                return buildExportResult(docId, format, cachedContent, false);
            }
            
            // 3. 获取文档
            Doc doc = getDocumentById(docId, userId);
            
            // 4. 安全验证
            exportSecurityValidator.validateSingleDocumentExport(doc, format);
            
            // 5. 格式转换
            byte[] content = convertDocument(doc, format);
            
            // 6. 验证文件大小
            validateFileSize(content.length, exportConfig.getMaxSingleFileSize());
            
            // 7. 缓存结果
            exportCacheService.cacheExport(docId, format, userId, content);
            
            // 8. 构建结果
            ExportResult result = buildExportResult(docId, format, content, false);
            
            log.info("文档导出完成: docId={}, format={}, size={}", docId, format, content.length);
            return result;
            
        } catch (Exception e) {
            log.error("文档导出失败: docId={}, format={}, userId={}", docId, format, userId, e);
            throw handleExportException(e);
        }
    }
    
    @Override
    public ExportResult exportDirectory(String directoryId, ExportFormat format, boolean recursive, String userId) {
        log.info("开始导出目录: directoryId={}, format={}, recursive={}, userId={}", 
                directoryId, format, recursive, userId);
        
        try {
            // 使用DirectoryExportService进行目录导出
            BatchExportResult batchResult = directoryExportService.exportDirectory(
                    directoryId, format, recursive, userId);
            
            // 转换为ExportResult
            ExportResult result = ExportResult.builder()
                    .filename(batchResult.getZipFilename())
                    .content(batchResult.getZipContent())
                    .mimeType("application/zip")
                    .size(batchResult.getTotalSize())
                    .format(format)
                    .createdAt(batchResult.getCreatedAt())
                    .batch(true)
                    .fileCount(batchResult.getTotalFiles())
                    .build();
            
            log.info("目录导出完成: directoryId={}, format={}, fileCount={}, size={}", 
                    directoryId, format, batchResult.getTotalFiles(), batchResult.getTotalSize());
            return result;
            
        } catch (Exception e) {
            log.error("目录导出失败: directoryId={}, format={}, recursive={}, userId={}", 
                    directoryId, format, recursive, userId, e);
            throw handleExportException(e);
        }
    }
    
    /**
     * 导出目录（带进度回调）
     */
    private ExportResult exportDirectoryWithProgress(String directoryId, ExportFormat format, 
                                                    boolean recursive, String userId, String taskId) {
        log.info("开始导出目录（带进度）: directoryId={}, format={}, recursive={}, userId={}, taskId={}", 
                directoryId, format, recursive, userId, taskId);
        
        try {
            // 获取文档列表
            List<Doc> docs = recursive ? 
                    directoryExportService.getDocumentsRecursively(directoryId, userId) : 
                    directoryExportService.getDocumentsInDirectory(directoryId, userId);
            
            if (docs.isEmpty()) {
                throw new ExportException(Constants.Export.ERROR_DIRECTORY_NOT_FOUND, 
                        I18nUtil.getMessage("error.export.noDocsToExport"));
            }
            
            // 获取进度对象
            ExportProgress progress = taskProgressMap.get(taskId);
            
            // 创建ZIP文件（带进度回调）
            byte[] zipContent = directoryExportService.createExportZip(docs, format, directoryId, 
                    filename -> {
                        if (progress != null) {
                            progress.incrementProcessed(filename);
                            log.debug("导出进度更新: taskId={}, processed={}/{}, current={}", 
                                    taskId, progress.getProcessedFiles(), progress.getTotalFiles(), filename);
                        }
                    });
            
            // 构建导出文件信息
            List<ExportedFile> exportedFiles = docs.stream()
                    .map(doc -> buildExportedFileInfo(doc, format, directoryId))
                    .collect(java.util.stream.Collectors.toList());
            
            // 生成ZIP文件名
            String directoryName = directoryService.getDirectoryName(directoryId);
            String zipFilename = ExportUtil.generateBatchFilename(directoryName, format);
            
            // 构建结果
            ExportResult result = ExportResult.builder()
                    .filename(zipFilename)
                    .content(zipContent)
                    .mimeType("application/zip")
                    .size(zipContent.length)
                    .format(format)
                    .createdAt(LocalDateTime.now())
                    .batch(true)
                    .fileCount(docs.size())
                    .build();
            
            log.info("目录导出完成: directoryId={}, fileCount={}, zipSize={}", 
                    directoryId, docs.size(), zipContent.length);
            
            return result;
            
        } catch (Exception e) {
            log.error("目录导出失败: directoryId={}, format={}, recursive={}", 
                    directoryId, format, recursive, e);
            throw handleExportException(e);
        }
    }
    
    /**
     * 导出项目（带进度回调）
     */
    private ExportResult exportProjectWithProgress(String projectId, ExportFormat format, 
                                                   String userId, String taskId) {
        log.info("开始导出项目（带进度）: projectId={}, format={}, userId={}, taskId={}", 
                projectId, format, userId, taskId);
        
        try {
            // 获取项目下的所有文档
            List<Doc> docs = docService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Doc>()
                    .eq(Doc::getProjectId, projectId)
                    .orderByAsc(Doc::getCreateTime));
            
            if (docs.isEmpty()) {
                throw new ExportException(Constants.Export.ERROR_DIRECTORY_NOT_FOUND, 
                        I18nUtil.getMessage("error.export.noDocsInProject"));
            }
            
            // 获取进度对象
            ExportProgress progress = taskProgressMap.get(taskId);
            
            // 创建ZIP文件（带进度回调，使用"0"作为根目录ID，表示项目根）
            byte[] zipContent = directoryExportService.createExportZip(docs, format, "0", 
                    filename -> {
                        if (progress != null) {
                            progress.incrementProcessed(filename);
                            log.debug("导出进度更新: taskId={}, processed={}/{}, current={}", 
                                    taskId, progress.getProcessedFiles(), progress.getTotalFiles(), filename);
                        }
                    });
            
            // 构建导出文件信息
            List<ExportedFile> exportedFiles = docs.stream()
                    .map(doc -> buildExportedFileInfo(doc, format, "0"))
                    .collect(java.util.stream.Collectors.toList());
            
            // 生成ZIP文件名
            String projectName = getProjectName(projectId, userId);
            String zipFilename = ExportUtil.generateBatchFilename(projectName, format);
            
            // 构建结果
            ExportResult result = ExportResult.builder()
                    .filename(zipFilename)
                    .content(zipContent)
                    .mimeType("application/zip")
                    .size(zipContent.length)
                    .format(format)
                    .createdAt(LocalDateTime.now())
                    .batch(true)
                    .fileCount(docs.size())
                    .build();
            
            log.info("项目导出完成: projectId={}, fileCount={}, zipSize={}", 
                    projectId, docs.size(), zipContent.length);
            
            return result;
            
        } catch (Exception e) {
            log.error("项目导出失败: projectId={}, format={}", projectId, format, e);
            throw handleExportException(e);
        }
    }
    
    /**
     * 构建导出文件信息
     */
    private ExportedFile buildExportedFileInfo(Doc doc, ExportFormat format, String rootDirectoryId) {
        String originalPath = directoryService.getDocumentRelativePath(doc.getDocId(), rootDirectoryId);
        String exportedPath = directoryExportService.buildZipEntryPath(doc, format, rootDirectoryId);
        String filename = ExportUtil.generateSafeFilename(doc.getDocTitle(), format);
        
        return ExportedFile.builder()
                .originalPath(originalPath)
                .exportedPath(exportedPath)
                .filename(filename)
                .format(format)
                .docId(doc.getDocId())
                .docTitle(doc.getDocTitle())
                .build();
    }
    
    /**
     * 获取项目名称
     */
    private String getProjectName(String projectId, String userId) {
        try {
            // 从文档中获取项目信息
            List<Doc> docs = docService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Doc>()
                    .eq(Doc::getProjectId, projectId)
                    .last("LIMIT 1"));
            
            if (!docs.isEmpty()) {
                // 可以通过ProjectService获取项目名称，这里简化处理
                return "Project_" + projectId;
            }
            return "Project Export";
        } catch (Exception e) {
            log.warn("获取项目名称失败: projectId={}", projectId, e);
            return "Project Export";
        }
    }
    
    @Override
    public ExportResult exportProject(String projectId, ExportFormat format, String userId) {
        log.info("开始导出项目: projectId={}, format={}, userId={}", projectId, format, userId);
        
        try {
            // 使用DirectoryExportService进行项目导出
            BatchExportResult batchResult = directoryExportService.exportProject(projectId, format, userId);
            
            // 转换为ExportResult
            ExportResult result = ExportResult.builder()
                    .filename(batchResult.getZipFilename())
                    .content(batchResult.getZipContent())
                    .mimeType("application/zip")
                    .size(batchResult.getTotalSize())
                    .format(format)
                    .createdAt(batchResult.getCreatedAt())
                    .batch(true)
                    .fileCount(batchResult.getTotalFiles())
                    .build();
            
            log.info("项目导出完成: projectId={}, format={}, fileCount={}, size={}", 
                    projectId, format, batchResult.getTotalFiles(), batchResult.getTotalSize());
            return result;
            
        } catch (Exception e) {
            log.error("项目导出失败: projectId={}, format={}, userId={}", projectId, format, userId, e);
            throw handleExportException(e);
        }
    }
    
    @Override
    public String exportDocumentAsync(String docId, ExportFormat format, String userId) {
        String taskId = generateTaskId();
        taskStatusMap.put(taskId, "PROCESSING");
        
        log.info("创建异步文档导出任务: taskId={}, docId={}, format={}", taskId, docId, format);
        
        // 使用配置的线程池异步执行
        CompletableFuture.runAsync(() -> {
            try {
                log.info("开始执行异步文档导出: taskId={}, docId={}", taskId, docId);
                ExportResult result = exportDocument(docId, format, userId);
                taskResultMap.put(taskId, result);
                taskStatusMap.put(taskId, "COMPLETED");
                log.info("异步文档导出完成: taskId={}, docId={}, size={}", taskId, docId, result.getSize());
            } catch (Exception e) {
                log.error("异步文档导出失败: taskId={}, docId={}", taskId, docId, e);
                taskStatusMap.put(taskId, "FAILED");
            }
        }, exportConfig.exportTaskExecutor());
        
        return taskId;
    }
    
    @Override
    public String exportDirectoryAsync(String directoryId, ExportFormat format, boolean recursive, boolean mergeDocuments, String userId) {
        String taskId = generateTaskId();
        taskStatusMap.put(taskId, "PROCESSING");
        
        log.info("创建异步目录导出任务: taskId={}, directoryId={}, format={}, recursive={}, mergeDocuments={}", 
                taskId, directoryId, format, recursive, mergeDocuments);
        
        // 使用配置的线程池异步执行
        CompletableFuture.runAsync(() -> {
            try {
                log.info("开始执行异步目录导出: taskId={}, directoryId={}", taskId, directoryId);
                
                // 先获取文档列表以初始化进度
                List<Doc> docs = recursive ? 
                        directoryExportService.getDocumentsRecursively(directoryId, userId) : 
                        directoryExportService.getDocumentsInDirectory(directoryId, userId);
                
                // 初始化进度
                ExportProgress progress = new ExportProgress(docs.size());
                taskProgressMap.put(taskId, progress);
                
                // 执行导出（使用带进度回调的方法）
                ExportResult result;
                if (mergeDocuments) {
                    // 合并为单个文档
                    result = exportDirectoryMerged(directoryId, format, recursive, userId, taskId);
                } else {
                    // 正常导出（多个文件）
                    result = exportDirectoryWithProgress(directoryId, format, recursive, userId, taskId);
                }
                
                taskResultMap.put(taskId, result);
                taskStatusMap.put(taskId, "COMPLETED");
                log.info("异步目录导出完成: taskId={}, directoryId={}, fileCount={}, size={}", 
                        taskId, directoryId, result.getFileCount(), result.getSize());
            } catch (Exception e) {
                log.error("异步目录导出失败: taskId={}, directoryId={}", taskId, directoryId, e);
                taskStatusMap.put(taskId, "FAILED");
            } finally {
                // 清理进度信息（可选，或者保留一段时间）
                // taskProgressMap.remove(taskId);
            }
        }, exportConfig.exportTaskExecutor());
        
        return taskId;
    }
    
    @Override
    public String exportProjectAsync(String projectId, ExportFormat format, boolean mergeDocuments, String userId) {
        String taskId = generateTaskId();
        taskStatusMap.put(taskId, "PROCESSING");
        
        log.info("创建异步项目导出任务: taskId={}, projectId={}, format={}, mergeDocuments={}", 
                taskId, projectId, format, mergeDocuments);
        
        // 使用配置的线程池异步执行
        CompletableFuture.runAsync(() -> {
            try {
                log.info("开始执行异步项目导出: taskId={}, projectId={}", taskId, projectId);
                
                // 先获取文档列表以初始化进度
                List<Doc> docs = docService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Doc>()
                        .eq(Doc::getProjectId, projectId));
                
                // 初始化进度
                ExportProgress progress = new ExportProgress(docs.size());
                taskProgressMap.put(taskId, progress);
                
                // 执行导出（使用带进度回调的方法）
                ExportResult result;
                if (mergeDocuments) {
                    // 合并为单个文档
                    result = exportProjectMerged(projectId, format, userId, taskId);
                } else {
                    // 正常导出（多个文件）
                    result = exportProjectWithProgress(projectId, format, userId, taskId);
                }
                
                taskResultMap.put(taskId, result);
                taskStatusMap.put(taskId, "COMPLETED");
                log.info("异步项目导出完成: taskId={}, projectId={}, fileCount={}, size={}", 
                        taskId, projectId, result.getFileCount(), result.getSize());
            } catch (Exception e) {
                log.error("异步项目导出失败: taskId={}, projectId={}", taskId, projectId, e);
                taskStatusMap.put(taskId, "FAILED");
            } finally {
                // 清理进度信息（可选，或者保留一段时间）
                // taskProgressMap.remove(taskId);
            }
        }, exportConfig.exportTaskExecutor());
        
        return taskId;
    }
    
    @Override
    public String getExportTaskStatus(String taskId) {
        String status = taskStatusMap.getOrDefault(taskId, "NOT_FOUND");
        
        // 如果任务正在处理中，附加进度信息
        if ("PROCESSING".equals(status)) {
            ExportProgress progress = taskProgressMap.get(taskId);
            if (progress != null) {
                status = String.format("PROCESSING:%d/%d", progress.getProcessedFiles(), progress.getTotalFiles());
            }
        }
        
        log.debug("查询导出任务状态: taskId={}, status={}", taskId, status);
        return status;
    }
    
    @Override
    public String getExportTaskProgress(String taskId) {
        ExportProgress progress = taskProgressMap.get(taskId);
        if (progress != null) {
            return String.format("%d/%d", progress.getProcessedFiles(), progress.getTotalFiles());
        }
        return "0/0";
    }
    
    @Override
    public ExportResult getExportTaskResult(String taskId) {
        ExportResult result = taskResultMap.get(taskId);
        if (result != null) {
            log.info("获取导出任务结果: taskId={}, filename={}, size={}", 
                    taskId, result.getFilename(), result.getSize());
        } else {
            log.warn("导出任务结果不存在: taskId={}", taskId);
        }
        return result;
    }
    
    /**
     * 验证文档访问权限
     */
    private void validateDocumentAccess(String docId, String userId) {
        if (!documentAccessService.hasReadPermission(userId, docId)) {
            throw new ExportException(Constants.Export.ERROR_ACCESS_DENIED, 
                    I18nUtil.getMessage("error.export.noPermissionDoc", docId));
        }
    }
    
    /**
     * 验证目录访问权限
     */
    private void validateDirectoryAccess(String directoryId, String userId) {
        if (!documentAccessService.hasDirectoryAccess(userId, directoryId)) {
            throw new ExportException(Constants.Export.ERROR_ACCESS_DENIED, 
                    I18nUtil.getMessage("error.export.noPermissionDir", directoryId));
        }
    }
    
    /**
     * 获取文档
     */
    private Doc getDocumentById(String docId, String userId) {
        Doc doc = docService.getDocById(docId, userId);
        if (doc == null) {
            throw new ExportException(Constants.Export.ERROR_DOCUMENT_NOT_FOUND, 
                    I18nUtil.getMessage("error.export.DOCUMENT_NOT_FOUND") + ": " + docId);
        }
        return doc;
    }
    
    /**
     * 转换文档格式
     */
    private byte[] convertDocument(Doc doc, ExportFormat format) {
        try {
            FormatConverter converter = converterFactory.getConverter(format);
            ConversionOptions options = ConversionOptions.builder()
                    .includeMetadata(true)
                    .preserveFormatting(true)
                    .build();
            
            return converter.convert(doc, options);
        } catch (Exception e) {
            throw new ConversionException(I18nUtil.getMessage("error.export.conversionFailedWithDetail", e.getMessage()), e);
        }
    }
    
    /**
     * 构建导出结果
     */
    private ExportResult buildExportResult(String docId, ExportFormat format, byte[] content, boolean batch) {
        Doc doc = docService.getById(docId);
        String filename = ExportUtil.generateSafeFilename(
                doc != null ? doc.getDocTitle() : Constants.Export.DEFAULT_FILENAME, 
                format);
        
        return ExportResult.builder()
                .filename(filename)
                .content(content)
                .mimeType(format.getMimeType())
                .size(content.length)
                .format(format)
                .createdAt(LocalDateTime.now())
                .batch(batch)
                .build();
    }
    
    /**
     * 验证文件大小
     */
    private void validateFileSize(long size, long maxSize) {
        if (ExportUtil.isFileSizeExceeded(size, maxSize)) {
            throw new ExportLimitExceededException(
                    I18nUtil.getMessage("error.export.fileSizeExceeded", 
                            ExportUtil.formatFileSize(size), 
                            ExportUtil.formatFileSize(maxSize)));
        }
    }
    
    /**
     * 生成任务ID
     */
    private String generateTaskId() {
        return "export_" + SnowflakeIdUtil.nextId();
    }
    
    /**
     * 处理导出异常
     */
    private RuntimeException handleExportException(Exception e) {
        if (e instanceof ExportException) {
            return (ExportException) e;
        } else if (e instanceof BusinessException) {
            return (BusinessException) e;
        } else {
            return new ExportException(Constants.Export.ERROR_CONVERSION_FAILED, 
                    I18nUtil.getMessage("error.export.unknownError", e.getMessage()), e);
        }
    }
    
    /**
     * 导出目录并合并为单个文档
     */
    private ExportResult exportDirectoryMerged(String directoryId, ExportFormat format, 
                                              boolean recursive, String userId, String taskId) {
        log.info("开始合并导出目录: directoryId={}, format={}, recursive={}", directoryId, format, recursive);
        
        try {
            // 获取文档列表（按创建时间排序）
            List<Doc> docs = recursive ? 
                    directoryExportService.getDocumentsRecursively(directoryId, userId) : 
                    directoryExportService.getDocumentsInDirectory(directoryId, userId);
            
            if (docs.isEmpty()) {
                throw new ExportException(Constants.Export.ERROR_DIRECTORY_NOT_FOUND, 
                        I18nUtil.getMessage("error.export.noDocsToExport"));
            }
            
            // 按创建时间排序
            docs.sort(java.util.Comparator.comparing(Doc::getCreateTime));
            
            // 合并文档内容
            StringBuilder mergedContent = new StringBuilder();
            ExportProgress progress = taskProgressMap.get(taskId);
            
            for (Doc doc : docs) {
                // 添加文档标题作为分隔
                mergedContent.append("# ").append(doc.getDocTitle()).append("\n\n");
                // 添加文档内容
                mergedContent.append(doc.getDocContent()).append("\n\n");
                mergedContent.append("---\n\n");  // 分隔线
                
                // 更新进度
                if (progress != null) {
                    progress.incrementProcessed(doc.getDocTitle());
                }
            }
            
            // 创建合并后的文档对象
            Doc mergedDoc = new Doc();
            String directoryName = directoryService.getDirectoryName(directoryId);
            mergedDoc.setDocTitle(directoryName + "_merged");
            mergedDoc.setDocContent(mergedContent.toString());
            
            // 转换格式
            byte[] content = convertDocument(mergedDoc, format);
            
            // 构建结果
            String filename = ExportUtil.generateSafeFilename(mergedDoc.getDocTitle(), format);
            ExportResult result = ExportResult.builder()
                    .filename(filename)
                    .content(content)
                    .mimeType(format.getMimeType())
                    .size(content.length)
                    .format(format)
                    .createdAt(LocalDateTime.now())
                    .batch(false)
                    .fileCount(docs.size())
                    .build();
            
            log.info("合并导出目录完成: directoryId={}, mergedFileCount={}, size={}", 
                    directoryId, docs.size(), content.length);
            
            return result;
            
        } catch (Exception e) {
            log.error("合并导出目录失败: directoryId={}, format={}", directoryId, format, e);
            throw handleExportException(e);
        }
    }
    
    /**
     * 导出项目并合并为单个文档
     */
    private ExportResult exportProjectMerged(String projectId, ExportFormat format, 
                                            String userId, String taskId) {
        log.info("开始合并导出项目: projectId={}, format={}", projectId, format);
        
        try {
            // 获取项目下的所有文档（按创建时间排序）
            List<Doc> docs = docService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Doc>()
                    .eq(Doc::getProjectId, projectId)
                    .orderByAsc(Doc::getCreateTime));
            
            if (docs.isEmpty()) {
                throw new ExportException(Constants.Export.ERROR_DIRECTORY_NOT_FOUND, 
                        I18nUtil.getMessage("error.export.noDocsInProject"));
            }
            
            // 合并文档内容
            StringBuilder mergedContent = new StringBuilder();
            ExportProgress progress = taskProgressMap.get(taskId);
            
            for (Doc doc : docs) {
                // 添加文档标题作为分隔
                mergedContent.append("# ").append(doc.getDocTitle()).append("\n\n");
                // 添加文档内容
                mergedContent.append(doc.getDocContent()).append("\n\n");
                mergedContent.append("---\n\n");  // 分隔线
                
                // 更新进度
                if (progress != null) {
                    progress.incrementProcessed(doc.getDocTitle());
                }
            }
            
            // 创建合并后的文档对象
            Doc mergedDoc = new Doc();
            String projectName = getProjectName(projectId, userId);
            mergedDoc.setDocTitle(projectName + "_merged");
            mergedDoc.setDocContent(mergedContent.toString());
            
            // 转换格式
            byte[] content = convertDocument(mergedDoc, format);
            
            // 构建结果
            String filename = ExportUtil.generateSafeFilename(mergedDoc.getDocTitle(), format);
            ExportResult result = ExportResult.builder()
                    .filename(filename)
                    .content(content)
                    .mimeType(format.getMimeType())
                    .size(content.length)
                    .format(format)
                    .createdAt(LocalDateTime.now())
                    .batch(false)
                    .fileCount(docs.size())
                    .build();
            
            log.info("合并导出项目完成: projectId={}, mergedFileCount={}, size={}", 
                    projectId, docs.size(), content.length);
            
            return result;
            
        } catch (Exception e) {
            log.error("合并导出项目失败: projectId={}, format={}", projectId, format, e);
            throw handleExportException(e);
        }
    }
}
