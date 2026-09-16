package com.tafhdev.split_bill_app.expense.persistence.repository;

import com.tafhdev.split_bill_app.expense.persistence.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseEntity, UUID> {

    @Query("""
        SELECT DISTINCT e
        FROM ExpenseEntity e
        LEFT JOIN FETCH e.splits s
        LEFT JOIN FETCH s.participant
        WHERE e.id = :expenseId
    """)
    Optional<ExpenseEntity> findByIdWithSplits(
            @Param("expenseId") UUID expenseId
    );

}
