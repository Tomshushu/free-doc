package com.freedoc.controller;

import com.freedoc.common.result.R;
import com.freedoc.entity.User;
import com.freedoc.security.UserPrincipal;
import com.freedoc.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public R<User> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getById(principal.getUserId());
        user.setPassword(null);
        user.setSalt(null);
        return R.ok(user);
    }

    @PutMapping("/info")
    public R<User> updateUserInfo(@RequestBody Map<String, Object> updates,
                                  @AuthenticationPrincipal UserPrincipal principal) {
        return R.ok(userService.updateUserInfo(principal.getUserId(), updates));
    }

    @PostMapping("/password")
    public R<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        userService.changePassword(principal.getUserId(), request.getOldPassword(), request.getNewPassword());
        return R.ok();
    }

    @GetMapping("/search")
    public R<List<User>> searchUsers(@RequestParam String keyword) {
        return R.ok(userService.searchUsers(keyword));
    }

    @GetMapping("/{userId}")
    public R<User> getUserById(@PathVariable String userId) {
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
            user.setSalt(null);
        }
        return R.ok(user);
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "{validation.oldPassword.notBlank}")
        private String oldPassword;

        @NotBlank(message = "{validation.newPassword.notBlank}")
        @Size(min = 6, message = "{validation.newPassword.size}")
        private String newPassword;
    }

}
