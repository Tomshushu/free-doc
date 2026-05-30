package com.freedoc.service;

import com.freedoc.entity.ProjectUser;

/**
 * 项目成员服务接口
 * 
 * @author FreeDoc Team
 */
public interface ProjectMemberService {
    
    /**
     * 获取项目成员信息
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 项目成员信息，如果不是成员则返回null
     */
    ProjectUser getProjectMember(String projectId, String userId);
    
    /**
     * 检查用户是否为项目成员
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 是否为项目成员
     */
    boolean isProjectMember(String projectId, String userId);
    
    /**
     * 检查用户是否有指定权限
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param permission 权限类型
     * @return 是否有权限
     */
    boolean hasPermission(String projectId, String userId, String permission);
}