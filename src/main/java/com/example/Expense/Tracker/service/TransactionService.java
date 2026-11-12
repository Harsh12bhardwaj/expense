package com.example.Expense.Tracker.service;

import com.example.Expense.Tracker.dto.transaction.TransactionRequest;
import com.example.Expense.Tracker.dto.transaction.TransactionResponse;
import com.example.Expense.Tracker.model.Transaction;
import com.example.Expense.Tracker.model.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    // Backward compatible methods
    Transaction addTransaction(Transaction transaction, String username);
    List<Transaction> getAllTransactions(String username);
    void deleteTransaction(Long id, String username);

    // Enhanced methods
    TransactionResponse createTransaction(TransactionRequest request, String username);
    TransactionResponse updateTransaction(Long id, TransactionRequest request, String username);
    TransactionResponse getTransactionById(Long id, String username);
    List<TransactionResponse> getTransactions(String username, TransactionType type, String category, LocalDate startDate, LocalDate endDate, String keyword);
}
