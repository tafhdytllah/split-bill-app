package com.tafhdev.split_bill_app.settlement.persistence.repository;

import com.tafhdev.split_bill_app.group.persistence.entity.ParticipantEntity;
import com.tafhdev.split_bill_app.settlement.persistence.projection.SettlementBalanceProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SettlementJpaRepository extends JpaRepository<ParticipantEntity, UUID> {

    @Query(value = """
        WITH paid AS (
            SELECT
                e.paid_by AS participant_id,
                SUM(e.amount) AS total_paid
            FROM expenses e
            WHERE e.group_id = :groupId
            GROUP BY e.paid_by
        ),

        owed AS (
            SELECT
                es.participant_id,
                SUM(es.amount) AS total_owed
            FROM expense_splits es
            JOIN expenses e
                ON e.id = es.expense_id
            WHERE e.group_id = :groupId
            GROUP BY es.participant_id
        ),

        sent AS (
            SELECT
                p.from_participant_id AS participant_id,
                SUM(p.amount) AS total_sent
            FROM payments p
            WHERE p.group_id = :groupId
            GROUP BY p.from_participant_id
        ),

        received AS (
            SELECT
                p.to_participant_id AS participant_id,
                SUM(p.amount) AS total_received
            FROM payments p
            WHERE p.group_id = :groupId
            GROUP BY p.to_participant_id
        )

        SELECT
            p.id AS participantId,
            p.name AS name,

            COALESCE(paid.total_paid, 0) AS totalPaid,
            COALESCE(owed.total_owed, 0) AS totalOwed,
            COALESCE(sent.total_sent, 0) AS totalSent,
            COALESCE(received.total_received, 0) AS totalReceived,

            (
                COALESCE(paid.total_paid, 0)
                - COALESCE(owed.total_owed, 0)
                + COALESCE(sent.total_sent, 0)
                - COALESCE(received.total_received, 0)
            ) AS balance

        FROM participants p

        LEFT JOIN paid
            ON paid.participant_id = p.id

        LEFT JOIN owed
            ON owed.participant_id = p.id

        LEFT JOIN sent
            ON sent.participant_id = p.id

        LEFT JOIN received
            ON received.participant_id = p.id

        WHERE p.group_id = :groupId

        ORDER BY p.id
        """, nativeQuery = true)
    List<SettlementBalanceProjection> findBalances(
            @Param("groupId") UUID groupId
    );
}
