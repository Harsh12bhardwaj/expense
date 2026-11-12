package com.example.Expense.Tracker.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String token; // Token or session identifier for secure client identification
    private LocalDateTime createdAt;
}
