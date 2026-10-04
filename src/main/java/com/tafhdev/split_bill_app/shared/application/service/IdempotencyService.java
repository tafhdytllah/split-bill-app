package com.tafhdev.split_bill_app.shared.application.service;

import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import com.tafhdev.split_bill_app.shared.repository.IdempotencyRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;
    private final IdGenerator idGenerator;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public IdempotencyService(
            IdempotencyRepository idempotencyRepository,
            IdGenerator idGenerator,
            Clock clock,
            ObjectMapper objectMapper
    ) {
        this.idempotencyRepository = idempotencyRepository;
        this.idGenerator = idGenerator;
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    public Optional<Idempotency> find(
            IdempotencyScope scope,
            String idempotencyKey
    ) {
        return idempotencyRepository.findByScopeAndKey(
                scope,
                idempotencyKey
        );
    }

    public Idempotency create(
            IdempotencyScope scope,
            String idempotencyKey,
            String requestHash
    ) {
        Idempotency idempotency = Idempotency.createNew(
                idGenerator.generate(),
                scope,
                idempotencyKey,
                requestHash,
                Instant.now(clock)
        );

        return idempotencyRepository.save(idempotency);
    }

    public void complete(
            Idempotency idempotency,
            int responseCode,
            Object response
    ) {
        try {

            String responseBody = objectMapper.writeValueAsString(response);

            idempotency.complete(
                    responseCode,
                    responseBody
            );

            idempotencyRepository.save(idempotency);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "failed to serialize idempotency response",
                    e
            );
        }

    }

    public <T> T getResponse(
            Idempotency idempotency,
            Class<T> responseType
    ) {
        try {

            return objectMapper.readValue(
                    idempotency.getResponseBody(),
                    responseType
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "failed to deserialize idempotency response",
                    e
            );
        }
    }
}
