package com.example.Expense.Tracker.controller;

import com.example.Expense.Tracker.dto.auth.*;
import com.example.Expense.Tracker.dto.common.ApiResponse;
import com.example.Expense.Tracker.model.User;
import com.example.Expense.Tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ExpTrack")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    // Enhanced Register Endpoint
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerModern(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    // Backward-compatible register endpoint (matches original signature & path)
    @PostMapping("/register")
    public ResponseEntity<?> registerLegacy(@RequestBody RegisterRequest request) {
        try {
            AuthResponse auth = userService.register(request);
            return ResponseEntity.ok(auth);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Enhanced Login Endpoint
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginModern(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // Backward-compatible login endpoint (matches original path)
    @PostMapping("/login")
    public ResponseEntity<?> loginLegacy(@RequestBody LoginRequest request) {
        try {
            AuthResponse auth = userService.login(request);
            return ResponseEntity.ok(auth);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // User Profile
    @GetMapping("/users/profile/{username}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(@PathVariable String username) {
        UserProfileDto profile = userService.getProfile(username);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    // Update Profile
    @PutMapping("/users/profile/{username}")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @PathVariable String username,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDto updated = userService.updateProfile(username, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    // Change Password
    @PutMapping("/users/password/{username}")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @PathVariable String username,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(username, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", "OK"));
    }
}
