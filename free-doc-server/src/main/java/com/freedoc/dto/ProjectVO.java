package com.freedoc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectVO {

    private String projectId;
    private String projectName;
    private String projectIcon;
    private String teamId;
    private String projectDesc;
    private String createUser;
    private LocalDateTime createTime;

    private String createUserName;
    private String createUserAccount;

    private Boolean isOwner;

    private String permission;

    public boolean canEdit() {
        return Boolean.TRUE.equals(isOwner) || "rw".equals(permission);
    }

    public boolean canDelete() {
        return Boolean.TRUE.equals(isOwner);
    }

    public boolean canManageMembers() {
        return Boolean.TRUE.equals(isOwner);
    }
}
