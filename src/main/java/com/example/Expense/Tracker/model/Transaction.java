package com.example.Expense.Tracker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transaction", indexes = {
    @Index(name = "idx_tx_user_date", columnList = "user_id, date"),
    @Index(name = "idx_tx_user_type", columnList = "user_id, type")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    // Backward-compatibility getter for 'text'
    public String getText() {
        return description;
    }

    // Backward-compatibility setter for 'text'
    public void setText(String text) {
        this.description = text;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (category == null || category.isBlank()) {
            category = "General";
        }
        if (date == null) {
            date = LocalDate.now();
        }
        // Normalize amount and type if type was not specified
        if (type == null) {
            if (amount < 0) {
                type = TransactionType.EXPENSE;
                amount = Math.abs(amount);
            } else {
                type = TransactionType.INCOME;
            }
        } else {
            amount = Math.abs(amount);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (category == null || category.isBlank()) {
            category = "General";
        }
        amount = Math.abs(amount);
    }
}
