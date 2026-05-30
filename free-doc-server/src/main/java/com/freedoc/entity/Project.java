package com.freedoc.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("doc_project")
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("project_id")
    private String projectId;

    private String projectName;

    private String projectIcon;

    private String teamId;

    private String projectDesc;

    private String createUser;

    private LocalDateTime createTime;

}
