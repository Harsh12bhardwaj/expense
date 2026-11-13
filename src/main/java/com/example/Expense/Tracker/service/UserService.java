package com.example.Expense.Tracker.service;

import com.example.Expense.Tracker.dto.auth.*;
import com.example.Expense.Tracker.exception.BadRequestException;
import com.example.Expense.Tracker.exception.ResourceNotFoundException;
import com.example.Expense.Tracker.exception.UnauthorizedException;
import com.example.Expense.Tracker.model.User;
import com.example.Expense.Tracker.repository.TransactionRepository;
import com.example.Expense.Tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BadRequestException("Username '" + username + "' is already taken.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email '" + email + "' is already registered.");
        }

        User user = User.builder()
                .name(request.getName() != null && !request.getName().isBlank() ? request.getName().trim() : username)
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        return toAuthResponse(savedUser);
    }

    // Backward-compatible register method
    public User registerUser(User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new BadRequestException("Username is required");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getUsername());
        }
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsername().trim();
        User user = userRepository.findByUsernameIgnoreCase(identifier)
                .or(() -> userRepository.findByEmailIgnoreCase(identifier))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        // Match password with BCrypt (or fallback to plain text if migrated from old records)
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!matches && user.getPassword().equals(request.getPassword())) {
            // Self-heal plain text password into BCrypt
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
            matches = true;
        }

        if (!matches) {
            throw new UnauthorizedException("Invalid username or password");
        }

        return toAuthResponse(user);
    }

    // Backward-compatible login method returning User
    public User login(String username, String password) {
        String identifier = username != null ? username.trim() : "";
        User user = userRepository.findByUsernameIgnoreCase(identifier)
                .or(() -> userRepository.findByEmailIgnoreCase(identifier))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (!matches && user.getPassword().equals(password)) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            matches = true;
        }

        if (!matches) {
            throw new UnauthorizedException("Invalid username or password");
        }

        return user;
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        long count = transactionRepository.findByUserUsername(username).size();

        return UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .totalTransactions(count)
                .build();
    }

    public UserProfileDto updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String newEmail = request.getEmail().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new BadRequestException("Email is already registered with another account.");
        }

        user.setName(request.getName().trim());
        user.setEmail(newEmail);
        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        return getProfile(saved.getUsername());
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())
                && !user.getPassword().equals(request.getCurrentPassword())) {
            throw new BadRequestException("Current password does not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String tokenPayload = user.getUsername() + ":" + UUID.randomUUID().toString();
        String token = Base64.getEncoder().encodeToString(tokenPayload.getBytes(StandardCharsets.UTF_8));

        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .token(token)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
