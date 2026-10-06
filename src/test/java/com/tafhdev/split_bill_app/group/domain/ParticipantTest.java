package com.tafhdev.split_bill_app.group.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ParticipantTest {

    private final Instant createdAt =
            Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void shouldCreateParticipant() {
        UUID participantId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Participant participant = Participant.createNew(
                participantId,
                groupId,
                "Taufik",
                createdAt
        );

        assertThat(participant.getId())
                .isEqualTo(participantId);

        assertThat(participant.getGroupId())
                .isEqualTo(groupId);

        assertThat(participant.getName())
                .isEqualTo("Taufik");

        assertThat(participant.getCreatedAt())
                .isEqualTo(createdAt);
    }

}