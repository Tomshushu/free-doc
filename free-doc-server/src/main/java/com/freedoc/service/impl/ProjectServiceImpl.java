package com.freedoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedoc.common.constants.Constants;
import com.freedoc.common.exception.BusinessException;
import com.freedoc.common.util.SnowflakeIdUtil;
import com.freedoc.dto.ProjectCreateRequest;
import com.freedoc.dto.ProjectMemberRequest;
import com.freedoc.dto.ProjectMemberVO;
import com.freedoc.dto.ProjectVO;
import com.freedoc.entity.Doc;
import com.freedoc.entity.Directory;
import com.freedoc.entity.Project;
import com.freedoc.entity.ProjectUser;
import com.freedoc.entity.TeamUser;
import com.freedoc.entity.User;
import com.freedoc.mapper.DirectoryMapper;
import com.freedoc.mapper.DocMapper;
import com.freedoc.mapper.ProjectMapper;
import com.freedoc.mapper.ProjectUserMapper;
import com.freedoc.mapper.TeamUserMapper;
import com.freedoc.mapper.UserMapper;
import com.freedoc.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private final ProjectUserMapper projectUserMapper;
    private final TeamUserMapper teamUserMapper;
    private final DirectoryMapper directoryMapper;
    private final DocMapper docMapper;
    private final UserMapper userMapper;
    private final com.freedoc.mapper.DocVersionMapper docVersionMapper;
    private final com.freedoc.mapper.CommentMapper commentMapper;

    @Override
    @Transactional
    public Project createProject(ProjectCreateRequest request, String userId) {
        TeamUser teamUser = teamUserMapper.selectOne(
                new LambdaQueryWrapper<TeamUser>()
                        .eq(TeamUser::getTeamId, request.getTeamId())
                        .eq(TeamUser::getUserId, userId)
        );
        if (teamUser == null) {
            throw new BusinessException("error.project.noPermissionCreate");
        }
        if (!Constants.OWNER.equals(teamUser.getType())) {
            throw new BusinessException("error.project.onlyAdminCanCreate");
        }

        Project project = new Project();
        project.setProjectId(SnowflakeIdUtil.nextId());
        project.setProjectName(request.getProjectName());
        project.setProjectIcon(request.getProjectIcon() != null ? request.getProjectIcon() : "fa-solid fa-code");
        project.setTeamId(request.getTeamId());
        project.setProjectDesc(request.getProjectDesc());
        project.setCreateUser(userId);
        project.setCreateTime(LocalDateTime.now());
        save(project);

        ProjectUser projectUser = new ProjectUser();
        projectUser.setId(SnowflakeIdUtil.nextId());
        projectUser.setProjectId(project.getProjectId());
        projectUser.setUserId(userId);
        projectUser.setType(Constants.OWNER);
        projectUser.setPermission(Constants.PERMISSION_READ_WRITE);
        projectUserMapper.insert(projectUser);

        return project;
    }

    @Override
    public List<Project> getTeamProjects(String teamId, String userId) {
        TeamUser teamUser = teamUserMapper.selectOne(
                new LambdaQueryWrapper<TeamUser>()
                        .eq(TeamUser::getTeamId, teamId)
                        .eq(TeamUser::getUserId, userId)
        );
        if (teamUser == null) {
            throw new BusinessException("error.team.noPermissionAccess");
        }
        return list(new LambdaQueryWrapper<Project>().eq(Project::getTeamId, teamId));
    }

    @Override
    public List<ProjectVO> getTeamProjectsWithCreator(String teamId, String userId) {
        TeamUser teamUser = teamUserMapper.selectOne(
                new LambdaQueryWrapper<TeamUser>()
                        .eq(TeamUser::getTeamId, teamId)
                        .eq(TeamUser::getUserId, userId)
        );
        if (teamUser == null) {
            throw new BusinessException("error.team.noPermissionAccess");
        }

        List<ProjectUser> userProjects = projectUserMapper.selectList(
                new LambdaQueryWrapper<ProjectUser>()
                        .eq(ProjectUser::getUserId, userId)
        );
        if (userProjects.isEmpty()) {
            return List.of();
        }

        Set<String> userProjectIds = userProjects.stream()
                .map(ProjectUser::getProjectId)
                .collect(Collectors.toSet());

        List<Project> projects = list(new LambdaQueryWrapper<Project>()
                .eq(Project::getTeamId, teamId)
                .in(Project::getProjectId, userProjectIds));
        if (projects.isEmpty()) {
            return List.of();
        }

        Set<String> creatorIds = projects.stream()
                .map(Project::getCreateUser)
                .collect(Collectors.toSet());
        List<User> creators = userMapper.selectBatchIds(creatorIds);
        Map<String, User> creatorMap = creators.stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        Map<String, ProjectUser> userRoleMap = userProjects.stream()
                .collect(Collectors.toMap(ProjectUser::getProjectId, pu -> pu));

        return projects.stream().map(project -> {
            ProjectVO vo = new ProjectVO();
            vo.setProjectId(project.getProjectId());
            vo.setProjectName(project.getProjectName());
            vo.setProjectIcon(project.getProjectIcon());
            vo.setTeamId(project.getTeamId());
            vo.setProjectDesc(project.getProjectDesc());
            vo.setCreateUser(project.getCreateUser());
            vo.setCreateTime(project.getCreateTime());

            User creator = creatorMap.get(project.getCreateUser());
            if (creator != null) {
                vo.setCreateUserName(creator.getUserName());
                vo.setCreateUserAccount(creator.getAccount());
            }

            ProjectUser pu = userRoleMap.get(project.getProjectId());
            vo.setIsOwner(pu != null && Constants.OWNER.equals(pu.getType()));
            vo.setPermission(pu != null ? pu.getPermission() : Constants.PERMISSION_READ);

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Project getProjectById(String projectId, String userId) {
        checkProjectMember(projectId, userId);
        return getById(projectId);
    }

    @Override
    @Transactional
    public void addMember(String projectId, ProjectMemberRequest request, String operatorId) {
        checkProjectOwner(projectId, operatorId);

        ProjectUser exist = projectUserMapper.selectOne(
                new LambdaQueryWrapper<ProjectUser>()
                        .eq(ProjectUser::getProjectId, projectId)
                        .eq(ProjectUser::getUserId, request.getUserId())
        );
        if (exist != null) {
            throw new BusinessException("error.project.userAlreadyInProject");
        }

        ProjectUser projectUser = new ProjectUser();
        projectUser.setId(SnowflakeIdUtil.nextId());
        projectUser.setProjectId(projectId);
        projectUser.setUserId(request.getUserId());
        projectUser.setType(request.getType());
        projectUser.setPermission(request.getPermission());
        projectUserMapper.insert(projectUser);
    }

    @Override
    @Transactional
    public void removeMember(String projectId, String userId, String operatorId) {
        checkProjectOwner(projectId, operatorId);

        ProjectUser projectUser = projectUserMapper.selectOne(
                new LambdaQueryWrapper<ProjectUser>()
                        .eq(ProjectUser::getProjectId, projectId)
                        .eq(ProjectUser::getUserId, userId)
        );
        if (projectUser == null) {
            throw new BusinessException("error.project.userNotInProject");
        }
        if (Constants.OWNER.equals(projectUser.getType())) {
            throw new BusinessException("error.project.cannotRemoveOwner");
        }

        projectUserMapper.deleteById(projectUser.getId());
    }

    @Override
    public List<ProjectUser> getProjectMembers(String projectId, String userId) {
        checkProjectMember(projectId, userId);
        return projectUserMapper.selectList(
                new LambdaQueryWrapper<ProjectUser>().eq(ProjectUser::getProjectId, projectId)
        );
    }

    @Override
    public List<ProjectMemberVO> getProjectMembersWithUser(String projectId, String userId) {
        checkProjectMember(projectId, userId);

        List<ProjectUser> projectUsers = projectUserMapper.selectList(
                new LambdaQueryWrapper<ProjectUser>().eq(ProjectUser::getProjectId, projectId)
        );

        if (projectUsers.isEmpty()) {
            return List.of();
        }

        List<String> userIds = projectUsers.stream()
                .map(ProjectUser::getUserId)
                .collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        return projectUsers.stream().map(pu -> {
            ProjectMemberVO vo = new ProjectMemberVO();
            vo.setId(pu.getId());
            vo.setProjectId(pu.getProjectId());
            vo.setUserId(pu.getUserId());
            vo.setType(pu.getType());
            vo.setPermission(pu.getPermission());

            User user = userMap.get(pu.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
                vo.setAccount(user.getAccount());
                vo.setUserIcon(user.getUserIcon());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean hasPermission(String projectId, String userId, String permission) {
        ProjectUser projectUser = projectUserMapper.selectOne(
                new LambdaQueryWrapper<ProjectUser>()
                        .eq(ProjectUser::getProjectId, projectId)
                        .eq(ProjectUser::getUserId, userId)
        );
        if (projectUser == null) {
            return false;
        }

        if (Constants.OWNER.equals(projectUser.getType())) {
            return true;
        }

        if (Constants.PERMISSION_READ_WRITE.equals(permission)) {
            return Constants.PERMISSION_READ_WRITE.equals(projectUser.getPermission());
        } else if (Constants.PERMISSION_READ.equals(permission)) {
            return Constants.PERMISSION_READ.equals(projectUser.getPermission()) ||
                    Constants.PERMISSION_READ_WRITE.equals(projectUser.getPermission());
        }

        return false;
    }

    @Override
    @Transactional
    public void deleteProject(String projectId, String userId) {
        checkProjectOwner(projectId, userId);

        List<Doc> docs = docMapper.selectList(new LambdaQueryWrapper<Doc>().eq(Doc::getProjectId, projectId));
        List<String> docIds = docs.stream().map(Doc::getDocId).collect(Collectors.toList());

        if (!docIds.isEmpty()) {
            docVersionMapper.delete(new LambdaQueryWrapper<com.freedoc.entity.DocVersion>()
                    .in(com.freedoc.entity.DocVersion::getDocId, docIds));
            commentMapper.delete(new LambdaQueryWrapper<com.freedoc.entity.Comment>()
                    .in(com.freedoc.entity.Comment::getDocId, docIds));
        }

        projectUserMapper.delete(new LambdaQueryWrapper<ProjectUser>().eq(ProjectUser::getProjectId, projectId));
        docMapper.delete(new LambdaQueryWrapper<Doc>().eq(Doc::getProjectId, projectId));
        directoryMapper.delete(new LambdaQueryWrapper<Directory>().eq(Directory::getProjectId, projectId));
        removeById(projectId);
    }

    @Override
    @Transactional
    public Project updateProject(String projectId, Map<String, Object> updates, String userId) {
        checkProjectWritePermission(projectId, userId);
        Project project = getById(projectId);
        if (project == null) {
            throw new BusinessException("error.project.notFound");
        }
        if (updates.containsKey("projectName")) {
            project.setProjectName((String) updates.get("projectName"));
        }
        if (updates.containsKey("projectIcon")) {
            project.setProjectIcon((String) updates.get("projectIcon"));
        }
        if (updates.containsKey("projectDesc")) {
            project.setProjectDesc((String) updates.get("projectDesc"));
        }
        updateById(project);
        return project;
    }

    @Override
    @Transactional
    public void updateMember(String projectId, String userId, Map<String, Object> updates, String operatorId) {
        checkProjectOwner(projectId, operatorId);

        ProjectUser projectUser = projectUserMapper.selectOne(
                new LambdaQueryWrapper<ProjectUser>()
                        .eq(ProjectUser::getProjectId, projectId)
                        .eq(ProjectUser::getUserId, userId)
        );
        if (projectUser == null) {
            throw new BusinessException("error.project.memberNotFound");
        }

        if (Constants.OWNER.equals(projectUser.getType())) {
            throw new BusinessException("error.project.cannotModifyOwnerPermission");
        }

        boolean updated = false;
        if (updates.containsKey("type")) {
            String newType = (String) updates.get("type");
            if (Constants.OWNER.equals(newType) || Constants.PARTICIPANT.equals(newType)) {
                projectUser.setType(newType);
                updated = true;
            }
        }
        if (updates.containsKey("permission")) {
            String newPermission = (String) updates.get("permission");
            if (Constants.PERMISSION_READ.equals(newPermission) || Constants.PERMISSION_READ_WRITE.equals(newPermission)) {
                projectUser.setPermission(newPermission);
                updated = true;
            }
        }

        if (updated) {
            projectUserMapper.updateById(projectUser);
        }
    }

    private void checkProjectMember(String projectId, String userId) {
        ProjectUser projectUser = projectUserMapper.selectOne(
                new LambdaQueryWrapper<ProjectUser>()
                        .eq(ProjectUser::getProjectId, projectId)
                        .eq(ProjectUser::getUserId, userId)
        );
        if (projectUser == null) {
            throw new BusinessException("error.project.noPermissionAccess");
        }
    }

    private void checkProjectOwner(String projectId, String userId) {
        ProjectUser projectUser = projectUserMapper.selectOne(
                new LambdaQueryWrapper<ProjectUser>()
                        .eq(ProjectUser::getProjectId, projectId)
                        .eq(ProjectUser::getUserId, userId)
        );
        if (projectUser == null || !Constants.OWNER.equals(projectUser.getType())) {
            throw new BusinessException("error.project.onlyOwnerCanOperate");
        }
    }

    private void checkProjectWritePermission(String projectId, String userId) {
        ProjectUser projectUser = projectUserMapper.selectOne(
                new LambdaQueryWrapper<ProjectUser>()
                        .eq(ProjectUser::getProjectId, projectId)
                        .eq(ProjectUser::getUserId, userId)
        );
        if (projectUser == null) {
            throw new BusinessException("error.project.noPermissionAccess");
        }
        if (Constants.OWNER.equals(projectUser.getType())) {
            return;
        }
        if (!Constants.PERMISSION_READ_WRITE.equals(projectUser.getPermission())) {
            throw new BusinessException("error.project.noPermissionEdit");
        }
    }

}
