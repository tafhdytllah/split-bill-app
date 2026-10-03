package com.tafhdev.split_bill_app.audit.persistence.entity;

import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private BillGroupEntity group;

    @Column(length = 50, nullable = false)
    private String action;

    @Column(name = "entity_type", length = 50, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AuditLogEntity() {
    }

    public AuditLogEntity(
            UUID id,
            BillGroupEntity group,
            String action,
            String entityType,
            UUID entityId,
            Instant createdAt
    ) {
        this.id = id;
        this.group = group;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public BillGroupEntity getGroup() {
        return group;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
