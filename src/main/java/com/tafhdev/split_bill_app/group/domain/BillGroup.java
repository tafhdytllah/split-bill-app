package com.tafhdev.split_bill_app.group.domain;

import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.domain.exception.Guard;

import java.time.Instant;
import java.util.*;

public class BillGroup {

    private final UUID id;
    private final String name;
    private final List<Participant> participants;
    private final Instant createdAt;

    private BillGroup(
            UUID id,
            String name,
            List<Participant> participants,
            Instant createdAt
    ) {
        this.id = id;
        this.name = name;
        this.participants = new ArrayList<>(participants);
        this.createdAt = createdAt;
    }

    public static BillGroup createNew(
            UUID id,
            String name,
            List<Participant> participants,
            Instant createdAt
    ) {

        validateInvariants(
                id,
                name,
                participants,
                createdAt
        );

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
        validateInvariants(
                id,
                name,
                participants,
                createdAt
        );

        return new BillGroup(
                id,
                name,
                participants,
                createdAt
        );
    }

    private static void validateInvariants(
            UUID id,
            String name,
            List<Participant> participants,
            Instant createdAt
    ) {
        Guard.requireNotNull(id, "id");
        Guard.requireNonBlank(name, "group name");
        Guard.requireNotNull(participants, "participants");
        Guard.requireNotNull(createdAt, "created at");

        if (participants.size() < 2) {
            throw new DomainException(
                    "group must have at least 2 participants"
            );
        }

        long uniqueIdCount = participants.stream()
                .map(Participant::getId)
                .distinct()
                .count();

        if (uniqueIdCount != participants.size()) {
            throw new DomainException(
                    "participant ids must be unique"
            );
        }

        long uniqueNameCount = participants.stream()
                .map(Participant::getName)
                .distinct()
                .count();

        if (uniqueNameCount != participants.size()) {
            throw new DomainException(
                    "participant names must be unique"
            );
        }
    }

    public Participant requireParticipant(
            UUID participantId
    ) {
        Guard.requireNotNull(participantId, "participant id");

        return participants.stream()
                .filter(participant ->
                        participant.getId().equals(participantId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new DomainException(
                                "participant does not belong to group"
                        )
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
