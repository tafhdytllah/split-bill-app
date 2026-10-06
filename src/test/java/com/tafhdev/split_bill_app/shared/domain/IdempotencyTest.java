package com.tafhdev.split_bill_app.shared.domain;

import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyTest {

    private static final UUID ID = UUID.randomUUID();
    private static final IdempotencyScope SCOPE = IdempotencyScope.PAYMENT;
    private static final String IDEMPOTENCY_KEY = "idem-123";
    private static final String REQUEST_HASH = "a".repeat(64);
    private static final Integer RESPONSE_CODE = 201;
    private static final String RESPONSE_BODY = """
            {"id":"payment-123","status":"SUCCESS"}
            """;
    private static final Instant CREATED_AT =
            Instant.parse("2026-10-07T10:00:00Z");

    @Test
    void shouldCreateValidIdempotency() {

        Idempotency idempotency = Idempotency.createNew(
                ID,
                SCOPE,
                IDEMPOTENCY_KEY,
                REQUEST_HASH,
                CREATED_AT
        );

        assertThat(idempotency.getId()).isEqualTo(ID);
        assertThat(idempotency.getScope()).isEqualTo(SCOPE);
        assertThat(idempotency.getIdempotencyKey())
                .isEqualTo(IDEMPOTENCY_KEY);
        assertThat(idempotency.getRequestHash())
                .isEqualTo(REQUEST_HASH);
        assertThat(idempotency.getResponseCode())
                .isNull();
        assertThat(idempotency.getResponseBody())
                .isNull();
        assertThat(idempotency.getCreatedAt())
                .isEqualTo(CREATED_AT);
    }

    @Test
    void shouldCompleteIdempotency() {

        Idempotency idempotency = Idempotency.createNew(
                ID,
                SCOPE,
                IDEMPOTENCY_KEY,
                REQUEST_HASH,
                CREATED_AT
        );

        idempotency.complete(
                RESPONSE_CODE,
                RESPONSE_BODY
        );

        assertThat(idempotency.getResponseCode())
                .isEqualTo(RESPONSE_CODE);

        assertThat(idempotency.getResponseBody())
                .isEqualTo(RESPONSE_BODY);
    }

    @Test
    void shouldReconstituteValidIdempotency() {

        Idempotency idempotency = Idempotency.reconstitute(
                ID,
                SCOPE,
                IDEMPOTENCY_KEY,
                REQUEST_HASH,
                RESPONSE_CODE,
                RESPONSE_BODY,
                CREATED_AT
        );

        assertThat(idempotency.getId()).isEqualTo(ID);
        assertThat(idempotency.getScope()).isEqualTo(SCOPE);
        assertThat(idempotency.getIdempotencyKey())
                .isEqualTo(IDEMPOTENCY_KEY);
        assertThat(idempotency.getRequestHash())
                .isEqualTo(REQUEST_HASH);
        assertThat(idempotency.getResponseCode())
                .isEqualTo(RESPONSE_CODE);
        assertThat(idempotency.getResponseBody())
                .isEqualTo(RESPONSE_BODY);
        assertThat(idempotency.getCreatedAt())
                .isEqualTo(CREATED_AT);
    }

    @Test
    void shouldRejectNullId() {

        assertThatThrownBy(() ->
                Idempotency.createNew(
                        null,
                        SCOPE,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("id must not be null");
    }

    @Test
    void shouldRejectNullScope() {

        assertThatThrownBy(() ->
                Idempotency.createNew(
                        ID,
                        null,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("scope must not be null");
    }

    @Test
    void shouldRejectBlankIdempotencyKey() {

        assertThatThrownBy(() ->
                Idempotency.createNew(
                        ID,
                        SCOPE,
                        " ",
                        REQUEST_HASH,
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("idempotency key must not be blank");
    }

    @Test
    void shouldRejectBlankRequestHash() {

        assertThatThrownBy(() ->
                Idempotency.createNew(
                        ID,
                        SCOPE,
                        IDEMPOTENCY_KEY,
                        " ",
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("request hash must not be blank");
    }

    @Test
    void shouldRejectNullCreatedAt() {

        assertThatThrownBy(() ->
                Idempotency.createNew(
                        ID,
                        SCOPE,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        null
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("created at must not be null");
    }

    @Test
    void shouldRejectBlankResponseBodyWhenCompleting() {

        Idempotency idempotency = Idempotency.createNew(
                ID,
                SCOPE,
                IDEMPOTENCY_KEY,
                REQUEST_HASH,
                CREATED_AT
        );

        assertThatThrownBy(() ->
                idempotency.complete(
                        RESPONSE_CODE,
                        " "
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("response body must not be blank");
    }
}