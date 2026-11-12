package com.example.Expense.Tracker.service;

import com.example.Expense.Tracker.dto.transaction.TransactionRequest;
import com.example.Expense.Tracker.dto.transaction.TransactionResponse;
import com.example.Expense.Tracker.exception.BadRequestException;
import com.example.Expense.Tracker.exception.ResourceNotFoundException;
import com.example.Expense.Tracker.model.Transaction;
import com.example.Expense.Tracker.model.TransactionType;
import com.example.Expense.Tracker.model.User;
import com.example.Expense.Tracker.repository.TransactionRepository;
import com.example.Expense.Tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Transaction addTransaction(Transaction transaction, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        if (transaction.getDescription() == null || transaction.getDescription().isBlank()) {
            if (transaction.getText() != null && !transaction.getText().isBlank()) {
                transaction.setDescription(transaction.getText());
            } else {
                transaction.setDescription("Transaction");
            }
        }

        if (transaction.getType() == null) {
            if (transaction.getAmount() < 0) {
                transaction.setType(TransactionType.EXPENSE);
                transaction.setAmount(Math.abs(transaction.getAmount()));
            } else {
                transaction.setType(TransactionType.INCOME);
            }
        } else {
            transaction.setAmount(Math.abs(transaction.getAmount()));
        }

        if (transaction.getCategory() == null || transaction.getCategory().isBlank()) {
            transaction.setCategory("General");
        }
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDate.now());
        }
        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(LocalDateTime.now());
        }

        transaction.setUser(user);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions(String username) {
        return transactionRepository.findByUserUsernameOrderByDateDescIdDesc(username);
    }

    @Override
    public void deleteTransaction(Long id, String username) {
        Transaction transaction = transactionRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or does not belong to user: " + username));

        transactionRepository.delete(transaction);
    }

    @Override
    public TransactionResponse createTransaction(TransactionRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        if (request.getAmount() == null || request.getAmount() == 0) {
            throw new BadRequestException("Transaction amount must be non-zero");
        }

        TransactionType type = request.getType();
        double amount = Math.abs(request.getAmount());

        if (type == null) {
            if (request.getAmount() < 0) {
                type = TransactionType.EXPENSE;
            } else {
                type = TransactionType.INCOME;
            }
        }

        String category = request.getCategory() != null && !request.getCategory().isBlank()
                ? request.getCategory().trim()
                : (type == TransactionType.INCOME ? "Salary & Income" : "General Expense");

        LocalDate date = request.getDate() != null ? request.getDate() : LocalDate.now();

        Transaction transaction = Transaction.builder()
                .description(request.resolveDescription())
                .amount(amount)
                .type(type)
                .category(category)
                .date(date)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(saved);
    }

    @Override
    public TransactionResponse updateTransaction(Long id, TransactionRequest request, String username) {
        Transaction transaction = transactionRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or does not belong to user: " + username));

        if (request.getAmount() != null) {
            if (request.getAmount() == 0) {
                throw new BadRequestException("Amount cannot be zero");
            }
            transaction.setAmount(Math.abs(request.getAmount()));
        }

        if (request.getType() != null) {
            transaction.setType(request.getType());
        }

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            transaction.setDescription(request.getDescription().trim());
        } else if (request.getText() != null && !request.getText().isBlank()) {
            transaction.setDescription(request.getText().trim());
        }

        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            transaction.setCategory(request.getCategory().trim());
        }

        if (request.getDate() != null) {
            transaction.setDate(request.getDate());
        }

        Transaction updated = transactionRepository.save(transaction);
        return TransactionResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id, String username) {
        Transaction transaction = transactionRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or does not belong to user: " + username));
        return TransactionResponse.fromEntity(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(String username, TransactionType type, String category,
                                                     LocalDate startDate, LocalDate endDate, String keyword) {
        // Ensure user exists
        if (!userRepository.existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        List<Transaction> transactions = transactionRepository.searchTransactions(
                username, type, category, startDate, endDate, keyword != null ? keyword.trim() : null
        );

        return transactions.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
