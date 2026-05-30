package com.freedoc.service;

/**
 * 文档访问权限服务接口
 * 
 * @author FreeDoc Team
 */
public interface DocumentAccessService {
    
    /**
     * 检查用户是否有文档读取权限
     * 
     * @param userId 用户ID
     * @param docId 文档ID
     * @return 是否有权限
     */
    boolean hasReadPermission(String userId, String docId);
    
    /**
     * 检查用户是否有目录访问权限
     * 
     * @param userId 用户ID
     * @param directoryId 目录ID
     * @return 是否有权限
     */
    boolean hasDirectoryAccess(String userId, String directoryId);
    
    /**
     * 检查用户是否有文档写入权限
     * 
     * @param userId 用户ID
     * @param docId 文档ID
     * @return 是否有权限
     */
    boolean hasWritePermission(String userId, String docId);
    
    /**
     * 检查用户是否为文档所有者
     * 
     * @param userId 用户ID
     * @param docId 文档ID
     * @return 是否为所有者
     */
    boolean isDocumentOwner(String userId, String docId);
    
    /**
     * 检查用户是否为项目成员
     * 
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 是否为项目成员
     */
    boolean isProjectMember(String userId, String projectId);
}