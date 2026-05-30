package com.freedoc.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("doc_directory")
public class Directory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    private String name;

    private String pid;

    private String pids;

    private String projectId;

    private String teamId;

    private String createUser;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String updateUser;

}
