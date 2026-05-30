package com.freedoc.controller;

import com.freedoc.common.result.R;
import com.freedoc.dto.ProjectCreateRequest;
import com.freedoc.dto.ProjectMemberRequest;
import com.freedoc.dto.ProjectMemberVO;
import com.freedoc.dto.ProjectVO;
import com.freedoc.entity.Project;
import com.freedoc.security.UserPrincipal;
import com.freedoc.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public R<Project> createProject(@Valid @RequestBody ProjectCreateRequest request,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(projectService.createProject(request, principal.getUserId()));
    }

    @GetMapping("/team/{teamId}")
    public R<List<ProjectVO>> getTeamProjects(@PathVariable String teamId,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(projectService.getTeamProjectsWithCreator(teamId, principal.getUserId()));
    }

    @GetMapping("/{projectId}")
    public R<Project> getProjectById(@PathVariable String projectId,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(projectService.getProjectById(projectId, principal.getUserId()));
    }

    @PutMapping("/{projectId}")
    public R<Project> updateProject(@PathVariable String projectId,
                                    @RequestBody Map<String, Object> updates,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(projectService.updateProject(projectId, updates, principal.getUserId()));
    }

    @DeleteMapping("/{projectId}")
    public R<Void> deleteProject(@PathVariable String projectId,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        projectService.deleteProject(projectId, principal.getUserId());
        return R.ok();
    }

    @PostMapping("/{projectId}/member")
    public R<Void> addMember(@PathVariable String projectId,
                             @Valid @RequestBody ProjectMemberRequest request,
                             @AuthenticationPrincipal UserPrincipal principal) {
        projectService.addMember(projectId, request, principal.getUserId());
        return R.ok();
    }

    @DeleteMapping("/{projectId}/member/{userId}")
    public R<Void> removeMember(@PathVariable String projectId,
                                @PathVariable String userId,
                                @AuthenticationPrincipal UserPrincipal principal) {
        projectService.removeMember(projectId, userId, principal.getUserId());
        return R.ok();
    }

    @GetMapping("/{projectId}/members")
    public R<List<ProjectMemberVO>> getProjectMembers(@PathVariable String projectId,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(projectService.getProjectMembersWithUser(projectId, principal.getUserId()));
    }

    @PutMapping("/{projectId}/member/{userId}")
    public R<Void> updateMember(@PathVariable String projectId,
                                @PathVariable String userId,
                                @RequestBody Map<String, Object> updates,
                                @AuthenticationPrincipal UserPrincipal principal) {
        projectService.updateMember(projectId, userId, updates, principal.getUserId());
        return R.ok();
    }

}
