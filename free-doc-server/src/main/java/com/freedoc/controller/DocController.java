package com.freedoc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.freedoc.common.result.R;
import com.freedoc.dto.DocCreateRequest;
import com.freedoc.dto.DocUpdateRequest;
import com.freedoc.entity.Doc;
import com.freedoc.security.UserPrincipal;
import com.freedoc.service.DocService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doc")
@RequiredArgsConstructor
public class DocController {

    private final DocService docService;

    @PostMapping
    public R<Doc> createDoc(@Valid @RequestBody DocCreateRequest request,
                            @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(docService.createDoc(request, principal.getUserId()));
    }

    @PutMapping
    public R<Doc> updateDoc(@Valid @RequestBody DocUpdateRequest request,
                            @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(docService.updateDoc(request, principal.getUserId()));
    }

    @DeleteMapping("/{docId}")
    public R<Void> deleteDoc(@PathVariable String docId,
                             @AuthenticationPrincipal UserPrincipal principal) {
        docService.deleteDoc(docId, principal.getUserId());
        return R.ok();
    }

    @GetMapping("/{docId}")
    public R<Doc> getDocById(@PathVariable String docId,
                             @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(docService.getDocById(docId, principal.getUserId()));
    }
    
    @GetMapping("/detail/{docId}")
    public R<Doc> getDocByIdWithUser(@PathVariable String docId,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(docService.getDocByIdWithUser(docId, principal.getUserId()));
    }

    @GetMapping("/directory/{directoryId}")
    public R<List<Doc>> getDirectoryDocs(@PathVariable String directoryId,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(docService.getDirectoryDocs(directoryId, principal.getUserId()));
    }

    @GetMapping("/search")
    public R<Page<Doc>> searchDocs(@RequestParam String keyword,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String projectId,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(docService.searchDocs(keyword, page, size, principal.getUserId(), projectId));
    }

    @GetMapping("/recent")
    public R<List<Doc>> getRecentDocs(@RequestParam(defaultValue = "10") int limit,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(docService.getRecentDocs(principal.getUserId(), limit));
    }

    /** 获取项目下所有文档 */
    @GetMapping("/project/{projectId}")
    public R<List<Doc>> getProjectDocs(@PathVariable String projectId,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(docService.getProjectDocs(projectId, principal.getUserId()));
    }

}
