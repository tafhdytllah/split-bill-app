package com.tafhdev.split_bill_app.group.domain;

import com.tafhdev.split_bill_app.shared.domain.exception.Guard;

import java.time.Instant;
import java.util.UUID;

public class Participant {

    private final UUID id;
    private final UUID groupId;
    private final String name;
    private final Instant createdAt;

    public Participant(
            UUID id,
            UUID groupId,
            String name,
            Instant createdAt
    ) {
        this.id = Guard.requireNotNull(id, "id");
        this.groupId = Guard.requireNotNull(groupId, "groupId");
        this.name = Guard.requireNonBlank(name, "name");
        this.createdAt = Guard.requireNotNull(createdAt, "createdAt");
    }

    public static Participant createNew(
        UUID id,
        UUID groupId,
        String name,
        Instant createdAt
    ) {
        return new Participant(
                id,
                groupId,
                name,
                createdAt
        );
    }

    public static Participant reconstitute(
            UUID id,
            UUID groupId,
            String name,
            Instant createdAt
    ) {
        return new Participant(
                id,
                groupId,
                name,
                createdAt
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
