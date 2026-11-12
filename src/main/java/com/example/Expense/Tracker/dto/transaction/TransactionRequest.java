package com.example.Expense.Tracker.dto.transaction;

import com.example.Expense.Tracker.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private String description;
    private String text; // For backward compatibility

    @NotNull(message = "Amount is required")
    private Double amount;

    private TransactionType type;

    private String category;

    private LocalDate date;

    public String resolveDescription() {
        if (description != null && !description.isBlank()) {
            return description.trim();
        }
        if (text != null && !text.isBlank()) {
            return text.trim();
        }
        return "Transaction";
    }
}
