package com.tafhdev.split_bill_app.shared.persistence.mapper;

import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.persistence.entity.IdempotencyEntity;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyMapper {

    public Idempotency toDomain(IdempotencyEntity entity) {
        return Idempotency.reconstitute(
                entity.getId(),
                entity.getScope(),
                entity.getIdempotencyKey(),
                entity.getRequestHash(),
                entity.getResponseCode(),
                entity.getResponseBody(),
                entity.getCreatedAt()
        );
    }

    public IdempotencyEntity toEntity(Idempotency domain) {
        return new IdempotencyEntity(
                domain.getId(),
                domain.getScope(),
                domain.getIdempotencyKey(),
                domain.getRequestHash(),
                domain.getResponseCode(),
                domain.getResponseBody(),
                domain.getCreatedAt()
        );
    }
}
