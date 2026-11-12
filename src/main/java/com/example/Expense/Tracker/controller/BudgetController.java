package com.example.Expense.Tracker.controller;

import com.example.Expense.Tracker.dto.budget.BudgetRequest;
import com.example.Expense.Tracker.dto.budget.BudgetResponse;
import com.example.Expense.Tracker.dto.common.ApiResponse;
import com.example.Expense.Tracker.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ExpTrack/budgets")
@CrossOrigin
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @PostMapping("/{username}")
    public ResponseEntity<ApiResponse<BudgetResponse>> setBudget(
            @PathVariable String username,
            @Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.setBudget(username, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget set successfully", response));
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @PathVariable String username,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        List<BudgetResponse> list = budgetService.getBudgets(username, month, year);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @DeleteMapping("/{username}/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBudget(
            @PathVariable String username,
            @PathVariable Long id) {
        budgetService.deleteBudget(id, username);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully", "DELETED"));
    }
}
