package com.qwen.chat.auth.controller;

import com.qwen.chat.auth.dto.LoginRequest;
import com.qwen.chat.auth.dto.LoginResponse;
import com.qwen.chat.auth.dto.RegisterRequest;
import com.qwen.chat.auth.entity.User;
import com.qwen.chat.auth.service.UserService;
import com.qwen.chat.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ApiResponse.success("注册成功", user);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request.getUsername(), request.getPassword());
        return ApiResponse.success(response);
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse.UserInfo> getCurrentUser(
            @RequestAttribute("username") String username) {
        User user = userService.loadUserByUsername(username).getUser();
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .build();
        return ApiResponse.success(userInfo);
    }
}
