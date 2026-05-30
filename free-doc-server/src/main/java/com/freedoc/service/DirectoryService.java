package com.freedoc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedoc.dto.DirectoryCreateRequest;
import com.freedoc.entity.Directory;
import com.freedoc.entity.Doc;

import java.util.List;

public interface DirectoryService extends IService<Directory> {

    Directory createDirectory(DirectoryCreateRequest request, String userId);

    List<Directory> getProjectDirectories(String projectId, String userId);

    void deleteDirectory(String directoryId, String userId);

    Directory updateDirectoryName(String directoryId, String name, String userId);

    /**
     * 递归获取目录及其所有子目录下的文档
     * 
     * @param directoryId 目录ID
     * @param userId 用户ID
     * @return 文档列表
     */
    List<Doc> getAllDocumentsRecursively(String directoryId, String userId);
    
    /**
     * 获取文档相对于根目录的路径
     * 
     * @param docId 文档ID
     * @param rootDirectoryId 根目录ID
     * @return 相对路径
     */
    String getDocumentRelativePath(String docId, String rootDirectoryId);
    
    /**
     * 根据目录ID获取目录名称
     * 
     * @param directoryId 目录ID
     * @return 目录名称
     */
    String getDirectoryName(String directoryId);

}
