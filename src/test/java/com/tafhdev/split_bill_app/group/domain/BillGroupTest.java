package com.tafhdev.split_bill_app.group.domain;

import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BillGroupTest {

    private final Instant createdAt =
            Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void shouldCreateBillGroup() {
        UUID groupId = UUID.randomUUID();

        Participant taufik = Participant.createNew(
                UUID.randomUUID(),
                groupId,
                "Taufik",
                createdAt
        );

        Participant andi = Participant.createNew(
                UUID.randomUUID(),
                groupId,
                "Andi",
                createdAt
        );

        BillGroup group = BillGroup.createNew(
                groupId,
                "Trip Bandung",
                List.of(taufik, andi),
                createdAt
        );

        assertThat(group.getId())
                .isEqualTo(groupId);

        assertThat(group.getName())
                .isEqualTo("Trip Bandung");

        assertThat(group.getParticipants())
                .containsExactly(taufik, andi);

        assertThat(group.getCreatedAt())
                .isEqualTo(createdAt);
    }

    @Test
    void shouldRejectNullGroupName() {
        UUID groupId = UUID.randomUUID();

        List<Participant> participants = createParticipants(groupId);

        assertThatThrownBy(() ->
                BillGroup.createNew(
                        groupId,
                        null,
                        participants,
                        createdAt
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("group name must not be null");
    }

    @Test
    void shouldRejectBlankGroupName() {
        UUID groupId = UUID.randomUUID();

        List<Participant> participants = createParticipants(groupId);

        assertThatThrownBy(() ->
                BillGroup.createNew(
                        groupId,
                        "   ",
                        participants,
                        createdAt
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("group name must not be blank");
    }

    @Test
    void shouldRejectGroupWithLessThanTwoParticipants() {
        UUID groupId = UUID.randomUUID();

        Participant taufik = Participant.createNew(
                UUID.randomUUID(),
                groupId,
                "Taufik",
                createdAt
        );

        assertThatThrownBy(() ->
                BillGroup.createNew(
                        groupId,
                        "Trip Bandung",
                        List.of(taufik),
                        createdAt
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("group must have at least 2 participants");
    }

    @Test
    void shouldRejectDuplicateParticipantNames() {
        UUID groupId = UUID.randomUUID();

        Participant taufik1 = Participant.createNew(
                UUID.randomUUID(),
                groupId,
                "Taufik",
                createdAt
        );

        Participant taufik2 = Participant.createNew(
                UUID.randomUUID(),
                groupId,
                "Taufik",
                createdAt
        );

        assertThatThrownBy(() ->
                BillGroup.createNew(
                        groupId,
                        "Trip Bandung",
                        List.of(taufik1, taufik2),
                        createdAt
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("participant names must be unique");
    }

    private List<Participant> createParticipants(UUID groupId) {
        return List.of(
                Participant.createNew(
                        UUID.randomUUID(),
                        groupId,
                        "Taufik",
                        createdAt
                ),
                Participant.createNew(
                        UUID.randomUUID(),
                        groupId,
                        "Andi",
                        createdAt
                )
        );
    }

}