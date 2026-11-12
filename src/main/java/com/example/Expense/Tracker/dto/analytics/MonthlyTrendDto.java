package com.example.Expense.Tracker.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendDto {
    private String monthName; // e.g., "Nov 2026"
    private int month;
    private int year;
    private double income;
    private double expense;
    private double netSavings;
}
