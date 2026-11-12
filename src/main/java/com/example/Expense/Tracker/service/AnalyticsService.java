package com.example.Expense.Tracker.service;

import com.example.Expense.Tracker.dto.analytics.AnalyticsResponse;
import com.example.Expense.Tracker.dto.analytics.CategoryExpenseDto;
import com.example.Expense.Tracker.dto.analytics.DashboardSummaryResponse;
import com.example.Expense.Tracker.dto.budget.BudgetResponse;
import com.example.Expense.Tracker.dto.transaction.TransactionResponse;
import com.example.Expense.Tracker.exception.ResourceNotFoundException;
import com.example.Expense.Tracker.model.Transaction;
import com.example.Expense.Tracker.model.TransactionType;
import com.example.Expense.Tracker.repository.TransactionRepository;
import com.example.Expense.Tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetService budgetService;

    public DashboardSummaryResponse getDashboardSummary(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        Double totalIncomeVal = transactionRepository.sumAmountByUserAndType(username, TransactionType.INCOME);
        Double totalExpenseVal = transactionRepository.sumAmountByUserAndType(username, TransactionType.EXPENSE);

        double totalIncome = totalIncomeVal != null ? totalIncomeVal : 0.0;
        double totalExpense = totalExpenseVal != null ? totalExpenseVal : 0.0;
        double totalBalance = totalIncome - totalExpense;

        // Current month
        YearMonth currentYearMonth = YearMonth.now();
        LocalDate startOfMonth = currentYearMonth.atDay(1);
        LocalDate endOfMonth = currentYearMonth.atEndOfMonth();

        Double monthIncomeVal = transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                username, TransactionType.INCOME, startOfMonth, endOfMonth
        );
        Double monthExpenseVal = transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                username, TransactionType.EXPENSE, startOfMonth, endOfMonth
        );

        double monthIncome = monthIncomeVal != null ? monthIncomeVal : 0.0;
        double monthExpense = monthExpenseVal != null ? monthExpenseVal : 0.0;
        double monthBalance = monthIncome - monthExpense;

        double savingsRate = monthIncome > 0 ? ((monthIncome - monthExpense) / monthIncome) * 100.0 : 0.0;

        // Recent transactions
        List<Transaction> recentList = transactionRepository.findRecentTransactions(username);
        long totalCount = recentList.size();
        List<TransactionResponse> recentResponses = recentList.stream()
                .limit(6)
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());

        // Category breakdown (All-time or current month)
        List<CategoryExpenseDto> categoryBreakdown = getCategoryBreakdown(username, null, null);

        // Budget alerts
        List<BudgetResponse> budgets = budgetService.getBudgets(username, currentYearMonth.getMonthValue(), currentYearMonth.getYear());
        List<BudgetResponse> alerts = budgets.stream()
                .filter(b -> !"NORMAL".equals(b.getStatus()))
                .collect(Collectors.toList());

        return DashboardSummaryResponse.builder()
                .totalBalance(round(totalBalance))
                .totalIncome(round(totalIncome))
                .totalExpense(round(totalExpense))
                .thisMonthIncome(round(monthIncome))
                .thisMonthExpense(round(monthExpense))
                .thisMonthBalance(round(monthBalance))
                .savingsRate(round(savingsRate))
                .totalTransactionCount(totalCount)
                .recentTransactions(recentResponses)
                .categoryBreakdown(categoryBreakdown)
                .budgetAlerts(alerts)
                .build();
    }

    public AnalyticsResponse getAnalytics(String username, Integer months) {
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        int count = (months != null && months > 0 && months <= 24) ? months : 6;
        YearMonth current = YearMonth.now();
        List<com.example.Expense.Tracker.dto.analytics.MonthlyTrendDto> trends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        double totalPeriodExpense = 0.0;
        int activeMonths = 0;

        for (int i = count - 1; i >= 0; i--) {
            YearMonth targetMonth = current.minusMonths(i);
            LocalDate start = targetMonth.atDay(1);
            LocalDate end = targetMonth.atEndOfMonth();

            Double incVal = transactionRepository.sumAmountByUserAndTypeAndDateBetween(username, TransactionType.INCOME, start, end);
            Double expVal = transactionRepository.sumAmountByUserAndTypeAndDateBetween(username, TransactionType.EXPENSE, start, end);

            double inc = incVal != null ? incVal : 0.0;
            double exp = expVal != null ? expVal : 0.0;
            double net = inc - exp;

            trends.add(com.example.Expense.Tracker.dto.analytics.MonthlyTrendDto.builder()
                    .monthName(targetMonth.format(formatter))
                    .month(targetMonth.getMonthValue())
                    .year(targetMonth.getYear())
                    .income(round(inc))
                    .expense(round(exp))
                    .netSavings(round(net))
                    .build());

            totalPeriodExpense += exp;
            if (exp > 0) activeMonths++;
        }

        Double allIncome = transactionRepository.sumAmountByUserAndType(username, TransactionType.INCOME);
        Double allExpense = transactionRepository.sumAmountByUserAndType(username, TransactionType.EXPENSE);

        double overallInc = allIncome != null ? allIncome : 0.0;
        double overallExp = allExpense != null ? allExpense : 0.0;

        List<CategoryExpenseDto> categoryBreakdown = getCategoryBreakdown(username, null, null);
        String highestCategory = !categoryBreakdown.isEmpty() ? categoryBreakdown.get(0).getCategory() : "None";
        double avgMonthlyExpense = count > 0 ? (totalPeriodExpense / count) : 0.0;

        return AnalyticsResponse.builder()
                .monthlyTrends(trends)
                .categoryBreakdown(categoryBreakdown)
                .overallIncome(round(overallInc))
                .overallExpense(round(overallExp))
                .netSavings(round(overallInc - overallExp))
                .highestSpendingCategory(highestCategory)
                .averageMonthlyExpense(round(avgMonthlyExpense))
                .build();
    }

    public List<CategoryExpenseDto> getCategoryBreakdown(String username, LocalDate startDate, LocalDate endDate) {
        List<Object[]> raw = transactionRepository.getCategoryExpenseBreakdown(username, startDate, endDate);
        double totalExpense = 0.0;
        for (Object[] row : raw) {
            Double amount = (Double) row[1];
            if (amount != null) totalExpense += amount;
        }

        List<CategoryExpenseDto> list = new ArrayList<>();
        for (Object[] row : raw) {
            String cat = (String) row[0];
            Double amt = (Double) row[1];
            double amount = amt != null ? amt : 0.0;
            double pct = totalExpense > 0 ? (amount / totalExpense) * 100.0 : 0.0;

            list.add(CategoryExpenseDto.builder()
                    .category(cat)
                    .amount(round(amount))
                    .percentage(round(pct))
                    .build());
        }
        return list;
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
