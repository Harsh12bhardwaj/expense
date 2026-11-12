package com.example.Expense.Tracker.dto.transaction;

import com.example.Expense.Tracker.model.Transaction;
import com.example.Expense.Tracker.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String description;
    private String text; // For backward compatibility
    private double amount;
    private TransactionType type;
    private String category;
    private LocalDate date;
    private LocalDateTime createdAt;

    public static TransactionResponse fromEntity(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .description(t.getDescription())
                .text(t.getDescription())
                .amount(t.getAmount())
                .type(t.getType())
                .category(t.getCategory())
                .date(t.getDate())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
