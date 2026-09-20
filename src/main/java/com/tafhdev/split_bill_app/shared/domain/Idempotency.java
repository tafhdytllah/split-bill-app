package com.tafhdev.split_bill_app.shared.domain;

import com.tafhdev.split_bill_app.shared.domain.exception.Guard;

import java.time.Instant;
import java.util.UUID;

public class Idempotency {

    private final UUID id;
    private final String scope;
    private final String idempotencyKey;
    private final String requestHash;
    private final Integer responseCode;
    private final String responseBody;
    private final Instant createdAt;

    public Idempotency(
            UUID id,
            String scope,
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
            String scope,
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
                responseCode,
                responseBody,
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

    public static Idempotency reconstitute(
            UUID id,
            String scope,
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
                responseCode,
                responseBody,
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

    private static void validateInvariants(
            UUID id,
            String scope,
            String idempotencyKey,
            String requestHash,
            Integer responseCode,
            String responseBody,
            Instant createdAt
    ) {
        Guard.requireNotNull(id, "id");
        Guard.requireNonBlank(scope, "scope");
        Guard.requireNonBlank(idempotencyKey, "idempotency key");
        Guard.requireNonBlank(requestHash, "request hash");
        Guard.requireNotNull(responseCode, "response code");
        Guard.requireNonBlank(responseBody, "response body");
        Guard.requireNotNull(createdAt, "created at");
    }

    public UUID getId() {
        return id;
    }

    public String getScope() {
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
