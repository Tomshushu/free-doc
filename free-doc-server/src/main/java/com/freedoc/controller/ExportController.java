package com.freedoc.controller;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.common.i18n.I18nUtil;
import com.freedoc.common.result.R;
import com.freedoc.dto.ExportRequest;
import com.freedoc.dto.ExportResult;
import com.freedoc.security.UserPrincipal;
import com.freedoc.service.ExportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 导出控制器
 * 处理文档和目录的导出请求
 * 
 * @author FreeDoc Team
 */
@Slf4j
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Validated
public class ExportController {
    
    private final ExportService exportService;
    
    /**
     * 导出单个文档
     * 支持GET和POST两种方式
     * 
     * @param docId 文档ID
     * @param format 导出格式
     * @param principal 当前用户
     * @return 导出的文件资源
     */
    @GetMapping("/document/{docId}")
    public ResponseEntity<Resource> exportDocumentGet(
            @PathVariable @NotBlank(message = "{validation.docId.notBlank}") String docId,
            @RequestParam @NotNull(message = "{validation.exportFormat.notNull}") ExportFormat format,
            @AuthenticationPrincipal UserPrincipal principal) {
        return exportDocument(docId, format, principal);
    }
    
    /**
     * 导出单个文档（POST方式）
     * 
     * @param docId 文档ID
     * @param format 导出格式
     * @param principal 当前用户
     * @return 导出的文件资源
     */
    @PostMapping("/document/{docId}")
    public ResponseEntity<Resource> exportDocument(
            @PathVariable @NotBlank(message = "{validation.docId.notBlank}") String docId,
            @RequestParam @NotNull(message = "{validation.exportFormat.notNull}") ExportFormat format,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("收到单文档导出请求: docId={}, format={}, userId={}", 
                docId, format, principal.getUserId());
        
        try {
            // 调用导出服务
            ExportResult result = exportService.exportDocument(docId, format, principal.getUserId());
            
            // 构建响应
            ByteArrayResource resource = new ByteArrayResource(result.getContent());
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(result.getMimeType()));
            headers.setContentLength(result.getSize());
            
            // 设置文件名，支持中文
            String encodedFilename = URLEncoder.encode(result.getFilename(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename*=UTF-8''" + encodedFilename);
            
            log.info("单文档导出成功: docId={}, format={}, filename={}, size={}", 
                    docId, format, result.getFilename(), result.getSize());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("单文档导出失败: docId={}, format={}, userId={}", 
                    docId, format, principal.getUserId(), e);
            throw e;
        }
    }
    
    /**
     * 导出目录
     * 支持GET和POST两种方式
     * 
     * @param directoryId 目录ID
     * @param format 导出格式
     * @param recursive 是否递归导出子目录，默认为false
     * @param principal 当前用户
     * @return 导出的ZIP文件资源
     */
    @GetMapping("/directory/{directoryId}")
    public ResponseEntity<Resource> exportDirectoryGet(
            @PathVariable @NotBlank(message = "{validation.directoryId.notBlank}") String directoryId,
            @RequestParam @NotNull(message = "{validation.exportFormat.notNull}") ExportFormat format,
            @RequestParam(defaultValue = "false") boolean recursive,
            @AuthenticationPrincipal UserPrincipal principal) {
        return exportDirectory(directoryId, format, recursive, principal);
    }
    
    /**
     * 导出目录（POST方式）
     * 
     * @param directoryId 目录ID
     * @param format 导出格式
     * @param recursive 是否递归导出子目录，默认为false
     * @param principal 当前用户
     * @return 导出的ZIP文件资源
     */
    @PostMapping("/directory/{directoryId}")
    public ResponseEntity<Resource> exportDirectory(
            @PathVariable @NotBlank(message = "{validation.directoryId.notBlank}") String directoryId,
            @RequestParam @NotNull(message = "{validation.exportFormat.notNull}") ExportFormat format,
            @RequestParam(defaultValue = "false") boolean recursive,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("收到目录导出请求: directoryId={}, format={}, recursive={}, userId={}", 
                directoryId, format, recursive, principal.getUserId());
        
        try {
            // 调用导出服务
            ExportResult result = exportService.exportDirectory(
                    directoryId, format, recursive, principal.getUserId());
            
            // 构建响应
            ByteArrayResource resource = new ByteArrayResource(result.getContent());
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(result.getSize());
            
            // 设置ZIP文件名，支持中文
            String encodedFilename = URLEncoder.encode(result.getFilename(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename*=UTF-8''" + encodedFilename);
            
            log.info("目录导出成功: directoryId={}, format={}, recursive={}, filename={}, fileCount={}, size={}", 
                    directoryId, format, recursive, result.getFilename(), 
                    result.getFileCount(), result.getSize());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("目录导出失败: directoryId={}, format={}, recursive={}, userId={}", 
                    directoryId, format, recursive, principal.getUserId(), e);
            throw e;
        }
    }
    
    /**
     * 导出项目（所有文档）
     * 
     * @param projectId 项目ID
     * @param format 导出格式
     * @param principal 当前用户
     * @return 导出的ZIP文件资源
     */
    @PostMapping("/project/{projectId}")
    public ResponseEntity<Resource> exportProject(
            @PathVariable @NotBlank(message = "{validation.projectId.notBlank}") String projectId,
            @RequestParam @NotNull(message = "{validation.exportFormat.notNull}") ExportFormat format,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("收到项目导出请求: projectId={}, format={}, userId={}", 
                projectId, format, principal.getUserId());
        
        try {
            // 调用导出服务
            ExportResult result = exportService.exportProject(projectId, format, principal.getUserId());
            
            // 构建响应
            ByteArrayResource resource = new ByteArrayResource(result.getContent());
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(result.getSize());
            
            // 设置ZIP文件名，支持中文
            String encodedFilename = URLEncoder.encode(result.getFilename(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename*=UTF-8''" + encodedFilename);
            
            log.info("项目导出成功: projectId={}, format={}, filename={}, fileCount={}, size={}", 
                    projectId, format, result.getFilename(), 
                    result.getFileCount(), result.getSize());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("项目导出失败: projectId={}, format={}, userId={}", 
                    projectId, format, principal.getUserId(), e);
            throw e;
        }
    }
    
    /**
     * 异步导出项目
     * 
     * @param projectId 项目ID
     * @param format 导出格式
     * @param mergeDocuments 是否合并为单个文档，默认为false
     * @param principal 当前用户
     * @return 导出任务ID
     */
    @PostMapping("/project/{projectId}/async")
    public R<String> exportProjectAsync(
            @PathVariable @NotBlank(message = "{validation.projectId.notBlank}") String projectId,
            @RequestParam @NotNull(message = "{validation.exportFormat.notNull}") ExportFormat format,
            @RequestParam(defaultValue = "false") boolean mergeDocuments,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("收到异步项目导出请求: projectId={}, format={}, mergeDocuments={}, userId={}", 
                projectId, format, mergeDocuments, principal.getUserId());
        
        String taskId = exportService.exportProjectAsync(projectId, format, mergeDocuments, principal.getUserId());
        
        log.info("异步项目导出任务已创建: projectId={}, format={}, mergeDocuments={}, taskId={}", 
                projectId, format, mergeDocuments, taskId);
        
        return R.ok(taskId);
    }
    
    /**
     * 通用导出接口（支持请求体）
     * 
     * @param request 导出请求
     * @param principal 当前用户
     * @return 导出的文件资源
     */
    @PostMapping("/export")
    public ResponseEntity<Resource> export(
            @Valid @RequestBody ExportRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("收到通用导出请求: targetId={}, format={}, exportType={}, recursive={}, userId={}", 
                request.getTargetId(), request.getFormat(), request.getExportType(), 
                request.isRecursive(), principal.getUserId());
        
        try {
            ExportResult result;
            
            // 根据导出类型调用相应的服务方法
            switch (request.getExportType()) {
                case DOCUMENT:
                    result = exportService.exportDocument(
                            request.getTargetId(), request.getFormat(), principal.getUserId());
                    break;
                case DIRECTORY:
                    result = exportService.exportDirectory(
                            request.getTargetId(), request.getFormat(), 
                            request.isRecursive(), principal.getUserId());
                    break;
                default:
                    throw new IllegalArgumentException(I18nUtil.getMessage("error.export.unsupportedExportType", request.getExportType()));
            }
            
            // 构建响应
            ByteArrayResource resource = new ByteArrayResource(result.getContent());
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(result.getMimeType()));
            headers.setContentLength(result.getSize());
            
            // 设置文件名，支持中文
            String encodedFilename = URLEncoder.encode(result.getFilename(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename*=UTF-8''" + encodedFilename);
            
            log.info("通用导出成功: targetId={}, format={}, exportType={}, filename={}, size={}", 
                    request.getTargetId(), request.getFormat(), request.getExportType(), 
                    result.getFilename(), result.getSize());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("通用导出失败: targetId={}, format={}, exportType={}, userId={}", 
                    request.getTargetId(), request.getFormat(), request.getExportType(), 
                    principal.getUserId(), e);
            throw e;
        }
    }
    
    /**
     * 异步导出文档
     * 
     * @param docId 文档ID
     * @param format 导出格式
     * @param principal 当前用户
     * @return 导出任务ID
     */
    @PostMapping("/document/{docId}/async")
    public R<String> exportDocumentAsync(
            @PathVariable @NotBlank(message = "{validation.docId.notBlank}") String docId,
            @RequestParam @NotNull(message = "{validation.exportFormat.notNull}") ExportFormat format,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("收到异步单文档导出请求: docId={}, format={}, userId={}", 
                docId, format, principal.getUserId());
        
        String taskId = exportService.exportDocumentAsync(docId, format, principal.getUserId());
        
        log.info("异步单文档导出任务已创建: docId={}, format={}, taskId={}", 
                docId, format, taskId);
        
        return R.ok(taskId);
    }
    
    /**
     * 异步导出目录
     * 
     * @param directoryId 目录ID
     * @param format 导出格式
     * @param recursive 是否递归导出子目录，默认为false
     * @param mergeDocuments 是否合并为单个文档，默认为false
     * @param principal 当前用户
     * @return 导出任务ID
     */
    @PostMapping("/directory/{directoryId}/async")
    public R<String> exportDirectoryAsync(
            @PathVariable @NotBlank(message = "{validation.directoryId.notBlank}") String directoryId,
            @RequestParam @NotNull(message = "{validation.exportFormat.notNull}") ExportFormat format,
            @RequestParam(defaultValue = "false") boolean recursive,
            @RequestParam(defaultValue = "false") boolean mergeDocuments,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("收到异步目录导出请求: directoryId={}, format={}, recursive={}, mergeDocuments={}, userId={}", 
                directoryId, format, recursive, mergeDocuments, principal.getUserId());
        
        String taskId = exportService.exportDirectoryAsync(
                directoryId, format, recursive, mergeDocuments, principal.getUserId());
        
        log.info("异步目录导出任务已创建: directoryId={}, format={}, recursive={}, mergeDocuments={}, taskId={}", 
                directoryId, format, recursive, mergeDocuments, taskId);
        
        return R.ok(taskId);
    }
    
    /**
     * 获取异步导出任务状态
     * 
     * @param taskId 任务ID
     * @return 任务状态
     */
    @GetMapping("/task/{taskId}/status")
    public R<String> getExportTaskStatus(
            @PathVariable @NotBlank(message = "{validation.taskId.notBlank}") String taskId) {
        
        log.info("查询导出任务状态: taskId={}", taskId);
        String status = exportService.getExportTaskStatus(taskId);
        log.info("导出任务状态查询结果: taskId={}, status={}", taskId, status);
        return R.ok(status);
    }
    
    /**
     * 获取异步导出任务结果
     * 
     * @param taskId 任务ID
     * @return 导出结果，如果任务未完成则返回null
     */
    @GetMapping("/task/{taskId}/result")
    public ResponseEntity<Resource> getExportTaskResult(
            @PathVariable @NotBlank(message = "{validation.taskId.notBlank}") String taskId) {
        
        log.info("获取导出任务结果: taskId={}", taskId);
        ExportResult result = exportService.getExportTaskResult(taskId);
        
        if (result == null) {
            log.warn("导出任务结果不存在或未完成: taskId={}", taskId);
            return ResponseEntity.notFound().build();
        }
        
        // 构建响应
        ByteArrayResource resource = new ByteArrayResource(result.getContent());
        
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.getMimeType()));
        headers.setContentLength(result.getSize());
        
        // 设置文件名，支持中文
        String encodedFilename = URLEncoder.encode(result.getFilename(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename*=UTF-8''" + encodedFilename);
        
        log.info("返回导出任务结果: taskId={}, filename={}, size={}", 
                taskId, result.getFilename(), result.getSize());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}