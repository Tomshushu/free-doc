package com.freedoc.dto;

import com.freedoc.entity.Doc;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class DocDetailVO extends Doc {
    
    // 用户信息
    private String createUserName;
    private String createUserAccount;
    private String updateUserName;
    private String updateUserAccount;
    
    // 文档统计
    private Long viewCount;
    private Integer versionCount;
}