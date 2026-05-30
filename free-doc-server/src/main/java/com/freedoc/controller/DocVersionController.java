package com.freedoc.controller;

import com.freedoc.common.result.R;
import com.freedoc.entity.DocVersion;
import com.freedoc.security.UserPrincipal;
import com.freedoc.service.DocVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/version")
@RequiredArgsConstructor
public class DocVersionController {

    private final DocVersionService docVersionService;

    @GetMapping("/doc/{docId}")
    public R<List<DocVersion>> getDocVersions(@PathVariable String docId,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(docVersionService.getDocVersions(docId, principal.getUserId()));
    }

    @GetMapping("/{versionId}")
    public R<DocVersion> getVersion(@PathVariable String versionId,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(docVersionService.getVersion(versionId, principal.getUserId()));
    }

    @PostMapping("/rollback/{docId}/{versionId}")
    public R<Void> rollbackToVersion(@PathVariable String docId,
                                     @PathVariable String versionId,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        docVersionService.rollbackToVersion(docId, versionId, principal.getUserId());
        return R.ok();
    }

    @DeleteMapping("/after/{docId}/{versionNum}")
    public R<Void> deleteVersionsAfter(@PathVariable String docId,
                                       @PathVariable Integer versionNum,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        docVersionService.deleteVersionsAfter(docId, versionNum, principal.getUserId());
        return R.ok();
    }

}
