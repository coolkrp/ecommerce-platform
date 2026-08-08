package com.ecommerce.user.service;

import com.ecommerce.user.entity.User;

public interface JwtService {

    String generateToken(User user);

    boolean isTokenValid(String token);

    String extractUserId(String token);

    String extractRole(String token);
}