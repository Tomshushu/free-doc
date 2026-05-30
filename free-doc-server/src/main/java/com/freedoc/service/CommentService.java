package com.freedoc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedoc.dto.CommentCreateRequest;
import com.freedoc.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    Comment createComment(CommentCreateRequest request, String userId);

    void deleteComment(String commentId, String userId);

    List<Comment> getDocComments(String docId, String userId);

}
