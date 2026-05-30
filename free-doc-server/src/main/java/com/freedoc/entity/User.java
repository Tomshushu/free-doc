package com.freedoc.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("doc_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("user_id")
    private String userId;

    private String userName;

    private String userIcon;

    private String account;

    private String password;

    private String salt;

    private String createUser;

    private LocalDateTime createTime;

}
