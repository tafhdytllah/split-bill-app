package com.tafhdev.split_bill_app.group.domain;

import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.domain.exception.Guard;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class BillGroup {

    private final UUID id;
    private final String name;
    private final List<Participant> participants;
    private final Instant createdAt;

    public BillGroup(
            UUID id,
            String name,
            List<Participant> participants,
            Instant createdAt
    ) {
        this.id = Guard.requireNotNull(id, "id");
        this.name = Guard.requireNonBlank(name, "group name");
        this.participants = new ArrayList<>(
                Guard.requireNotNull(participants, "participants")
        );
        this.createdAt = Guard.requireNotNull(createdAt, "createdAt");
    }

    public static BillGroup createNew(
            UUID id,
            String name,
            List<Participant> participants,
            Instant createdAt
    ) {

        if (participants.size() < 2) {
            throw new DomainException(
                    "group must have at least 2 participants"
            );
        }

        Set<String> uniqueParticipantNames = participants
                .stream()
                .map(Participant::getName)
                .collect(Collectors.toSet());

        if (uniqueParticipantNames.size() != participants.size()) {
            throw new DomainException(
                    "participant names must be unique"
            );
        }

        return new BillGroup(
                id,
                name,
                participants,
                createdAt
        );
    }

    public static BillGroup reconstitute(
            UUID id,
            String name,
            List<Participant> participants,
            Instant createdAt
    ) {
        return new BillGroup(
                id,
                name,
                participants,
                createdAt
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
