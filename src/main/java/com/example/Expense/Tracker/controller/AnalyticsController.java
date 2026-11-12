package com.example.Expense.Tracker.controller;

import com.example.Expense.Tracker.dto.analytics.AnalyticsResponse;
import com.example.Expense.Tracker.dto.analytics.CategoryExpenseDto;
import com.example.Expense.Tracker.dto.analytics.DashboardSummaryResponse;
import com.example.Expense.Tracker.dto.common.ApiResponse;
import com.example.Expense.Tracker.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/ExpTrack/analytics")
@CrossOrigin
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/dashboard/{username}")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary(@PathVariable String username) {
        DashboardSummaryResponse summary = analyticsService.getDashboardSummary(username);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/reports/{username}")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getReports(
            @PathVariable String username,
            @RequestParam(required = false, defaultValue = "6") Integer months) {
        AnalyticsResponse response = analyticsService.getAnalytics(username, months);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/categories/{username}")
    public ResponseEntity<ApiResponse<List<CategoryExpenseDto>>> getCategoryBreakdown(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CategoryExpenseDto> list = analyticsService.getCategoryBreakdown(username, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
