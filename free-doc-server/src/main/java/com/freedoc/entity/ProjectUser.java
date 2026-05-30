package com.freedoc.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("doc_project_user")
public class ProjectUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    private String projectId;

    private String userId;

    private String type;

    private String permission;

}
