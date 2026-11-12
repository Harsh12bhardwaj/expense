package com.example.Expense.Tracker;

import com.example.Expense.Tracker.dto.analytics.DashboardSummaryResponse;
import com.example.Expense.Tracker.dto.auth.AuthResponse;
import com.example.Expense.Tracker.dto.auth.LoginRequest;
import com.example.Expense.Tracker.dto.auth.RegisterRequest;
import com.example.Expense.Tracker.dto.budget.BudgetRequest;
import com.example.Expense.Tracker.dto.budget.BudgetResponse;
import com.example.Expense.Tracker.dto.transaction.TransactionRequest;
import com.example.Expense.Tracker.dto.transaction.TransactionResponse;
import com.example.Expense.Tracker.exception.BadRequestException;
import com.example.Expense.Tracker.exception.UnauthorizedException;
import com.example.Expense.Tracker.model.TransactionType;
import com.example.Expense.Tracker.service.AnalyticsService;
import com.example.Expense.Tracker.service.BudgetService;
import com.example.Expense.Tracker.service.TransactionService;
import com.example.Expense.Tracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class ExpenseTrackerApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private AnalyticsService analyticsService;

    private String testUser = "john_doe";

    @BeforeEach
    void setUp() {
        // Register test user
        RegisterRequest registerReq = RegisterRequest.builder()
                .name("John Doe")
                .username(testUser)
                .email("john@example.com")
                .password("securePassword123")
                .build();

        userService.register(registerReq);
    }

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
        assertNotNull(userService);
        assertNotNull(transactionService);
        assertNotNull(budgetService);
        assertNotNull(analyticsService);
    }

    @Test
    @DisplayName("User login succeeds with correct credentials and fails with invalid")
    void testUserAuth() {
        LoginRequest validLogin = LoginRequest.builder()
                .username(testUser)
                .password("securePassword123")
                .build();
        AuthResponse auth = userService.login(validLogin);
        assertNotNull(auth);
        assertEquals(testUser, auth.getUsername());
        assertNotNull(auth.getToken());

        LoginRequest invalidLogin = LoginRequest.builder()
                .username(testUser)
                .password("wrongPassword")
                .build();
        assertThrows(UnauthorizedException.class, () -> userService.login(invalidLogin));
    }

    @Test
    @DisplayName("Duplicate registration is rejected")
    void testDuplicateRegistration() {
        RegisterRequest duplicateReq = RegisterRequest.builder()
                .name("Duplicate User")
                .username(testUser)
                .email("another@example.com")
                .password("password123")
                .build();

        assertThrows(BadRequestException.class, () -> userService.register(duplicateReq));
    }

    @Test
    @DisplayName("Transaction lifecycle: Create, Edit, Filter, Delete")
    void testTransactionLifecycle() {
        // 1. Add Income
        TransactionRequest incomeReq = TransactionRequest.builder()
                .description("Monthly Salary")
                .amount(5000.0)
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.now())
                .build();
        TransactionResponse income = transactionService.createTransaction(incomeReq, testUser);
        assertNotNull(income.getId());
        assertEquals(5000.0, income.getAmount());
        assertEquals(TransactionType.INCOME, income.getType());

        // 2. Add Expense
        TransactionRequest expenseReq = TransactionRequest.builder()
                .description("Grocery Store")
                .amount(300.0)
                .type(TransactionType.EXPENSE)
                .category("Food & Dining")
                .date(LocalDate.now())
                .build();
        TransactionResponse expense = transactionService.createTransaction(expenseReq, testUser);
        assertNotNull(expense.getId());
        assertEquals(300.0, expense.getAmount());
        assertEquals(TransactionType.EXPENSE, expense.getType());

        // 3. Edit Expense
        TransactionRequest updateReq = TransactionRequest.builder()
                .description("Supermarket Groceries")
                .amount(350.0)
                .category("Food & Dining")
                .type(TransactionType.EXPENSE)
                .date(LocalDate.now())
                .build();
        TransactionResponse updated = transactionService.updateTransaction(expense.getId(), updateReq, testUser);
        assertEquals("Supermarket Groceries", updated.getDescription());
        assertEquals(350.0, updated.getAmount());

        // 4. Filter Transactions
        List<TransactionResponse> expensesOnly = transactionService.getTransactions(
                testUser, TransactionType.EXPENSE, null, null, null, null
        );
        assertEquals(1, expensesOnly.size());
        assertEquals("Supermarket Groceries", expensesOnly.get(0).getDescription());

        // 5. Delete Transaction
        transactionService.deleteTransaction(updated.getId(), testUser);
        List<TransactionResponse> remaining = transactionService.getTransactions(testUser, null, null, null, null, null);
        assertEquals(1, remaining.size());
        assertEquals("Monthly Salary", remaining.get(0).getDescription());
    }

    @Test
    @DisplayName("Dashboard and Financial Calculations")
    void testFinancialCalculations() {
        // Income = 6000
        transactionService.createTransaction(TransactionRequest.builder()
                .description("Consulting")
                .amount(6000.0)
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.now())
                .build(), testUser);

        // Expense = 2000
        transactionService.createTransaction(TransactionRequest.builder()
                .description("Rent")
                .amount(2000.0)
                .type(TransactionType.EXPENSE)
                .category("Housing")
                .date(LocalDate.now())
                .build(), testUser);

        // Expense = 500
        transactionService.createTransaction(TransactionRequest.builder()
                .description("Utilities")
                .amount(500.0)
                .type(TransactionType.EXPENSE)
                .category("Utilities")
                .date(LocalDate.now())
                .build(), testUser);

        DashboardSummaryResponse summary = analyticsService.getDashboardSummary(testUser);
        assertEquals(6000.0, summary.getTotalIncome());
        assertEquals(2500.0, summary.getTotalExpense());
        assertEquals(3500.0, summary.getTotalBalance()); // Balance = Income - Expense
        assertTrue(summary.getSavingsRate() > 0);
        assertEquals(3, summary.getTotalTransactionCount());
    }

    @Test
    @DisplayName("Budget management and alert calculations")
    void testBudgetManagement() {
        LocalDate now = LocalDate.now();

        // Set budget of $400 for Dining
        BudgetRequest budgetReq = BudgetRequest.builder()
                .category("Dining")
                .monthlyLimit(400.0)
                .month(now.getMonthValue())
                .year(now.getYear())
                .build();
        BudgetResponse budget = budgetService.setBudget(testUser, budgetReq);
        assertEquals(400.0, budget.getMonthlyLimit());

        // Add expense of $350 in Dining (utilization: 87.5% -> WARNING status)
        transactionService.createTransaction(TransactionRequest.builder()
                .description("Dinner with friends")
                .amount(350.0)
                .type(TransactionType.EXPENSE)
                .category("Dining")
                .date(now)
                .build(), testUser);

        List<BudgetResponse> budgets = budgetService.getBudgets(testUser, now.getMonthValue(), now.getYear());
        assertFalse(budgets.isEmpty());
        BudgetResponse diningBudget = budgets.get(0);
        assertEquals(350.0, diningBudget.getSpent());
        assertEquals(50.0, diningBudget.getRemaining());
        assertEquals("WARNING", diningBudget.getStatus());
    }
}
