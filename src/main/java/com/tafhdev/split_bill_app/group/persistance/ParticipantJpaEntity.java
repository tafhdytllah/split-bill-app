package com.tafhdev.split_bill_app.group.persistance;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "participants")
public class ParticipantJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "group_id",
            nullable = false
    )
    private BillGroupJpaEntity group;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ParticipantJpaEntity() {
    }

    public ParticipantJpaEntity(
            UUID id,
            BillGroupJpaEntity group,
            String name,
            Instant createdAt
    ) {
        this.id = id;
        this.group = group;
        this.name = name;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public BillGroupJpaEntity getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
