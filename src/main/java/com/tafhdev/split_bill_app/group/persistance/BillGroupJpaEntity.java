package com.tafhdev.split_bill_app.group.persistance;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "groups")
public class BillGroupJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "group",
            fetch = FetchType.LAZY
    )
    private List<ParticipantJpaEntity> participants = new ArrayList<>();

    protected BillGroupJpaEntity() {
    }

    public BillGroupJpaEntity(UUID id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ParticipantJpaEntity> getParticipants() {
        return Collections.unmodifiableList(participants);
    }
}
