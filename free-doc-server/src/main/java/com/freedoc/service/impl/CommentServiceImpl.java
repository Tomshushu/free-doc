package com.freedoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.freedoc.common.exception.BusinessException;
import com.freedoc.common.util.SnowflakeIdUtil;
import com.freedoc.dto.CommentCreateRequest;
import com.freedoc.entity.Comment;
import com.freedoc.mapper.CommentMapper;
import com.freedoc.service.CommentService;
import com.freedoc.service.DocService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final DocService docService;

    @Override
    public Comment createComment(CommentCreateRequest request, String userId) {
        docService.getDocById(request.getDocId(), userId);

        if (request.getParentCommentId() != null) {
            Comment parent = getById(request.getParentCommentId());
            if (parent == null) {
                throw new BusinessException("error.comment.parentNotFound");
            }
        }

        Comment comment = new Comment();
        comment.setCommentId(SnowflakeIdUtil.nextId());
        comment.setDocId(request.getDocId());
        comment.setParentCommentId(request.getParentCommentId());
        comment.setContent(request.getContent());
        comment.setCreateBy(userId);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        save(comment);

        return comment;
    }

    @Override
    public void deleteComment(String commentId, String userId) {
        Comment comment = getById(commentId);
        if (comment == null) {
            throw new BusinessException("error.comment.notFound");
        }

        if (!comment.getCreateBy().equals(userId)) {
            throw new BusinessException("error.comment.noPermissionDelete");
        }

        remove(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getParentCommentId, commentId));

        removeById(commentId);
    }

    @Override
    public List<Comment> getDocComments(String docId, String userId) {
        docService.getDocById(docId, userId);
        return list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDocId, docId)
                .orderByAsc(Comment::getCreateTime));
    }

}
