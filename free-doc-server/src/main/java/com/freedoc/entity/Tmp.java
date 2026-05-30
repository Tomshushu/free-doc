package com.freedoc.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("doc_tmp")
public class Tmp implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("tmp_id")
    private String tmpId;

    private String teamId;

    private String projectId;

    private String tmpIcon;

    private String tmpTitle;

    private String tmpContent;

    /** 模板类型 SYS-系统模板 SELF-自建模板 */
    private String type;

    private String createUser;

    private LocalDateTime createTime;

}
