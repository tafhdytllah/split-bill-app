package com.tafhdev.split_bill_app.audit.domain;

import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditLogTest {

    private final UUID id = UUID.randomUUID();
    private final UUID groupId = UUID.randomUUID();
    private final AuditAction action = AuditAction.CREATED;
    private final AuditEntityType entityType = AuditEntityType.PAYMENT;
    private final UUID entityId = UUID.randomUUID();
    private final Instant createdAt =
            Instant.parse("2026-10-07T10:00:00Z");

    @Test
    void shouldCreateAuditLog() {

        AuditLog auditLog = AuditLog.createNew(
                id,
                groupId,
                action,
                entityType,
                entityId,
                createdAt
        );

        assertThat(auditLog.getId()).isEqualTo(id);
        assertThat(auditLog.getGroupId()).isEqualTo(groupId);
        assertThat(auditLog.getAction()).isEqualTo(action);
        assertThat(auditLog.getEntityType()).isEqualTo(entityType);
        assertThat(auditLog.getEntityId()).isEqualTo(entityId);
        assertThat(auditLog.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldReconstituteAuditLog() {

        AuditLog auditLog = AuditLog.reconstitute(
                id,
                groupId,
                action,
                entityType,
                entityId,
                createdAt
        );

        assertThat(auditLog.getId()).isEqualTo(id);
        assertThat(auditLog.getGroupId()).isEqualTo(groupId);
        assertThat(auditLog.getAction()).isEqualTo(action);
        assertThat(auditLog.getEntityType()).isEqualTo(entityType);
        assertThat(auditLog.getEntityId()).isEqualTo(entityId);
        assertThat(auditLog.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldRejectWhenIdIsNull() {

        assertThatThrownBy(() ->
                AuditLog.createNew(
                        null,
                        groupId,
                        action,
                        entityType,
                        entityId,
                        createdAt
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("id must not be null");
    }

    @Test
    void shouldRejectWhenGroupIdIsNull() {

        assertThatThrownBy(() ->
                AuditLog.createNew(
                        id,
                        null,
                        action,
                        entityType,
                        entityId,
                        createdAt
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("group id must not be null");
    }

    @Test
    void shouldRejectWhenActionIsNull() {

        assertThatThrownBy(() ->
                AuditLog.createNew(
                        id,
                        groupId,
                        null,
                        entityType,
                        entityId,
                        createdAt
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("action must not be null");
    }

    @Test
    void shouldRejectWhenEntityTypeIsNull() {

        assertThatThrownBy(() ->
                AuditLog.createNew(
                        id,
                        groupId,
                        action,
                        null,
                        entityId,
                        createdAt
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("entity type must not be null");
    }

    @Test
    void shouldRejectWhenEntityIdIsNull() {

        assertThatThrownBy(() ->
                AuditLog.createNew(
                        id,
                        groupId,
                        action,
                        entityType,
                        null,
                        createdAt
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("entity id must not be null");
    }

    @Test
    void shouldRejectWhenCreatedAtIsNull() {

        assertThatThrownBy(() ->
                AuditLog.createNew(
                        id,
                        groupId,
                        action,
                        entityType,
                        entityId,
                        null
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("created at must not be null");
    }
}