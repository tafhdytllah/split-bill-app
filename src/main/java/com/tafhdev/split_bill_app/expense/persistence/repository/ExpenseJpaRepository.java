package com.tafhdev.split_bill_app.expense.persistence.repository;

import com.tafhdev.split_bill_app.expense.persistence.entity.ExpenseEntity;
import com.tafhdev.split_bill_app.expense.persistence.projection.ExpenseProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseEntity, UUID> {

    @Query(value = """
        SELECT
            e.id AS expenseId,
            e.group_id AS groupId,
            e.paid_by AS paidBy,
            e.amount AS amount,
            e.category AS category,
            e.split_type AS splitType,
            e.created_at AS createdAt,
            s.id AS splitId,
            s.participant_id AS participantId,
            s.amount AS splitAmount
        FROM expenses e
        LEFT JOIN expense_splits s
            ON e.id = s.expense_id
        WHERE e.group_id = :groupId
        ORDER BY e.id, s.id
        """,
            nativeQuery = true)
    List<ExpenseProjection> findProjectionByGroupId(
            @Param("groupId") UUID groupId
    );

    @EntityGraph(attributePaths = "splits")
    List<ExpenseEntity> findByGroup_id(UUID groupId);

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
