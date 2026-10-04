package com.tafhdev.split_bill_app.shared.domain;

import com.tafhdev.split_bill_app.shared.domain.exception.Guard;

import java.time.Instant;
import java.util.UUID;

public class Idempotency {

    private final UUID id;
    private final IdempotencyScope scope;
    private final String idempotencyKey;
    private final String requestHash;

    private Integer responseCode;
    private String responseBody;

    private final Instant createdAt;

    private Idempotency(
            UUID id,
            IdempotencyScope scope,
            String idempotencyKey,
            String requestHash,
            Integer responseCode,
            String responseBody,
            Instant createdAt
    ) {
        this.id = id;
        this.scope = scope;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.responseCode = responseCode;
        this.responseBody = responseBody;
        this.createdAt = createdAt;
    }

    public static Idempotency createNew(
            UUID id,
            IdempotencyScope scope,
            String idempotencyKey,
            String requestHash,
            Instant createdAt
    ) {
        validateInvariants(
                id,
                scope,
                idempotencyKey,
                requestHash,
                createdAt
        );

        return new Idempotency(
                id,
                scope,
                idempotencyKey,
                requestHash,
                null,
                null,
                createdAt
        );
    }

    public static Idempotency reconstitute(
            UUID id,
            IdempotencyScope scope,
            String idempotencyKey,
            String requestHash,
            Integer responseCode,
            String responseBody,
            Instant createdAt
    ) {
        validateInvariants(
                id,
                scope,
                idempotencyKey,
                requestHash,
                createdAt
        );

        return new Idempotency(
                id,
                scope,
                idempotencyKey,
                requestHash,
                responseCode,
                responseBody,
                createdAt
        );
    }

    public void complete(
            int responseCode,
            String responseBody
    ) {
        Guard.requireNotNull(responseCode, "response code");
        Guard.requireNonBlank(responseBody, "response body");

        this.responseCode = responseCode;
        this.responseBody = responseBody;
    }

    private static void validateInvariants(
            UUID id,
            IdempotencyScope scope,
            String idempotencyKey,
            String requestHash,
            Instant createdAt
    ) {
        Guard.requireNotNull(id, "id");
        Guard.requireNotNull(scope, "scope");
        Guard.requireNonBlank(idempotencyKey, "idempotency key");
        Guard.requireNonBlank(requestHash, "request hash");
        Guard.requireNotNull(createdAt, "created at");
    }

    public UUID getId() {
        return id;
    }

    public IdempotencyScope getScope() {
        return scope;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
