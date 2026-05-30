package com.freedoc.dto;

import lombok.Data;

/**
 * 团队成员VO（包含用户信息）
 */
@Data
public class TeamMemberVO {

    private String id;
    private String userId;
    private String teamId;
    private String type;
    private Boolean isDefault;

    // 用户信息
    private String userName;
    private String account;
    private String userIcon;
}
