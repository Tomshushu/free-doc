package com.freedoc.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("doc")
public class Doc implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("doc_id")
    private String docId;

    private String directoryId;

    private String docIcon;

    private String docTitle;

    private String docContent;

    private String teamId;

    private String projectId;

    private String createUser;

    private LocalDateTime createTime;

    private String updateUser;

    private LocalDateTime updateTime;

}
