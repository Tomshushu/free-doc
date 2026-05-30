package com.freedoc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedoc.dto.TeamCreateRequest;
import com.freedoc.dto.TeamMemberRequest;
import com.freedoc.dto.TeamMemberVO;
import com.freedoc.dto.TeamStatsVO;
import com.freedoc.entity.Team;
import com.freedoc.entity.TeamUser;

import java.util.List;
import java.util.Map;

public interface TeamService extends IService<Team> {

    Team createTeam(TeamCreateRequest request, String userId);

    List<Team> getUserTeams(String userId);

    Team getTeamById(String teamId, String userId);

    void addMember(String teamId, TeamMemberRequest request, String operatorId);

    void removeMember(String teamId, String userId, String operatorId);

    List<TeamUser> getTeamMembers(String teamId, String userId);

    /**
     * 获取团队成员（包含用户信息）
     */
    List<TeamMemberVO> getTeamMembersWithUser(String teamId, String userId);

    /**
     * 获取团队统计信息（项目数、成员数、文档数）
     */
    TeamStatsVO getTeamStats(String teamId, String userId);

    /**
     * 删除团队（级联删除项目、目录、文档等）
     */
    void deleteTeam(String teamId, String userId);

    /**
     * 更新团队基本信息
     */
    Team updateTeam(String teamId, Map<String, Object> updates, String userId);

}
