package com.example.Expense.Tracker.controller;

import com.example.Expense.Tracker.dto.common.ApiResponse;
import com.example.Expense.Tracker.dto.transaction.TransactionRequest;
import com.example.Expense.Tracker.dto.transaction.TransactionResponse;
import com.example.Expense.Tracker.model.TransactionType;
import com.example.Expense.Tracker.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/ExpTrack/transactions")
@CrossOrigin
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Add transaction for given username
    @PostMapping("/{username}")
    public ResponseEntity<TransactionResponse> addTransaction(
            @Valid @RequestBody TransactionRequest transactionRequest,
            @PathVariable String username) {
        TransactionResponse response = transactionService.createTransaction(transactionRequest, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all transactions (with optional filters: type, category, date range, keyword)
    @GetMapping("/{username}")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @PathVariable String username,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String search) {

        String filterKeyword = keyword != null ? keyword : search;
        List<TransactionResponse> list = transactionService.getTransactions(
                username, type, category, startDate, endDate, filterKeyword
        );
        return ResponseEntity.ok(list);
    }

    // Get single transaction by id
    @GetMapping("/{username}/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @PathVariable String username,
            @PathVariable Long id) {
        TransactionResponse response = transactionService.getTransactionById(id, username);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Update specific transaction by id and username
    @PutMapping("/{username}/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable String username,
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.updateTransaction(id, request, username);
        return ResponseEntity.ok(ApiResponse.success("Transaction updated successfully", response));
    }

    // Delete specific transaction by id and username
    @DeleteMapping("/{username}/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTransaction(
            @PathVariable String username,
            @PathVariable Long id) {
        transactionService.deleteTransaction(id, username);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully", "DELETED"));
    }
}
