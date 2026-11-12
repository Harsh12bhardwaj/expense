package com.example.Expense.Tracker.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    private List<MonthlyTrendDto> monthlyTrends;
    private List<CategoryExpenseDto> categoryBreakdown;
    private double overallIncome;
    private double overallExpense;
    private double netSavings;
    private String highestSpendingCategory;
    private double averageMonthlyExpense;
}
