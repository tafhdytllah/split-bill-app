package com.tafhdev.split_bill_app.group.persistence.repository;

import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BillGroupJpaRepository extends JpaRepository<BillGroupEntity, UUID> {

    @Query("""
        SELECT DISTINCT g
        FROM BillGroupEntity g
        LEFT JOIN FETCH g.participants
        WHERE g.id = :groupId
    """)
    Optional<BillGroupEntity> findByIdWithParticipants(
            @Param("groupId") UUID groupId
    );
}
