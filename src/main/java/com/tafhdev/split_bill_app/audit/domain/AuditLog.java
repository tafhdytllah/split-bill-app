package com.tafhdev.split_bill_app.audit.domain;

import com.tafhdev.split_bill_app.shared.domain.exception.Guard;

import java.time.Instant;
import java.util.UUID;

public class AuditLog {

    private final UUID id;
    private final UUID groupId;
    private final AuditAction action;
    private final AuditEntityType entityType;
    private final UUID entityId;
    private final Instant createdAt;

    private AuditLog(
            UUID id,
            UUID groupId,
            AuditAction action,
            AuditEntityType entityType,
            UUID entityId,
            Instant createdAt
    ) {
        this.id = id;
        this.groupId = groupId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.createdAt = createdAt;
    }

    public static AuditLog createNew(
            UUID id,
            UUID groupId,
            AuditAction action,
            AuditEntityType entityType,
            UUID entityId,
            Instant createdAt
    ) {
        validateInvariants(
                id,
                groupId,
                action,
                entityType,
                entityId,
                createdAt
        );

        return new AuditLog(
                id,
                groupId,
                action,
                entityType,
                entityId,
                createdAt
        );
    }

    public static AuditLog reconstitute(
            UUID id,
            UUID groupId,
            AuditAction action,
            AuditEntityType entityType,
            UUID entityId,
            Instant createdAt
    ) {
        validateInvariants(
                id,
                groupId,
                action,
                entityType,
                entityId,
                createdAt
        );

        return new AuditLog(
                id,
                groupId,
                action,
                entityType,
                entityId,
                createdAt
        );
    }

    private static void validateInvariants(
            UUID id,
            UUID groupId,
            AuditAction action,
            AuditEntityType entityType,
            UUID entityId,
            Instant createdAt
    ) {
        Guard.requireNotNull(id, "id");
        Guard.requireNotNull(groupId, "group id");
        Guard.requireNotNull(action, "action");
        Guard.requireNotNull(entityType, "entity type");
        Guard.requireNotNull(entityId, "entity id");
        Guard.requireNotNull(createdAt, "created at");
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public AuditAction getAction() {
        return action;
    }

    public AuditEntityType getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
