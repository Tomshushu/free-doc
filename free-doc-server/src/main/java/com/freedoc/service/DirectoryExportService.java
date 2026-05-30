package com.freedoc.service;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.dto.BatchExportResult;
import com.freedoc.entity.Doc;

import java.util.List;

/**
 * 目录导出服务接口
 * 专门处理目录和批量文档导出功能
 * 
 * @author FreeDoc Team
 */
public interface DirectoryExportService {
    
    /**
     * 递归获取目录下的所有文档（包含子目录）
     * 
     * @param directoryId 目录ID
     * @param userId 用户ID
     * @return 文档列表，按目录结构排序
     */
    List<Doc> getDocumentsRecursively(String directoryId, String userId);
    
    /**
     * 非递归获取目录下的文档（仅当前目录）
     * 
     * @param directoryId 目录ID
     * @param userId 用户ID
     * @return 文档列表
     */
    List<Doc> getDocumentsInDirectory(String directoryId, String userId);
    
    /**
     * 创建批量导出的ZIP文件
     * 
     * @param docs 要导出的文档列表
     * @param format 导出格式
     * @param rootDirectoryId 根目录ID，用于计算相对路径
     * @return ZIP文件的字节数组
     */
    byte[] createExportZip(List<Doc> docs, ExportFormat format, String rootDirectoryId);
    
    /**
     * 创建批量导出的ZIP文件（带进度回调）
     * 
     * @param docs 要导出的文档列表
     * @param format 导出格式
     * @param rootDirectoryId 根目录ID，用于计算相对路径
     * @param progressCallback 进度回调函数，参数为当前处理的文件名
     * @return ZIP文件的字节数组
     */
    byte[] createExportZip(List<Doc> docs, ExportFormat format, String rootDirectoryId, 
                          java.util.function.Consumer<String> progressCallback);
    
    /**
     * 批量导出目录下的文档
     * 
     * @param directoryId 目录ID
     * @param format 导出格式
     * @param recursive 是否递归导出子目录
     * @param userId 用户ID
     * @return 批量导出结果
     */
    BatchExportResult exportDirectory(String directoryId, ExportFormat format, boolean recursive, String userId);
    
    /**
     * 导出整个项目的所有文档
     * 
     * @param projectId 项目ID
     * @param format 导出格式
     * @param userId 用户ID
     * @return 批量导出结果
     */
    BatchExportResult exportProject(String projectId, ExportFormat format, String userId);
    
    /**
     * 构建ZIP文件中的目录结构
     * 
     * @param doc 文档对象
     * @param format 导出格式
     * @param rootDirectoryId 根目录ID
     * @return ZIP条目路径
     */
    String buildZipEntryPath(Doc doc, ExportFormat format, String rootDirectoryId);
    
    /**
     * 验证目录导出权限
     * 
     * @param directoryId 目录ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean validateDirectoryExportPermission(String directoryId, String userId);
    
    /**
     * 获取目录的完整路径信息
     * 
     * @param directoryId 目录ID
     * @return 目录路径信息
     */
    String getDirectoryFullPath(String directoryId);
}