package com.freedoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.freedoc.common.constants.Constants;
import com.freedoc.common.enums.ExportFormat;
import com.freedoc.common.exception.BusinessException;
import com.freedoc.common.exception.ExportException;
import com.freedoc.common.i18n.I18nUtil;
import com.freedoc.common.util.ExportUtil;
import com.freedoc.dto.BatchExportResult;
import com.freedoc.dto.ExportedFile;
import com.freedoc.entity.Directory;
import com.freedoc.entity.Doc;
import com.freedoc.service.DirectoryExportService;
import com.freedoc.service.DirectoryService;
import com.freedoc.service.DocService;
import com.freedoc.service.DocumentAccessService;
import com.freedoc.service.converter.ConverterFactory;
import com.freedoc.service.converter.FormatConverter;
import com.freedoc.service.converter.ConversionOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 目录导出服务实现类
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DirectoryExportServiceImpl implements DirectoryExportService {
    
    private final DirectoryService directoryService;
    private final DocService docService;
    private final DocumentAccessService documentAccessService;
    private final ConverterFactory converterFactory;
    
    @Override
    public List<Doc> getDocumentsRecursively(String directoryId, String userId) {
        log.debug("递归获取目录文档: directoryId={}, userId={}", directoryId, userId);
        
        // 验证权限
        if (!validateDirectoryExportPermission(directoryId, userId)) {
            throw new ExportException(Constants.Export.ERROR_ACCESS_DENIED, 
                    I18nUtil.getMessage("error.export.noPermissionDir", directoryId));
        }
        
        try {
            // 使用DirectoryService的递归方法
            List<Doc> docs = directoryService.getAllDocumentsRecursively(directoryId, userId);
            
            // 按目录结构和文档名称排序
            return sortDocumentsByPath(docs, directoryId);
            
        } catch (Exception e) {
            log.error("递归获取目录文档失败: directoryId={}", directoryId, e);
            throw new ExportException(Constants.Export.ERROR_DIRECTORY_NOT_FOUND, 
                    I18nUtil.getMessage("error.export.getDirDocsFailed", e.getMessage()), e);
        }
    }
    
    @Override
    public List<Doc> getDocumentsInDirectory(String directoryId, String userId) {
        log.debug("获取目录文档: directoryId={}, userId={}", directoryId, userId);
        
        // 验证权限
        if (!validateDirectoryExportPermission(directoryId, userId)) {
            throw new ExportException(Constants.Export.ERROR_ACCESS_DENIED, 
                    I18nUtil.getMessage("error.export.noPermissionDir", directoryId));
        }
        
        try {
            // 只获取当前目录下的文档
            List<Doc> docs = docService.getDirectoryDocs(directoryId, userId);
            
            // 按文档名称排序
            return docs.stream()
                    .sorted(Comparator.comparing(Doc::getDocTitle))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("获取目录文档失败: directoryId={}", directoryId, e);
            throw new ExportException(Constants.Export.ERROR_DIRECTORY_NOT_FOUND, 
                    I18nUtil.getMessage("error.export.getDirDocsFailed", e.getMessage()), e);
        }
    }
    
    @Override
    public byte[] createExportZip(List<Doc> docs, ExportFormat format, String rootDirectoryId) {
        return createExportZip(docs, format, rootDirectoryId, null);
    }
    
    @Override
    public byte[] createExportZip(List<Doc> docs, ExportFormat format, String rootDirectoryId, 
                                 java.util.function.Consumer<String> progressCallback) {
        log.debug("创建导出ZIP: docCount={}, format={}, rootDirectoryId={}", 
                docs.size(), format, rootDirectoryId);
        
        if (docs.isEmpty()) {
            throw new ExportException(Constants.Export.ERROR_DIRECTORY_NOT_FOUND, 
                    I18nUtil.getMessage("error.export.noDocsAvailable"));
        }
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            
            // 设置ZIP文件编码为UTF-8，支持中文文件名
            zos.setComment("FreeDoc Export - " + LocalDateTime.now());
            
            Set<String> addedPaths = new HashSet<>();
            FormatConverter converter = converterFactory.getConverter(format);
            ConversionOptions options = ConversionOptions.builder()
                    .includeMetadata(true)
                    .preserveFormatting(true)
                    .build();
            
            for (Doc doc : docs) {
                try {
                    // 转换文档内容
                    byte[] content = converter.convert(doc, options);
                    
                    // 生成ZIP条目路径
                    String entryPath = buildZipEntryPath(doc, format, rootDirectoryId);
                    
                    // 确保路径唯一性
                    entryPath = ensureUniqueZipPath(entryPath, addedPaths);
                    addedPaths.add(entryPath);
                    
                    // 创建目录结构
                    createDirectoryStructure(zos, entryPath, addedPaths);
                    
                    // 添加文件到ZIP
                    ZipEntry entry = new ZipEntry(entryPath);
                    entry.setTime(doc.getUpdateTime() != null ? 
                            doc.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() :
                            System.currentTimeMillis());
                    entry.setComment("Document: " + doc.getDocTitle());
                    
                    zos.putNextEntry(entry);
                    zos.write(content);
                    zos.closeEntry();
                    
                    log.debug("已添加文档到ZIP: docId={}, path={}, size={}", 
                            doc.getDocId(), entryPath, content.length);
                    
                    // 调用进度回调
                    if (progressCallback != null) {
                        progressCallback.accept(doc.getDocTitle());
                    }
                    
                } catch (Exception e) {
                    log.warn("跳过文档转换失败: docId={}, title={}, error={}", 
                            doc.getDocId(), doc.getDocTitle(), e.getMessage());
                    // 继续处理其他文档，不因单个文档失败而中断整个导出
                }
            }
            
            zos.finish();
            byte[] zipBytes = baos.toByteArray();
            
            log.info("ZIP文件创建完成: size={}, entries={}", zipBytes.length, addedPaths.size());
            return zipBytes;
            
        } catch (IOException e) {
            log.error("创建ZIP文件失败", e);
            throw new ExportException(Constants.Export.ERROR_CONVERSION_FAILED, 
                    I18nUtil.getMessage("error.export.createZipFailed", e.getMessage()), e);
        }
    }
    
    @Override
    public BatchExportResult exportDirectory(String directoryId, ExportFormat format, 
                                           boolean recursive, String userId) {
        log.info("开始批量导出目录: directoryId={}, format={}, recursive={}, userId={}", 
                directoryId, format, recursive, userId);
        
        try {
            // 获取文档列表
            List<Doc> docs = recursive ? 
                    getDocumentsRecursively(directoryId, userId) : 
                    getDocumentsInDirectory(directoryId, userId);
            
            if (docs.isEmpty()) {
                throw new ExportException(Constants.Export.ERROR_DIRECTORY_NOT_FOUND, 
                        I18nUtil.getMessage("error.export.noDocsToExport"));
            }
            
            // 创建ZIP文件
            byte[] zipContent = createExportZip(docs, format, directoryId);
            
            // 构建导出文件信息
            List<ExportedFile> exportedFiles = docs.stream()
                    .map(doc -> buildExportedFileInfo(doc, format, directoryId))
                    .collect(Collectors.toList());
            
            // 生成ZIP文件名
            String directoryName = directoryService.getDirectoryName(directoryId);
            String zipFilename = ExportUtil.generateBatchFilename(directoryName, format);
            
            BatchExportResult result = BatchExportResult.builder()
                    .zipFilename(zipFilename)
                    .zipContent(zipContent)
                    .files(exportedFiles)
                    .totalFiles(docs.size())
                    .totalSize(zipContent.length)
                    .format(format)
                    .recursive(recursive)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            log.info("目录导出完成: directoryId={}, fileCount={}, zipSize={}", 
                    directoryId, docs.size(), zipContent.length);
            
            return result;
            
        } catch (Exception e) {
            log.error("目录导出失败: directoryId={}, format={}, recursive={}", 
                    directoryId, format, recursive, e);
            if (e instanceof ExportException) {
                throw (ExportException) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new ExportException(Constants.Export.ERROR_CONVERSION_FAILED, 
                        I18nUtil.getMessage("error.export.dirExportFailed", e.getMessage()), e);
            }
        }
    }
    
    @Override
    public BatchExportResult exportProject(String projectId, ExportFormat format, String userId) {
        log.info("开始导出项目: projectId={}, format={}, userId={}", projectId, format, userId);
        
        try {
            // 获取项目下的所有文档
            List<Doc> docs = docService.list(new LambdaQueryWrapper<Doc>()
                    .eq(Doc::getProjectId, projectId)
                    .orderByAsc(Doc::getCreateTime));
            
            if (docs.isEmpty()) {
                throw new ExportException(Constants.Export.ERROR_DIRECTORY_NOT_FOUND, 
                        I18nUtil.getMessage("error.export.noDocsInProject"));
            }
            
            // 创建ZIP文件（使用"0"作为根目录ID，表示项目根）
            byte[] zipContent = createExportZip(docs, format, "0");
            
            // 构建导出文件信息
            List<ExportedFile> exportedFiles = docs.stream()
                    .map(doc -> buildExportedFileInfo(doc, format, "0"))
                    .collect(Collectors.toList());
            
            // 生成ZIP文件名
            String projectName = getProjectName(projectId, userId);
            String zipFilename = ExportUtil.generateBatchFilename(projectName, format);
            
            BatchExportResult result = BatchExportResult.builder()
                    .zipFilename(zipFilename)
                    .zipContent(zipContent)
                    .files(exportedFiles)
                    .totalFiles(docs.size())
                    .totalSize(zipContent.length)
                    .format(format)
                    .recursive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            log.info("项目导出完成: projectId={}, fileCount={}, zipSize={}", 
                    projectId, docs.size(), zipContent.length);
            
            return result;
            
        } catch (Exception e) {
            log.error("项目导出失败: projectId={}, format={}", projectId, format, e);
            if (e instanceof ExportException) {
                throw (ExportException) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new ExportException(Constants.Export.ERROR_CONVERSION_FAILED, 
                        I18nUtil.getMessage("error.export.projectExportFailed", e.getMessage()), e);
            }
        }
    }
    
    @Override
    public String buildZipEntryPath(Doc doc, ExportFormat format, String rootDirectoryId) {
        // 获取文档相对于根目录的路径
        String relativePath = directoryService.getDocumentRelativePath(doc.getDocId(), rootDirectoryId);
        
        log.debug("构建ZIP路径: docId={}, docTitle={}, rootDirectoryId={}, relativePath={}", 
                doc.getDocId(), doc.getDocTitle(), rootDirectoryId, relativePath);
        
        // 生成安全的文件名
        String filename = ExportUtil.generateSafeFilename(doc.getDocTitle(), format);
        
        // 组合完整路径
        String fullPath;
        if (relativePath == null || relativePath.isEmpty()) {
            fullPath = filename;
        } else {
            fullPath = relativePath + "/" + filename;
        }
        
        // 清理和标准化路径
        String sanitizedPath = ExportUtil.sanitizePath(fullPath);
        log.debug("最终ZIP路径: {}", sanitizedPath);
        
        return sanitizedPath;
    }
    
    @Override
    public boolean validateDirectoryExportPermission(String directoryId, String userId) {
        try {
            return documentAccessService.hasDirectoryAccess(userId, directoryId);
        } catch (Exception e) {
            log.warn("权限验证失败: directoryId={}, userId={}", directoryId, userId, e);
            return false;
        }
    }
    
    @Override
    public String getDirectoryFullPath(String directoryId) {
        try {
            Directory directory = directoryService.getById(directoryId);
            if (directory == null) {
                return "";
            }
            
            StringBuilder pathBuilder = new StringBuilder();
            Directory current = directory;
            
            // 从当前目录向上遍历到根目录
            while (current != null && !"0".equals(current.getPid())) {
                if (pathBuilder.length() > 0) {
                    pathBuilder.insert(0, "/");
                }
                pathBuilder.insert(0, current.getName());
                
                current = directoryService.getById(current.getPid());
            }
            
            return pathBuilder.toString();
            
        } catch (Exception e) {
            log.warn("获取目录完整路径失败: directoryId={}", directoryId, e);
            return "";
        }
    }
    
    /**
     * 按路径对文档进行排序
     */
    private List<Doc> sortDocumentsByPath(List<Doc> docs, String rootDirectoryId) {
        return docs.stream()
                .sorted((doc1, doc2) -> {
                    String path1 = buildZipEntryPath(doc1, ExportFormat.MARKDOWN, rootDirectoryId);
                    String path2 = buildZipEntryPath(doc2, ExportFormat.MARKDOWN, rootDirectoryId);
                    return path1.compareToIgnoreCase(path2);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 确保ZIP路径的唯一性
     */
    private String ensureUniqueZipPath(String originalPath, Set<String> existingPaths) {
        if (!existingPaths.contains(originalPath)) {
            return originalPath;
        }
        
        // 如果路径已存在，添加数字后缀
        String basePath = originalPath;
        String extension = "";
        
        int lastDotIndex = originalPath.lastIndexOf('.');
        if (lastDotIndex > 0) {
            basePath = originalPath.substring(0, lastDotIndex);
            extension = originalPath.substring(lastDotIndex);
        }
        
        int counter = 1;
        String newPath;
        do {
            newPath = basePath + "_" + counter + extension;
            counter++;
        } while (existingPaths.contains(newPath));
        
        return newPath;
    }
    
    /**
     * 在ZIP中创建目录结构
     */
    private void createDirectoryStructure(ZipOutputStream zos, String filePath, 
                                        Set<String> addedPaths) throws IOException {
        String[] pathParts = filePath.split("/");
        StringBuilder currentPath = new StringBuilder();
        
        for (int i = 0; i < pathParts.length - 1; i++) {
            if (currentPath.length() > 0) {
                currentPath.append("/");
            }
            currentPath.append(pathParts[i]);
            String dirPath = currentPath.toString() + "/";
            
            if (!addedPaths.contains(dirPath)) {
                ZipEntry dirEntry = new ZipEntry(dirPath);
                dirEntry.setTime(System.currentTimeMillis());
                zos.putNextEntry(dirEntry);
                zos.closeEntry();
                addedPaths.add(dirPath);
            }
        }
    }
    
    /**
     * 构建导出文件信息
     */
    private ExportedFile buildExportedFileInfo(Doc doc, ExportFormat format, String rootDirectoryId) {
        String originalPath = directoryService.getDocumentRelativePath(doc.getDocId(), rootDirectoryId);
        String exportedPath = buildZipEntryPath(doc, format, rootDirectoryId);
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
            List<Doc> docs = docService.list(new LambdaQueryWrapper<Doc>()
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
}