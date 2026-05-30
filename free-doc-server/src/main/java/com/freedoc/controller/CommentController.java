package com.freedoc.controller;

import com.freedoc.common.result.R;
import com.freedoc.dto.CommentCreateRequest;
import com.freedoc.entity.Comment;
import com.freedoc.security.UserPrincipal;
import com.freedoc.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public R<Comment> createComment(@Valid @RequestBody CommentCreateRequest request,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(commentService.createComment(request, principal.getUserId()));
    }

    @DeleteMapping("/{commentId}")
    public R<Void> deleteComment(@PathVariable String commentId,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        commentService.deleteComment(commentId, principal.getUserId());
        return R.ok();
    }

    @GetMapping("/doc/{docId}")
    public R<List<Comment>> getDocComments(@PathVariable String docId,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(commentService.getDocComments(docId, principal.getUserId()));
    }

}
