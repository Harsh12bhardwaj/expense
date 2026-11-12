package com.example.Expense.Tracker.repository;

import com.example.Expense.Tracker.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserUsernameAndMonthAndYear(String username, int month, int year);

    Optional<Budget> findByUserUsernameAndCategoryIgnoreCaseAndMonthAndYear(String username, String category, int month, int year);

    Optional<Budget> findByIdAndUserUsername(Long id, String username);
}
