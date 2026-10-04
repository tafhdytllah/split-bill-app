package com.tafhdev.split_bill_app.shared.persistence.repository;

import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;
import com.tafhdev.split_bill_app.shared.persistence.entity.IdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyJpaRepository extends JpaRepository<IdempotencyEntity, UUID> {

    Optional<IdempotencyEntity> findByScopeAndIdempotencyKey(
            IdempotencyScope scope,
            String idempotencyKey
    );
}
