package com.ecommerce.user.service.impl;

import com.ecommerce.user.dto.request.ChangePasswordRequest;
import com.ecommerce.user.dto.request.LoginRequest;
import com.ecommerce.user.dto.request.RegisterUserRequest;
import com.ecommerce.user.dto.request.ResetPasswordRequest;
import com.ecommerce.user.dto.request.UpdateUserRequest;
import com.ecommerce.user.dto.response.LoginResponse;
import com.ecommerce.user.dto.response.UserResponse;
import com.ecommerce.user.entity.PasswordResetToken;
import com.ecommerce.user.entity.Role;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.entity.UserStatus;
import com.ecommerce.user.exception.AccessDeniedException;
import com.ecommerce.user.exception.EmailAlreadyExistsException;
import com.ecommerce.user.exception.GlobalExceptionHandler.ErrorResponse;
import com.ecommerce.user.exception.InvalidCredentialsException;
import com.ecommerce.user.exception.InvalidPasswordException;
import com.ecommerce.user.exception.InvalidPasswordResetTokenException;
import com.ecommerce.user.exception.UserAccountException;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.repository.PasswordResetTokenRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.security.PasswordResetTokenGenerator;
import com.ecommerce.user.service.JwtService;
import com.ecommerce.user.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenGenerator passwordResetTokenGenerator;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService,
            PasswordResetTokenGenerator pwdResetTokenGenerator, PasswordResetTokenRepository pwdResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordResetTokenGenerator = pwdResetTokenGenerator;
        this.passwordResetTokenRepository = pwdResetTokenRepository;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        Instant now = Instant.now();

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    private UserResponse toResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserAccountException("User account is not active");
        }

        String accessToken = jwtService.generateToken(user);

        return new LoginResponse(accessToken, "Bearer");
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return toResponse(user);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException exception) {

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "USER_NOT_FOUND",
                exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(
            Long userId,
            Long authenticatedUserId,
            boolean isAdmin) {

        if (!isAdmin && !userId.equals(authenticatedUserId)) {
            throw new AccessDeniedException(
                    "You do not have permission to access this user");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(
            Long userId,
            UpdateUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        User updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(
            Long userId,
            ChangePasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash())) {

            throw new InvalidPasswordException(
                    "Current password is incorrect");
        }

        user.setPasswordHash(
                passwordEncoder.encode(request.newPassword()));

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();

        passwordResetTokenRepository.deleteByUser(user);

        PasswordResetTokenGenerator.GeneratedResetToken generated = passwordResetTokenGenerator.generate();

        PasswordResetToken resetToken = new PasswordResetToken();

        resetToken.setUser(user);
        resetToken.setTokenHash(generated.tokenHash());
        resetToken.setExpiresAt(generated.expiresAt());
        resetToken.setUsed(false);
        resetToken.setCreatedAt(Instant.now());

        passwordResetTokenRepository.save(resetToken);

        // Notification event will be added when we implement Kafka.
        // For now, the raw token is available here for that integration.
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        String tokenHash = passwordResetTokenGenerator.hash(request.token());

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidPasswordResetTokenException(
                        "Invalid or expired password reset token"));

        if (resetToken.isUsed()) {
            throw new InvalidPasswordResetTokenException(
                    "Invalid or expired password reset token");
        }

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidPasswordResetTokenException(
                    "Invalid or expired password reset token");
        }

        User user = resetToken.getUser();

        user.setPasswordHash(
                passwordEncoder.encode(request.newPassword()));

        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}