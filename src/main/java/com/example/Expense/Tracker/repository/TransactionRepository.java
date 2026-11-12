package com.example.Expense.Tracker.repository;

import com.example.Expense.Tracker.model.Transaction;
import com.example.Expense.Tracker.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserUsername(String username);

    List<Transaction> findByUserUsernameOrderByDateDescIdDesc(String username);

    Optional<Transaction> findByIdAndUserUsername(Long id, String username);

    @Query("SELECT t FROM Transaction t WHERE t.user.username = :username " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:category IS NULL OR :category = '' OR LOWER(t.category) = LOWER(:category)) " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.date DESC, t.id DESC")
    List<Transaction> searchTransactions(
            @Param("username") String username,
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("keyword") String keyword
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.user.username = :username AND t.type = :type")
    Double sumAmountByUserAndType(@Param("username") String username, @Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.user.username = :username AND t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    Double sumAmountByUserAndTypeAndDateBetween(
            @Param("username") String username,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.user.username = :username AND t.type = 'EXPENSE' AND LOWER(t.category) = LOWER(:category) AND t.date BETWEEN :startDate AND :endDate")
    Double sumExpenseByCategoryAndDateBetween(
            @Param("username") String username,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0.0) FROM Transaction t " +
           "WHERE t.user.username = :username AND t.type = 'EXPENSE' " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> getCategoryExpenseBreakdown(
            @Param("username") String username,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT t FROM Transaction t WHERE t.user.username = :username ORDER BY t.date DESC, t.id DESC")
    List<Transaction> findRecentTransactions(@Param("username") String username);
}
