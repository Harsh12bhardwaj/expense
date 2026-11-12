package com.example.Expense.Tracker.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    private Long id;
    private String category;
    private double monthlyLimit;
    private int month;
    private int year;
    private double spent;
    private double remaining;
    private double percentageUsed;
    private String status; // NORMAL, WARNING, EXCEEDED
}
