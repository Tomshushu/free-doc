package com.freedoc.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("doc_share")
public class Share implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("share_id")
    private String shareId;

    private String shareCode;

    private String targetType;

    private String targetId;

    private String password;

    private LocalDateTime expireTime;

    private String createUser;

    private LocalDateTime createTime;

}
