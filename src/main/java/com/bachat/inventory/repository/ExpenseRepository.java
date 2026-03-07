package com.bachat.inventory.repository;

import com.bachat.inventory.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByExpenseDateBetween(LocalDate start, LocalDate end);

    org.springframework.data.domain.Page<Expense> findByExpenseDateBetween(
            LocalDate start, LocalDate end, org.springframework.data.domain.Pageable pageable);

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.expenseDate >= :start and e.expenseDate <= :end")
    BigDecimal sumAmountBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    List<Expense> findByOrderIdOrderByExpenseDateAsc(Long orderId);

}
