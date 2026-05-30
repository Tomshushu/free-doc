package com.freedoc.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("doc_team_user")
public class TeamUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    private String userId;

    private String teamId;

    private String type;

    private Boolean isDefault;

}
