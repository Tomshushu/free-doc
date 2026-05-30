package com.freedoc.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("doc_comments")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("comment_id")
    private String commentId;

    private String docId;

    private String parentCommentId;

    private String content;

    private String createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
