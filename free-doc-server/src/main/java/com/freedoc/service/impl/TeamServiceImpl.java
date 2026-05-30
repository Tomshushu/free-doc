package com.freedoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedoc.common.constants.Constants;
import com.freedoc.common.exception.BusinessException;
import com.freedoc.common.util.SnowflakeIdUtil;
import com.freedoc.dto.TeamCreateRequest;
import com.freedoc.dto.TeamMemberRequest;
import com.freedoc.dto.TeamMemberVO;
import com.freedoc.dto.TeamStatsVO;
import com.freedoc.entity.Team;
import com.freedoc.entity.TeamUser;
import com.freedoc.entity.User;
import com.freedoc.mapper.DocMapper;
import com.freedoc.mapper.ProjectMapper;
import com.freedoc.mapper.TeamMapper;
import com.freedoc.mapper.TeamUserMapper;
import com.freedoc.mapper.UserMapper;
import com.freedoc.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    private final TeamUserMapper teamUserMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final DocMapper docMapper;
    private final com.freedoc.service.ProjectService projectService;

    @Override
    @Transactional
    public Team createTeam(TeamCreateRequest request, String userId) {
        Team team = new Team();
        team.setTeamId(SnowflakeIdUtil.nextId());
        team.setTeamName(request.getTeamName());
        team.setTeamIcon(request.getTeamIcon() != null ? request.getTeamIcon() : "fa-solid fa-users");
        team.setTeamDesc(request.getTeamDesc());
        team.setCreateUser(userId);
        team.setCreateTime(LocalDateTime.now());
        save(team);

        TeamUser teamUser = new TeamUser();
        teamUser.setId(SnowflakeIdUtil.nextId());
        teamUser.setTeamId(team.getTeamId());
        teamUser.setUserId(userId);
        teamUser.setType(Constants.OWNER);
        teamUser.setIsDefault(true);
        teamUserMapper.insert(teamUser);

        return team;
    }

    @Override
    public List<Team> getUserTeams(String userId) {
        List<TeamUser> teamUsers = teamUserMapper.selectList(
                new LambdaQueryWrapper<TeamUser>().eq(TeamUser::getUserId, userId)
        );
        if (teamUsers.isEmpty()) {
            return List.of();
        }
        List<String> teamIds = teamUsers.stream().map(TeamUser::getTeamId).collect(Collectors.toList());
        return listByIds(teamIds);
    }

    @Override
    public Team getTeamById(String teamId, String userId) {
        checkTeamMember(teamId, userId);
        return getById(teamId);
    }

    @Override
    @Transactional
    public void addMember(String teamId, TeamMemberRequest request, String operatorId) {
        checkTeamOwner(teamId, operatorId);

        TeamUser exist = teamUserMapper.selectOne(
                new LambdaQueryWrapper<TeamUser>()
                        .eq(TeamUser::getTeamId, teamId)
                        .eq(TeamUser::getUserId, request.getUserId())
        );
        if (exist != null) {
            throw new BusinessException("error.team.userAlreadyInTeam");
        }

        TeamUser teamUser = new TeamUser();
        teamUser.setId(SnowflakeIdUtil.nextId());
        teamUser.setTeamId(teamId);
        teamUser.setUserId(request.getUserId());
        teamUser.setType(request.getType());
        teamUser.setIsDefault(false);
        teamUserMapper.insert(teamUser);
    }

    @Override
    @Transactional
    public void removeMember(String teamId, String userId, String operatorId) {
        checkTeamOwner(teamId, operatorId);

        TeamUser teamUser = teamUserMapper.selectOne(
                new LambdaQueryWrapper<TeamUser>()
                        .eq(TeamUser::getTeamId, teamId)
                        .eq(TeamUser::getUserId, userId)
        );
        if (teamUser == null) {
            throw new BusinessException("error.team.userNotInTeam");
        }
        if (Constants.OWNER.equals(teamUser.getType())) {
            throw new BusinessException("error.team.cannotRemoveOwner");
        }

        teamUserMapper.deleteById(teamUser.getId());
    }

    @Override
    public List<TeamUser> getTeamMembers(String teamId, String userId) {
        checkTeamMember(teamId, userId);
        return teamUserMapper.selectList(
                new LambdaQueryWrapper<TeamUser>().eq(TeamUser::getTeamId, teamId)
        );
    }

    @Override
    public List<TeamMemberVO> getTeamMembersWithUser(String teamId, String userId) {
        checkTeamMember(teamId, userId);
        
        // 1. 获取团队成员列表
        List<TeamUser> teamUsers = teamUserMapper.selectList(
                new LambdaQueryWrapper<TeamUser>().eq(TeamUser::getTeamId, teamId)
        );
        
        if (teamUsers.isEmpty()) {
            return List.of();
        }
        
        // 2. 批量获取用户信息（一次查询）
        List<String> userIds = teamUsers.stream()
                .map(TeamUser::getUserId)
                .collect(Collectors.toList());
        
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));
        
        // 3. 组装数据
        return teamUsers.stream().map(teamUser -> {
            TeamMemberVO vo = new TeamMemberVO();
            vo.setId(teamUser.getId());
            vo.setUserId(teamUser.getUserId());
            vo.setTeamId(teamUser.getTeamId());
            vo.setType(teamUser.getType());
            vo.setIsDefault(teamUser.getIsDefault());
            
            User user = userMap.get(teamUser.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
                vo.setAccount(user.getAccount());
                vo.setUserIcon(user.getUserIcon());
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public TeamStatsVO getTeamStats(String teamId, String userId) {
        checkTeamMember(teamId, userId);
        
        Team team = getById(teamId);
        if (team == null) {
            throw new BusinessException("error.team.notFound");
        }
        
        // 统计项目数
        Long projectCount = projectMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.freedoc.entity.Project>()
                        .eq(com.freedoc.entity.Project::getTeamId, teamId)
        );
        
        // 统计成员数
        Long memberCount = teamUserMapper.selectCount(
                new LambdaQueryWrapper<TeamUser>().eq(TeamUser::getTeamId, teamId)
        );
        
        // 统计文档数
        Long docCount = docMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.freedoc.entity.Doc>()
                        .eq(com.freedoc.entity.Doc::getTeamId, teamId)
        );
        
        // 获取创建者信息
        String createUserId = team.getCreateUser();
        User createUser = null;
        if (createUserId != null && !createUserId.isEmpty()) {
            createUser = userMapper.selectById(createUserId);
        }
        
        TeamStatsVO vo = new TeamStatsVO();
        vo.setTeamId(teamId);
        vo.setTeamName(team.getTeamName());
        vo.setTeamDesc(team.getTeamDesc());
        vo.setCreateUser(createUserId);
        vo.setCreateUserName(createUser != null ? createUser.getUserName() : null);
        vo.setCreateTime(team.getCreateTime());
        vo.setProjectCount(projectCount.intValue());
        vo.setMemberCount(memberCount.intValue());
        vo.setDocCount(docCount.intValue());

        TeamUser currentTeamUser = teamUserMapper.selectOne(
                new LambdaQueryWrapper<TeamUser>()
                        .eq(TeamUser::getTeamId, teamId)
                        .eq(TeamUser::getUserId, userId)
        );
        if (currentTeamUser != null) {
            vo.setCurrentUserType(currentTeamUser.getType());
        }
        
        return vo;
    }

    @Override
    @Transactional
    public void deleteTeam(String teamId, String userId) {
        checkTeamOwner(teamId, userId);

        // 1. 删除团队成员关联
        teamUserMapper.delete(new LambdaQueryWrapper<TeamUser>().eq(TeamUser::getTeamId, teamId));

        // 2. 删除该团队下的所有项目（级联删除目录、文档、版本、评论）
        List<com.freedoc.entity.Project> projects = projectMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.freedoc.entity.Project>()
                        .eq(com.freedoc.entity.Project::getTeamId, teamId));
        for (com.freedoc.entity.Project project : projects) {
            projectService.deleteProject(project.getProjectId(), userId);
        }

        // 3. 删除团队
        removeById(teamId);
    }

    @Override
    @Transactional
    public Team updateTeam(String teamId, Map<String, Object> updates, String userId) {
        checkTeamOwner(teamId, userId);
        Team team = getById(teamId);
        if (team == null) {
            throw new BusinessException("error.team.notFound");
        }
        if (updates.containsKey("teamName")) {
            team.setTeamName((String) updates.get("teamName"));
        }
        if (updates.containsKey("teamIcon")) {
            team.setTeamIcon((String) updates.get("teamIcon"));
        }
        if (updates.containsKey("teamDesc")) {
            team.setTeamDesc((String) updates.get("teamDesc"));
        }
        updateById(team);
        return team;
    }

    private void checkTeamMember(String teamId, String userId) {
        TeamUser teamUser = teamUserMapper.selectOne(
                new LambdaQueryWrapper<TeamUser>()
                        .eq(TeamUser::getTeamId, teamId)
                        .eq(TeamUser::getUserId, userId)
        );
        if (teamUser == null) {
            throw new BusinessException("error.team.noPermissionAccess");
        }
    }

    private void checkTeamOwner(String teamId, String userId) {
        TeamUser teamUser = teamUserMapper.selectOne(
                new LambdaQueryWrapper<TeamUser>()
                        .eq(TeamUser::getTeamId, teamId)
                        .eq(TeamUser::getUserId, userId)
        );
        if (teamUser == null || !Constants.OWNER.equals(teamUser.getType())) {
            throw new BusinessException("error.team.onlyOwnerCanOperate");
        }
    }

}
