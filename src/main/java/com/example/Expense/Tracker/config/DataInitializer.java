package com.example.Expense.Tracker.config;

import com.example.Expense.Tracker.model.Budget;
import com.example.Expense.Tracker.model.Transaction;
import com.example.Expense.Tracker.model.TransactionType;
import com.example.Expense.Tracker.model.User;
import com.example.Expense.Tracker.repository.BudgetRepository;
import com.example.Expense.Tracker.repository.TransactionRepository;
import com.example.Expense.Tracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsernameIgnoreCase("sarah").isEmpty()) {
            log.info("Seeding demo user 'sarah' with initial Indian finance dataset...");

            User demoUser = User.builder()
                    .name("Sarah Connor")
                    .username("sarah")
                    .email("sarah@vaultflow.in")
                    .password(passwordEncoder.encode("Password123"))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            User saved = userRepository.save(demoUser);

            LocalDate now = LocalDate.now();
            int year = now.getYear();
            int month = now.getMonthValue();

            // Seed sample Indian transactions
            List<Transaction> transactions = List.of(
                    Transaction.builder()
                            .description("Monthly Tech Salary credited via NEFT")
                            .amount(125000.0)
                            .type(TransactionType.INCOME)
                            .category("Salary")
                            .date(LocalDate.of(year, month, 1))
                            .paymentMethod("NEFT")
                            .user(saved)
                            .build(),
                    Transaction.builder()
                            .description("Freelance UI Design gig payment")
                            .amount(18000.0)
                            .type(TransactionType.INCOME)
                            .category("Freelancing")
                            .date(LocalDate.of(year, month, Math.min(now.getDayOfMonth(), 5)))
                            .paymentMethod("UPI")
                            .upiApp("Google Pay")
                            .user(saved)
                            .build(),
                    Transaction.builder()
                            .description("Apartment Rent 2BHK flat")
                            .amount(25000.0)
                            .type(TransactionType.EXPENSE)
                            .category("Rent")
                            .date(LocalDate.of(year, month, 2))
                            .paymentMethod("Net Banking")
                            .user(saved)
                            .build(),
                    Transaction.builder()
                            .description("DMart Monthly Groceries")
                            .amount(4500.0)
                            .type(TransactionType.EXPENSE)
                            .category("Groceries")
                            .date(LocalDate.of(year, month, Math.min(now.getDayOfMonth(), 4)))
                            .paymentMethod("UPI")
                            .upiApp("PhonePe")
                            .user(saved)
                            .build(),
                    Transaction.builder()
                            .description("Electricity & Water Bill")
                            .amount(2200.0)
                            .type(TransactionType.EXPENSE)
                            .category("Electricity")
                            .date(LocalDate.of(year, month, Math.min(now.getDayOfMonth(), 7)))
                            .paymentMethod("UPI")
                            .upiApp("Paytm")
                            .user(saved)
                            .build(),
                    Transaction.builder()
                            .description("Weekend Dinner at Mainland China")
                            .amount(1850.0)
                            .type(TransactionType.EXPENSE)
                            .category("Restaurants & Dining")
                            .date(LocalDate.of(year, month, Math.min(now.getDayOfMonth(), 8)))
                            .paymentMethod("Credit Card")
                            .user(saved)
                            .build(),
                    Transaction.builder()
                            .description("Nifty Index Fund SIP Auto-debit")
                            .amount(15000.0)
                            .type(TransactionType.EXPENSE)
                            .category("Investments")
                            .date(LocalDate.of(year, month, Math.min(now.getDayOfMonth(), 10)))
                            .paymentMethod("Net Banking")
                            .user(saved)
                            .build(),
                    Transaction.builder()
                            .description("Indian Oil Petrol Fuel Fill")
                            .amount(2000.0)
                            .type(TransactionType.EXPENSE)
                            .category("Fuel")
                            .date(LocalDate.of(year, month, Math.min(now.getDayOfMonth(), 12)))
                            .paymentMethod("UPI")
                            .upiApp("Google Pay")
                            .user(saved)
                            .build()
            );

            transactionRepository.saveAll(transactions);

            // Seed sample budgets
            List<Budget> budgets = List.of(
                    Budget.builder()
                            .category("Groceries")
                            .monthlyLimit(10000.0)
                            .month(month)
                            .year(year)
                            .user(saved)
                            .build(),
                    Budget.builder()
                            .category("Rent")
                            .monthlyLimit(25000.0)
                            .month(month)
                            .year(year)
                            .user(saved)
                            .build(),
                    Budget.builder()
                            .category("Restaurants & Dining")
                            .monthlyLimit(6000.0)
                            .month(month)
                            .year(year)
                            .user(saved)
                            .build(),
                    Budget.builder()
                            .category("Investments")
                            .monthlyLimit(20000.0)
                            .month(month)
                            .year(year)
                            .user(saved)
                            .build(),
                    Budget.builder()
                            .category("Fuel")
                            .monthlyLimit(5000.0)
                            .month(month)
                            .year(year)
                            .user(saved)
                            .build()
            );

            budgetRepository.saveAll(budgets);

            log.info("Demo user 'sarah' created successfully with {} transactions and {} budgets.",
                    transactions.size(), budgets.size());
        }
    }
}
