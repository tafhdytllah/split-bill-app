package com.tafhdev.split_bill_app.group.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BillGroupJpaRepository extends JpaRepository<BillGroupJpaEntity, UUID> {

    @Query("""
        SELECT DISTINCT g
        FROM BillGroupJpaEntity g
        LEFT JOIN FETCH g.participants
        WHERE g.id = :groupId
    """)
    Optional<BillGroupJpaEntity> findByIdWithParticipants(
            @Param("groupId") UUID groupId
    );
}
