package com.tafhdev.split_bill_app.shared.persistence.repository;

import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;
import com.tafhdev.split_bill_app.shared.persistence.entity.IdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyJpaRepository extends JpaRepository<IdempotencyEntity, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO idempotencies (
                id,
                scope,
                idempotency_key,
                request_hash,
                created_at
            )
            VALUES (
                :id,
                :scope,
                :idempotencyKey,
                :requestHash,
                :createdAt
            )
            ON CONFLICT (scope, idempotency_key)
            DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("scope") String scope,
            @Param("idempotencyKey") String idempotencyKey,
            @Param("requestHash") String requestHash,
            @Param("createdAt") Instant createdAt
    );

    Optional<IdempotencyEntity> findByScopeAndIdempotencyKey(
            IdempotencyScope scope,
            String idempotencyKey
    );
}
