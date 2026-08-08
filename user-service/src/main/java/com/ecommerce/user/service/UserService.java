package com.ecommerce.user.service;

import com.ecommerce.user.dto.request.ChangePasswordRequest;
import com.ecommerce.user.dto.request.LoginRequest;
import com.ecommerce.user.dto.request.RegisterUserRequest;
import com.ecommerce.user.dto.request.UpdateUserRequest;
import com.ecommerce.user.dto.response.LoginResponse;
import com.ecommerce.user.dto.response.UserResponse;

public interface UserService {

    UserResponse register(RegisterUserRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getCurrentUser(Long userId);

    UserResponse getUserById(Long userId, Long authenticatedUserId, boolean isAdmin);

    UserResponse updateCurrentUser(Long userId, UpdateUserRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);
}