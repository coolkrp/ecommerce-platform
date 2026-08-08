package com.ecommerce.user.service;

import com.ecommerce.user.entity.User;

public interface JwtService {

    String generateToken(User user);
}