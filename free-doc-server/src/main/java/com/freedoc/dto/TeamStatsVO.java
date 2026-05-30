package com.freedoc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamStatsVO {

    private String teamId;
    private String teamName;
    private String teamDesc;

    private String createUser;
    private String createUserName;
    private LocalDateTime createTime;

    private Integer projectCount;
    private Integer memberCount;
    private Integer docCount;

    private String currentUserType;

    private String currentUserPermission;

    public boolean isTeamOwner() {
        return "OWNER".equals(currentUserType);
    }

    public boolean canEditTeam() {
        return isTeamOwner();
    }

    public boolean canManageMembers() {
        return isTeamOwner();
    }

    public boolean canCreateProject() {
        return isTeamOwner();
    }
}
