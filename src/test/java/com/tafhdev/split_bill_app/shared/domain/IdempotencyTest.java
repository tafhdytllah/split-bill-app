package com.tafhdev.split_bill_app.shared.domain;

import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String SCOPE = "PAYMENT";
    private static final String IDEMPOTENCY_KEY = "idem-123";
    private static final String REQUEST_HASH = "a".repeat(64);
    private static final Integer RESPONSE_CODE = 201;
    private static final String RESPONSE_BODY = """
            {"id":"payment-123","status":"SUCCESS"}
            """;
    private static final Instant CREATED_AT = Instant.now();

    @Test
    void shouldCreateValidIdempotency() {
        Idempotency idempotency = Idempotency.createNew(
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
        assertThat(idempotency.getIdempotencyKey()).isEqualTo(IDEMPOTENCY_KEY);
        assertThat(idempotency.getRequestHash()).isEqualTo(REQUEST_HASH);
        assertThat(idempotency.getResponseCode()).isEqualTo(RESPONSE_CODE);
        assertThat(idempotency.getResponseBody()).isEqualTo(RESPONSE_BODY);
        assertThat(idempotency.getCreatedAt()).isEqualTo(CREATED_AT);
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
        assertThat(idempotency.getIdempotencyKey()).isEqualTo(IDEMPOTENCY_KEY);
        assertThat(idempotency.getRequestHash()).isEqualTo(REQUEST_HASH);
        assertThat(idempotency.getResponseCode()).isEqualTo(RESPONSE_CODE);
        assertThat(idempotency.getResponseBody()).isEqualTo(RESPONSE_BODY);
        assertThat(idempotency.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void shouldRejectNullId() {
        assertThatThrownBy(() ->
                Idempotency.createNew(
                        null,
                        SCOPE,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        RESPONSE_CODE,
                        RESPONSE_BODY,
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("id");
    }

    @Test
    void shouldRejectBlankScope() {
        assertThatThrownBy(() ->
                Idempotency.createNew(
                        ID,
                        " ",
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        RESPONSE_CODE,
                        RESPONSE_BODY,
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("scope");
    }

    @Test
    void shouldRejectBlankIdempotencyKey() {
        assertThatThrownBy(() ->
                Idempotency.createNew(
                        ID,
                        SCOPE,
                        " ",
                        REQUEST_HASH,
                        RESPONSE_CODE,
                        RESPONSE_BODY,
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("idempotency key");
    }

    @Test
    void shouldRejectBlankRequestHash() {
        assertThatThrownBy(() ->
                Idempotency.createNew(
                        ID,
                        SCOPE,
                        IDEMPOTENCY_KEY,
                        " ",
                        RESPONSE_CODE,
                        RESPONSE_BODY,
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("request hash");
    }

    @Test
    void shouldRejectNullResponseCode() {
        assertThatThrownBy(() ->
                Idempotency.createNew(
                        ID,
                        SCOPE,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        null,
                        RESPONSE_BODY,
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("response code");
    }

    @Test
    void shouldRejectBlankResponseBody() {
        assertThatThrownBy(() ->
                Idempotency.createNew(
                        ID,
                        SCOPE,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        RESPONSE_CODE,
                        " ",
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("response body");
    }

    @Test
    void shouldRejectNullCreatedAt() {
        assertThatThrownBy(() ->
                Idempotency.createNew(
                        ID,
                        SCOPE,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH,
                        RESPONSE_CODE,
                        RESPONSE_BODY,
                        null
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("created at");
    }
}