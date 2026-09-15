package com.tafhdev.split_bill_app.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class BillGroupServiceTest {

    @Mock
    private BillGroupRepository billGroupRepository;

    @Mock
    private IdGenerator idGenerator;

    private Clock clock;
    private BillGroupService billGroupService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                Instant.parse("2026-01-01T00:00:00Z"),
                ZoneOffset.UTC
        );

        billGroupService = new BillGroupService(
                billGroupRepository,
                idGenerator,
                clock
        );
    }

    @Test
    void shouldCreateGroup() {
        UUID groupId = UUID.randomUUID();
        UUID participantId1 = UUID.randomUUID();
        UUID participantId2 = UUID.randomUUID();

        when(idGenerator.generate())
                .thenReturn(groupId)
                .thenReturn(participantId1)
                .thenReturn(participantId2);

        when(billGroupRepository.save(any(BillGroup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BillGroup result = billGroupService.createGroup(
                "Trip Bandung",
                List.of("Taufik", "Andi")
        );

        assertThat(result.getId())
                .isEqualTo(groupId);

        assertThat(result.getName())
                .isEqualTo("Trip Bandung");

        assertThat(result.getParticipants())
                .hasSize(2);

        assertThat(result.getParticipants())
                .extracting("id")
                .containsExactly(
                        participantId1,
                        participantId2
                );

        assertThat(result.getParticipants())
                .extracting("name")
                .containsExactly(
                        "Taufik",
                        "Andi"
                );

        assertThat(result.getCreatedAt())
                .isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));

        verify(idGenerator, times(3))
                .generate();

        verify(billGroupRepository)
                .save(any(BillGroup.class));
    }

}