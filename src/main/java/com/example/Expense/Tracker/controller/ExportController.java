package com.example.Expense.Tracker.controller;

import com.example.Expense.Tracker.dto.transaction.TransactionResponse;
import com.example.Expense.Tracker.model.TransactionType;
import com.example.Expense.Tracker.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/ExpTrack/export")
@CrossOrigin
public class ExportController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping(value = "/csv/{username}", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable String username,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword) {

        List<TransactionResponse> list = transactionService.getTransactions(
                username, type, category, startDate, endDate, keyword
        );

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Date,Description,Type,Category,Amount\n");

        for (TransactionResponse t : list) {
            String sanitizedDesc = t.getDescription() != null ? t.getDescription().replace("\"", "\"\"") : "";
            String sanitizedCat = t.getCategory() != null ? t.getCategory().replace("\"", "\"\"") : "";
            sb.append(t.getId()).append(",")
              .append(t.getDate()).append(",")
              .append("\"").append(sanitizedDesc).append("\",")
              .append(t.getType()).append(",")
              .append("\"").append(sanitizedCat).append("\",")
              .append(String.format("%.2f", t.getAmount()))
              .append("\n");
        }

        byte[] csvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        String filename = "expense_tracker_" + username + "_" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }
}
