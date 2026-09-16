package com.tafhdev.split_bill_app.group.domain;

import com.tafhdev.split_bill_app.shared.domain.exception.Guard;

import java.time.Instant;
import java.util.UUID;

public class Participant {

    private final UUID id;
    private final UUID groupId;
    private final String name;
    private final Instant createdAt;

    private Participant(
            UUID id,
            UUID groupId,
            String name,
            Instant createdAt
    ) {
        this.id = id;
        this.groupId = groupId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static Participant createNew(
        UUID id,
        UUID groupId,
        String name,
        Instant createdAt
    ) {
        validateInvariants(
                id,
                groupId,
                name,
                createdAt
        );

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
        validateInvariants(
                id,
                groupId,
                name,
                createdAt
        );

        return new Participant(
                id,
                groupId,
                name,
                createdAt
        );
    }

    private static void validateInvariants(
            UUID id,
            UUID groupId,
            String name,
            Instant createdAt
    ) {
        Guard.requireNotNull(id, "id");
        Guard.requireNotNull(groupId, "group id");
        Guard.requireNonBlank(name, "name");
        Guard.requireNotNull(createdAt, "created at");
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
