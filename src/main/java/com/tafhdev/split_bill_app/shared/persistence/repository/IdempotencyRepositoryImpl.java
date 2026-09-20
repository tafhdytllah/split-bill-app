package com.tafhdev.split_bill_app.shared.persistence.repository;

import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.persistence.entity.IdempotencyEntity;
import com.tafhdev.split_bill_app.shared.persistence.mapper.IdempotencyMapper;
import com.tafhdev.split_bill_app.shared.repository.IdempotencyRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class IdempotencyRepositoryImpl implements IdempotencyRepository {

    private final IdempotencyJpaRepository idempotencyJpaRepository;
    private final IdempotencyMapper idempotencyMapper;

    public IdempotencyRepositoryImpl(IdempotencyJpaRepository idempotencyJpaRepository, IdempotencyMapper idempotencyMapper) {
        this.idempotencyJpaRepository = idempotencyJpaRepository;
        this.idempotencyMapper = idempotencyMapper;
    }

    @Override
    public Idempotency save(Idempotency idempotency) {
        IdempotencyEntity idempotencyEntity = idempotencyMapper.toEntity(idempotency);

        return idempotencyMapper.toDomain(idempotencyEntity);
    }

    @Override
    public Optional<Idempotency> findByScopeAndKey(
            String scope,
            String idempotencyKey
    ) {
        return idempotencyJpaRepository.findByScopeAndIdempotencyKey(scope, idempotencyKey)
                .map(idempotencyMapper::toDomain);
    }
}
