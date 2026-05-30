package com.freedoc.common.validator;

import com.freedoc.common.constants.Constants;
import com.freedoc.common.enums.ExportFormat;
import com.freedoc.common.exception.ExportLimitExceededException;
import com.freedoc.common.i18n.I18nUtil;
import com.freedoc.config.ExportConfig;
import com.freedoc.dto.ExportRequest;
import com.freedoc.entity.Doc;
import com.freedoc.service.DocumentAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 导出安全验证器
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExportSecurityValidator {
    
    private final ExportConfig exportConfig;
    private final DocumentAccessService documentAccessService;
    
    /**
     * 验证导出请求的安全性
     * 
     * @param request 导出请求
     * @param userId 用户ID
     */
    public void validateExportRequest(ExportRequest request, String userId) {
        log.debug("验证用户 {} 的导出请求: {}", userId, request);
        
        // 验证用户权限
        validateUserPermission(request, userId);
        
        // 验证导出限制
        validateExportLimits(request);
        
        log.debug("导出请求验证通过");
    }
    
    /**
     * 验证单文档导出
     * 
     * @param doc 文档
     * @param format 导出格式
     */
    public void validateSingleDocumentExport(Doc doc, ExportFormat format) {
        log.debug("验证单文档导出: docId={}, format={}", doc.getDocId(), format);
        
        // 验证文档内容大小
        if (doc.getDocContent() != null) {
            long contentSize = doc.getDocContent().getBytes().length;
            validateFileSize(contentSize, false);
        }
        
        log.debug("单文档导出验证通过");
    }
    
    /**
     * 验证批量导出
     * 
     * @param docs 文档列表
     * @param format 导出格式
     */
    public void validateBatchExport(List<Doc> docs, ExportFormat format) {
        log.debug("验证批量导出: fileCount={}, format={}", docs.size(), format);
        
        // 验证文件数量
        validateBatchFileCount(docs.size());
        
        // 估算总大小
        long estimatedSize = estimateBatchExportSize(docs);
        validateFileSize(estimatedSize, true);
        
        log.debug("批量导出验证通过");
    }
    
    /**
     * 验证用户权限
     * 
     * @param request 导出请求
     * @param userId 用户ID
     */
    private void validateUserPermission(ExportRequest request, String userId) {
        boolean hasPermission = false;
        
        if (request.getExportType() == ExportRequest.ExportType.DOCUMENT) {
            hasPermission = documentAccessService.hasReadPermission(userId, request.getTargetId());
        } else if (request.getExportType() == ExportRequest.ExportType.DIRECTORY) {
            hasPermission = documentAccessService.hasDirectoryAccess(userId, request.getTargetId());
        }
        
        if (!hasPermission) {
            log.warn("用户 {} 无权限访问资源 {}", userId, request.getTargetId());
            throw new AccessDeniedException(I18nUtil.getMessage("error.auth.noPermission"));
        }
    }
    
    /**
     * 验证导出限制
     * 
     * @param request 导出请求
     */
    private void validateExportLimits(ExportRequest request) {
        // 这里可以添加更多的限制验证逻辑
        // 例如：估算导出大小、检查文件数量等
        
        if (request.getExportType() == ExportRequest.ExportType.DIRECTORY && request.isRecursive()) {
            // 对于递归目录导出，可以添加更严格的限制
            log.debug("递归目录导出请求，应用额外的安全检查");
        }
    }
    
    /**
     * 验证文件大小是否超出限制
     * 
     * @param size 文件大小
     * @param isBatch 是否为批量导出
     */
    public void validateFileSize(long size, boolean isBatch) {
        long maxSize = isBatch ? exportConfig.getMaxBatchExportSize() : exportConfig.getMaxSingleFileSize();
        
        if (size > maxSize) {
            String errorCode = isBatch ? Constants.Export.ERROR_TOO_MANY_FILES : Constants.Export.ERROR_FILE_TOO_LARGE;
            throw new ExportLimitExceededException(errorCode,
                I18nUtil.getMessage("error.export.fileSizeExceededWithLimit", size, maxSize));
        }
    }
    
    /**
     * 验证批量导出文件数量
     * 
     * @param fileCount 文件数量
     */
    public void validateBatchFileCount(int fileCount) {
        if (fileCount > exportConfig.getMaxBatchFiles()) {
            throw new ExportLimitExceededException(Constants.Export.ERROR_TOO_MANY_FILES,
                I18nUtil.getMessage("error.export.fileCountExceededWithLimit", fileCount, exportConfig.getMaxBatchFiles()));
        }
    }
    
    /**
     * 估算批量导出大小
     * 
     * @param docs 文档列表
     * @return 估算的总大小
     */
    private long estimateBatchExportSize(List<Doc> docs) {
        long totalSize = 0;
        for (Doc doc : docs) {
            if (doc.getDocContent() != null) {
                // 估算转换后的大小（通常比原始内容大1.5-2倍）
                totalSize += doc.getDocContent().getBytes().length * 2;
            }
        }
        return totalSize;
    }
}