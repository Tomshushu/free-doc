package com.freedoc.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareVO {

    private String shareId;
    private String shareCode;
    private String targetType;
    private String targetId;
    private Boolean needPassword;
    private Boolean isExpired;
    private LocalDateTime expireTime;
    private String createUser;
    private LocalDateTime createTime;

    // 公开视角附加信息
    private String projectName;
    private String projectDesc;
    private String projectIcon;
    private String docTitle;
    private String docIcon;
    private String shareUserName;

}
