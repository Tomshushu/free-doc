package com.freedoc.service.impl;

import com.freedoc.entity.Doc;
import com.freedoc.entity.Directory;
import com.freedoc.entity.Project;
import com.freedoc.entity.ProjectUser;
import com.freedoc.service.DocService;
import com.freedoc.service.DirectoryService;
import com.freedoc.service.DocumentAccessService;
import com.freedoc.service.ProjectMemberService;
import com.freedoc.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 文档访问权限服务实现类
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentAccessServiceImpl implements DocumentAccessService {
    
    private final DocService docService;
    private final DirectoryService directoryService;
    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    
    @Override
    public boolean hasReadPermission(String userId, String docId) {
        try {
            // 获取文档信息
            Doc doc = docService.getById(docId);
            if (doc == null) {
                log.warn("文档不存在: docId={}", docId);
                return false;
            }
            
            // 检查是否为文档创建者
            if (userId.equals(doc.getCreateUser())) {
                return true;
            }
            
            // 检查项目权限
            String projectId = getDocumentProjectId(doc);
            if (projectId != null) {
                return isProjectMember(userId, projectId);
            }
            
            return false;
        } catch (Exception e) {
            log.error("检查文档读取权限失败: userId={}, docId={}", userId, docId, e);
            return false;
        }
    }
    
    @Override
    public boolean hasDirectoryAccess(String userId, String directoryId) {
        try {
            // 获取目录信息
            Directory directory = directoryService.getById(directoryId);
            if (directory == null) {
                log.warn("目录不存在: directoryId={}", directoryId);
                return false;
            }
            
            // 检查是否为目录创建者
            if (userId.equals(directory.getCreateUser())) {
                return true;
            }
            
            // 检查项目权限
            if (directory.getProjectId() != null) {
                return isProjectMember(userId, directory.getProjectId());
            }
            
            return false;
        } catch (Exception e) {
            log.error("检查目录访问权限失败: userId={}, directoryId={}", userId, directoryId, e);
            return false;
        }
    }
    
    @Override
    public boolean hasWritePermission(String userId, String docId) {
        try {
            // 获取文档信息
            Doc doc = docService.getById(docId);
            if (doc == null) {
                return false;
            }
            
            // 检查是否为文档创建者
            if (userId.equals(doc.getCreateUser())) {
                return true;
            }
            
            // 检查项目权限（项目成员通常有写入权限）
            String projectId = getDocumentProjectId(doc);
            if (projectId != null) {
                ProjectUser member = projectMemberService.getProjectMember(projectId, userId);
                return member != null && hasWriteRole(member.getType());
            }
            
            return false;
        } catch (Exception e) {
            log.error("检查文档写入权限失败: userId={}, docId={}", userId, docId, e);
            return false;
        }
    }
    
    @Override
    public boolean isDocumentOwner(String userId, String docId) {
        try {
            Doc doc = docService.getById(docId);
            return doc != null && userId.equals(doc.getCreateUser());
        } catch (Exception e) {
            log.error("检查文档所有者失败: userId={}, docId={}", userId, docId, e);
            return false;
        }
    }
    
    @Override
    public boolean isProjectMember(String userId, String projectId) {
        try {
            ProjectUser member = projectMemberService.getProjectMember(projectId, userId);
            return member != null;
        } catch (Exception e) {
            log.error("检查项目成员失败: userId={}, projectId={}", userId, projectId, e);
            return false;
        }
    }
    
    /**
     * 获取文档所属的项目ID
     * 
     * @param doc 文档
     * @return 项目ID，如果不属于任何项目则返回null
     */
    private String getDocumentProjectId(Doc doc) {
        if (doc.getDirectoryId() != null) {
            Directory directory = directoryService.getById(doc.getDirectoryId());
            return directory != null ? directory.getProjectId() : null;
        }
        return null;
    }
    
    /**
     * 检查角色是否有写入权限
     * 
     * @param type 角色类型
     * @return 是否有写入权限
     */
    private boolean hasWriteRole(String type) {
        // 根据实际的角色定义来判断
        // 这里假设OWNER和PARTICIPANT都有写入权限
        return "OWNER".equals(type) || "PARTICIPANT".equals(type);
    }
}