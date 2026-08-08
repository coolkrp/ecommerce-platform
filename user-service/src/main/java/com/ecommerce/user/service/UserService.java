package com.ecommerce.user.service;

import com.ecommerce.user.dto.request.RegisterUserRequest;
import com.ecommerce.user.dto.response.UserResponse;

public interface UserService {

    UserResponse register(RegisterUserRequest request);
}