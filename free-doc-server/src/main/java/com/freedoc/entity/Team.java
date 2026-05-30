package com.freedoc.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("doc_team")
public class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("team_id")
    private String teamId;

    private String teamName;

    private String teamIcon;

    private String teamDesc;

    private String createUser;

    private LocalDateTime createTime;

}
