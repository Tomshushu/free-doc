package com.freedoc.dto;

import lombok.Data;

/**
 * 项目成员VO（包含用户信息）
 */
@Data
public class ProjectMemberVO {

    private String id;
    private String projectId;
    private String userId;
    private String type;
    private String permission;

    // 用户信息
    private String userName;
    private String account;
    private String userIcon;
}
