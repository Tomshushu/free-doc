package com.freedoc.controller;

import com.freedoc.common.result.R;
import com.freedoc.dto.TeamCreateRequest;
import com.freedoc.dto.TeamMemberRequest;
import com.freedoc.dto.TeamMemberVO;
import com.freedoc.dto.TeamStatsVO;
import com.freedoc.entity.Team;
import com.freedoc.security.UserPrincipal;
import com.freedoc.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public R<Team> createTeam(@Valid @RequestBody TeamCreateRequest request,
                              @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(teamService.createTeam(request, principal.getUserId()));
    }

    @GetMapping("/list")
    public R<List<Team>> getUserTeams(@AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(teamService.getUserTeams(principal.getUserId()));
    }

    @GetMapping("/{teamId}")
    public R<Team> getTeamById(@PathVariable String teamId,
                               @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(teamService.getTeamById(teamId, principal.getUserId()));
    }

    @PutMapping("/{teamId}")
    public R<Team> updateTeam(@PathVariable String teamId,
                              @RequestBody Map<String, Object> updates,
                              @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(teamService.updateTeam(teamId, updates, principal.getUserId()));
    }

    @DeleteMapping("/{teamId}")
    public R<Void> deleteTeam(@PathVariable String teamId,
                              @AuthenticationPrincipal UserPrincipal principal) {
        teamService.deleteTeam(teamId, principal.getUserId());
        return R.ok();
    }

    @PostMapping("/{teamId}/member")
    public R<Void> addMember(@PathVariable String teamId,
                             @Valid @RequestBody TeamMemberRequest request,
                             @AuthenticationPrincipal UserPrincipal principal) {
        teamService.addMember(teamId, request, principal.getUserId());
        return R.ok();
    }

    @DeleteMapping("/{teamId}/member/{userId}")
    public R<Void> removeMember(@PathVariable String teamId,
                                @PathVariable String userId,
                                @AuthenticationPrincipal UserPrincipal principal) {
        teamService.removeMember(teamId, userId, principal.getUserId());
        return R.ok();
    }

    @GetMapping("/{teamId}/members")
    public R<List<TeamMemberVO>> getTeamMembers(@PathVariable String teamId,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(teamService.getTeamMembersWithUser(teamId, principal.getUserId()));
    }

    @GetMapping("/{teamId}/stats")
    public R<TeamStatsVO> getTeamStats(@PathVariable String teamId,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(teamService.getTeamStats(teamId, principal.getUserId()));
    }

}
