package com.freedoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.freedoc.entity.ProjectUser;
import com.freedoc.mapper.ProjectUserMapper;
import com.freedoc.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 项目成员服务实现类
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {
    
    private final ProjectUserMapper projectUserMapper;
    
    @Override
    public ProjectUser getProjectMember(String projectId, String userId) {
        try {
            return projectUserMapper.selectOne(new LambdaQueryWrapper<ProjectUser>()
                    .eq(ProjectUser::getProjectId, projectId)
                    .eq(ProjectUser::getUserId, userId));
        } catch (Exception e) {
            log.error("获取项目成员信息失败: projectId={}, userId={}", projectId, userId, e);
            return null;
        }
    }
    
    @Override
    public boolean isProjectMember(String projectId, String userId) {
        return getProjectMember(projectId, userId) != null;
    }
    
    @Override
    public boolean hasPermission(String projectId, String userId, String permission) {
        ProjectUser member = getProjectMember(projectId, userId);
        if (member == null) {
            return false;
        }
        
        // 检查权限
        String memberPermission = member.getPermission();
        if (memberPermission == null) {
            return false;
        }
        
        // 如果是读写权限，则包含读权限
        if ("rw".equals(memberPermission)) {
            return true;
        }
        
        // 检查具体权限
        return memberPermission.contains(permission);
    }
}