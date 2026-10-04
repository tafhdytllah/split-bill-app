package com.tafhdev.split_bill_app.shared.repository;

import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;

import java.util.Optional;

public interface IdempotencyRepository {

    Idempotency save(Idempotency idempotency);

    Optional<Idempotency> findByScopeAndKey(
            IdempotencyScope scope,
            String idempotencyKey
    );
}
