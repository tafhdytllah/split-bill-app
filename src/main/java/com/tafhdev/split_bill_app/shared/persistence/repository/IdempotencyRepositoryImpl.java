package com.tafhdev.split_bill_app.shared.persistence.repository;

import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;
import com.tafhdev.split_bill_app.shared.persistence.entity.IdempotencyEntity;
import com.tafhdev.split_bill_app.shared.persistence.mapper.IdempotencyMapper;
import com.tafhdev.split_bill_app.shared.repository.IdempotencyRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class IdempotencyRepositoryImpl implements IdempotencyRepository {

    private final IdempotencyJpaRepository idempotencyJpaRepository;
    private final IdempotencyMapper idempotencyMapper;

    public IdempotencyRepositoryImpl(
            IdempotencyJpaRepository idempotencyJpaRepository,
            IdempotencyMapper idempotencyMapper
    ) {
        this.idempotencyJpaRepository = idempotencyJpaRepository;
        this.idempotencyMapper = idempotencyMapper;
    }

    @Override
    public Idempotency save(Idempotency idempotency) {

        IdempotencyEntity entity = idempotencyMapper.toEntity(idempotency);

        IdempotencyEntity savedEntity = idempotencyJpaRepository.save(entity);

        return idempotencyMapper.toDomain(savedEntity);
    }

    @Override
    public Idempotency saveAndFlush(Idempotency idempotency) {

        IdempotencyEntity entity = idempotencyMapper.toEntity(idempotency);

        IdempotencyEntity savedEntity = idempotencyJpaRepository.saveAndFlush(entity);

        return idempotencyMapper.toDomain(savedEntity);
    }

    @Override
    public boolean insertIfAbsent(Idempotency idempotency) {

        int inserted = idempotencyJpaRepository.insertIfAbsent(
                idempotency.getId(),
                idempotency.getScope().name(),
                idempotency.getIdempotencyKey(),
                idempotency.getRequestHash(),
                idempotency.getCreatedAt()
        );

        return inserted == 1;
    }

    @Override
    public Optional<Idempotency> findByScopeAndKey(
            IdempotencyScope scope,
            String idempotencyKey
    ) {
        return idempotencyJpaRepository.findByScopeAndIdempotencyKey(scope, idempotencyKey)
                .map(idempotencyMapper::toDomain);
    }
}
