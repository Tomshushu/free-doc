package com.freedoc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateRequest {

    @NotBlank(message = "{validation.docId.notBlank}")
    private String docId;

    private String parentCommentId;

    @NotBlank(message = "{validation.commentContent.notBlank}")
    private String content;

}
