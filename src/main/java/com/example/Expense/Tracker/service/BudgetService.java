package com.example.Expense.Tracker.service;

import com.example.Expense.Tracker.dto.budget.BudgetRequest;
import com.example.Expense.Tracker.dto.budget.BudgetResponse;
import com.example.Expense.Tracker.exception.BadRequestException;
import com.example.Expense.Tracker.exception.ResourceNotFoundException;
import com.example.Expense.Tracker.model.Budget;
import com.example.Expense.Tracker.model.User;
import com.example.Expense.Tracker.repository.BudgetRepository;
import com.example.Expense.Tracker.repository.TransactionRepository;
import com.example.Expense.Tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public BudgetResponse setBudget(String username, BudgetRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        int month = request.getMonth() > 0 ? request.getMonth() : LocalDate.now().getMonthValue();
        int year = request.getYear() > 0 ? request.getYear() : LocalDate.now().getYear();
        String category = request.getCategory().trim();

        Optional<Budget> existing = budgetRepository.findByUserUsernameAndCategoryIgnoreCaseAndMonthAndYear(
                username, category, month, year
        );

        Budget budget;
        if (existing.isPresent()) {
            budget = existing.get();
            budget.setMonthlyLimit(request.getMonthlyLimit());
        } else {
            budget = Budget.builder()
                    .user(user)
                    .category(category)
                    .monthlyLimit(request.getMonthlyLimit())
                    .month(month)
                    .year(year)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        Budget saved = budgetRepository.save(budget);
        return toBudgetResponse(saved, username);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(String username, Integer month, Integer year) {
        int m = (month != null && month >= 1 && month <= 12) ? month : LocalDate.now().getMonthValue();
        int y = (year != null && year >= 2000) ? year : LocalDate.now().getYear();

        List<Budget> budgets = budgetRepository.findByUserUsernameAndMonthAndYear(username, m, y);
        return budgets.stream()
                .map(b -> toBudgetResponse(b, username))
                .collect(Collectors.toList());
    }

    public void deleteBudget(Long id, String username) {
        Budget budget = budgetRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found or unauthorized"));
        budgetRepository.delete(budget);
    }

    private BudgetResponse toBudgetResponse(Budget budget, String username) {
        YearMonth ym = YearMonth.of(budget.getYear(), budget.getMonth());
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        Double spentVal = transactionRepository.sumExpenseByCategoryAndDateBetween(
                username, budget.getCategory(), startDate, endDate
        );
        double spent = spentVal != null ? spentVal : 0.0;
        double remaining = Math.max(0.0, budget.getMonthlyLimit() - spent);
        double percentage = budget.getMonthlyLimit() > 0 ? (spent / budget.getMonthlyLimit()) * 100.0 : 0.0;

        String status = "NORMAL";
        if (percentage >= 100.0) {
            status = "EXCEEDED";
        } else if (percentage >= 80.0) {
            status = "WARNING";
        }

        return BudgetResponse.builder()
                .id(budget.getId())
                .category(budget.getCategory())
                .monthlyLimit(budget.getMonthlyLimit())
                .month(budget.getMonth())
                .year(budget.getYear())
                .spent(Math.round(spent * 100.0) / 100.0)
                .remaining(Math.round(remaining * 100.0) / 100.0)
                .percentageUsed(Math.round(percentage * 10.0) / 10.0)
                .status(status)
                .build();
    }
}
