package com.ecommerce.user.controller;

import com.ecommerce.user.dto.request.ChangePasswordRequest;
import com.ecommerce.user.dto.request.RegisterUserRequest;
import com.ecommerce.user.dto.request.UpdateUserRequest;
import com.ecommerce.user.dto.response.UserResponse;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService) {
                this.userService = userService;
        }

        @PostMapping("/register")
        public ResponseEntity<UserResponse> register(
                        @Valid @RequestBody RegisterUserRequest request) {

                UserResponse response = userService.register(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        @GetMapping("/me")
        public ResponseEntity<UserResponse> getCurrentUser(
                        Authentication authentication) {

                Long userId = Long.valueOf(authentication.getName());

                return ResponseEntity.ok(
                                userService.getCurrentUser(userId));
        }

        @GetMapping("/{id}")
        public ResponseEntity<UserResponse> getUserById(
                        @PathVariable Long id,
                        Authentication authentication) {

                Long authenticatedUserId = Long.valueOf(authentication.getName());

                boolean isAdmin = authentication.getAuthorities()
                                .stream()
                                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

                return ResponseEntity.ok(
                                userService.getUserById(
                                                id,
                                                authenticatedUserId,
                                                isAdmin));
        }

        @PutMapping("/me")
        public ResponseEntity<UserResponse> updateCurrentUser(
                        @Valid @RequestBody UpdateUserRequest request,
                        Authentication authentication) {

                Long userId = Long.valueOf(authentication.getName());

                return ResponseEntity.ok(
                                userService.updateCurrentUser(userId, request));
        }

        @PatchMapping("/me/password")
        public ResponseEntity<Void> changePassword(
                        @Valid @RequestBody ChangePasswordRequest request,
                        Authentication authentication) {

                Long userId = Long.valueOf(authentication.getName());

                userService.changePassword(userId, request);

                return ResponseEntity.noContent().build();
        }
}