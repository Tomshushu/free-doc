package com.freedoc.controller;

import com.freedoc.common.result.R;
import com.freedoc.dto.ShareCreateRequest;
import com.freedoc.dto.ShareVO;
import com.freedoc.entity.Share;
import com.freedoc.security.UserPrincipal;
import com.freedoc.service.ShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping("/share")
    public R<ShareVO> createShare(@Valid @RequestBody ShareCreateRequest request,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(shareService.createShare(request, principal.getUserId()));
    }

    @DeleteMapping("/share/{shareId}")
    public R<Void> deleteShare(@PathVariable String shareId,
                               @AuthenticationPrincipal UserPrincipal principal) {
        shareService.deleteShare(shareId, principal.getUserId());
        return R.ok();
    }

    @GetMapping("/share/list")
    public R<List<ShareVO>> getShareList(@RequestParam(required = false) String targetType,
                                         @RequestParam(required = false) String targetId,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(shareService.getShareList(targetType, targetId, principal.getUserId()));
    }

    // ===== 无需JWT认证的公开接口（路径匹配 /*/no-auth/**）=====

    @GetMapping("/share/no-auth/{shareCode}")
    public R<ShareVO> getSharePublicInfo(@PathVariable String shareCode) {
        return R.ok(shareService.getSharePublicInfo(shareCode));
    }

    @PostMapping("/share/no-auth/{shareCode}/verify")
    public R<ShareVO> verifyShare(@PathVariable String shareCode,
                                  @RequestBody Map<String, String> body) {
        String password = body.get("password");
        return R.ok(shareService.verifyShare(shareCode, password));
    }

    @GetMapping("/share/no-auth/{shareCode}/project")
    public R<Object> getShareProjectContent(@PathVariable String shareCode,
                                            @RequestHeader(value = "X-Share-Token", required = false) String shareToken) {
        return R.ok(shareService.getShareProjectContent(shareCode, shareToken));
    }

    @GetMapping("/share/no-auth/{shareCode}/doc/{docId}")
    public R<Object> getShareDocContent(@PathVariable String shareCode,
                                        @PathVariable String docId,
                                        @RequestHeader(value = "X-Share-Token", required = false) String shareToken) {
        return R.ok(shareService.getShareDocContent(shareCode, docId, shareToken));
    }

}
