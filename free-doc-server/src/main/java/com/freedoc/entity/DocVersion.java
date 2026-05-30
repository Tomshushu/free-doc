package com.freedoc.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("doc_version")
public class DocVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("version_id")
    private String versionId;

    private Integer versionNum;

    private String docId;

    private String docContent;

    private String diffContent;

    private Boolean isCurrent;

    private String contentHash;

    private LocalDateTime createTime;

    private String createUser;

}
