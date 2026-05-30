package com.freedoc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedoc.dto.ProjectCreateRequest;
import com.freedoc.dto.ProjectMemberRequest;
import com.freedoc.dto.ProjectMemberVO;
import com.freedoc.dto.ProjectVO;
import com.freedoc.entity.Project;
import com.freedoc.entity.ProjectUser;

import java.util.List;
import java.util.Map;

public interface ProjectService extends IService<Project> {

    Project createProject(ProjectCreateRequest request, String userId);

    List<Project> getTeamProjects(String teamId, String userId);

    /**
     * 获取团队项目列表（包含创建者信息）
     */
    List<ProjectVO> getTeamProjectsWithCreator(String teamId, String userId);

    Project getProjectById(String projectId, String userId);

    void addMember(String projectId, ProjectMemberRequest request, String operatorId);

    void removeMember(String projectId, String userId, String operatorId);

    List<ProjectUser> getProjectMembers(String projectId, String userId);

    /**
     * 获取项目成员（包含用户信息）
     */
    List<ProjectMemberVO> getProjectMembersWithUser(String projectId, String userId);

    boolean hasPermission(String projectId, String userId, String permission);

    /**
     * 删除项目（级联删除目录、文档等）
     */
    void deleteProject(String projectId, String userId);

    /**
     * 更新项目基本信息
     */
    Project updateProject(String projectId, Map<String, Object> updates, String userId);

    /**
     * 更新项目成员权限
     */
    void updateMember(String projectId, String userId, Map<String, Object> updates, String operatorId);

}
