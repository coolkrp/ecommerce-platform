package com.ecommerce.user.service;

import com.ecommerce.user.dto.request.LoginRequest;
import com.ecommerce.user.dto.request.RegisterUserRequest;
import com.ecommerce.user.dto.response.LoginResponse;
import com.ecommerce.user.dto.response.UserResponse;

public interface UserService {

    UserResponse register(RegisterUserRequest request);
    LoginResponse login(LoginRequest request);
    UserResponse getCurrentUser(Long userId);
    UserResponse getUserById(Long userId, Long authenticatedUserId, boolean isAdmin);
}