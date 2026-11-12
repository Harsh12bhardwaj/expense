package com.example.Expense.Tracker.dto.analytics;

import com.example.Expense.Tracker.dto.budget.BudgetResponse;
import com.example.Expense.Tracker.dto.transaction.TransactionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private double totalBalance;
    private double totalIncome;
    private double totalExpense;

    private double thisMonthIncome;
    private double thisMonthExpense;
    private double thisMonthBalance;
    private double savingsRate; // (income - expense)/income * 100

    private long totalTransactionCount;

    private List<TransactionResponse> recentTransactions;
    private List<CategoryExpenseDto> categoryBreakdown;
    private List<BudgetResponse> budgetAlerts;
}
