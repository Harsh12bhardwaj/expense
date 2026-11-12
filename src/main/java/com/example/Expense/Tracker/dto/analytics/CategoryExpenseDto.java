package com.example.Expense.Tracker.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryExpenseDto {
    private String category;
    private double amount;
    private double percentage;
}
