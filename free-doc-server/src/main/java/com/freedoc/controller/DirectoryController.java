package com.freedoc.controller;

import com.freedoc.common.result.R;
import com.freedoc.dto.DirectoryCreateRequest;
import com.freedoc.entity.Directory;
import com.freedoc.security.UserPrincipal;
import com.freedoc.service.DirectoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectoryService directoryService;

    @PostMapping
    public R<Directory> createDirectory(@Valid @RequestBody DirectoryCreateRequest request,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(directoryService.createDirectory(request, principal.getUserId()));
    }

    @GetMapping("/project/{projectId}")
    public R<List<Directory>> getProjectDirectories(@PathVariable String projectId,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(directoryService.getProjectDirectories(projectId, principal.getUserId()));
    }

    @DeleteMapping("/{directoryId}")
    public R<Void> deleteDirectory(@PathVariable String directoryId,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        directoryService.deleteDirectory(directoryId, principal.getUserId());
        return R.ok();
    }

    @PutMapping("/{directoryId}/name")
    public R<Directory> updateDirectoryName(@PathVariable String directoryId,
                                            @RequestParam String name,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(directoryService.updateDirectoryName(directoryId, name, principal.getUserId()));
    }

}
