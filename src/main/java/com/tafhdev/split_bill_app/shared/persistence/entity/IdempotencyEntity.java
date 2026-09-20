package com.tafhdev.split_bill_app.shared.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotencies")
public class IdempotencyEntity {

    @Id
    private UUID id;

    @Column(length = 100, nullable = false)
    private String scope;

    @Column(name = "idempotency_key", length = 255, nullable = false)
    private String idempotencyKey;

    @Column(name = "request_hash", length = 64, nullable = false)
    private String requestHash;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "created_at")
    private Instant createdAt;

    protected IdempotencyEntity() {
    }

    public IdempotencyEntity(
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
