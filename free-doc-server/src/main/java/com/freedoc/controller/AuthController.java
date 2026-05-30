package com.freedoc.controller;

import com.freedoc.common.result.R;
import com.freedoc.dto.LoginRequest;
import com.freedoc.dto.LoginResponse;
import com.freedoc.dto.RegisterRequest;
import com.freedoc.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(userService.login(request));
    }

    @PostMapping("/register")
    public R<String> register(@Valid @RequestBody RegisterRequest request) {
        String userId = userService.register(request);
        return R.ok(userId);
    }

}
