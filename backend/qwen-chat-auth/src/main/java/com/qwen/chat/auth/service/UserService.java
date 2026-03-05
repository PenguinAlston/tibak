package com.qwen.chat.auth.service;

import com.qwen.chat.auth.dto.LoginResponse;
import com.qwen.chat.auth.dto.RegisterRequest;
import com.qwen.chat.auth.entity.User;

public interface UserService {

    User register(RegisterRequest request);

    LoginResponse login(String username, String password);

    UserDetailsImpl loadUserByUsername(String username);
}
