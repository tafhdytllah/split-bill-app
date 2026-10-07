package com.tafhdev.split_bill_app.group.service;

import com.tafhdev.split_bill_app.group.controller.dto.BillGroupResponse;
import com.tafhdev.split_bill_app.group.controller.mapper.BillGroupApiMapper;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.group.service.dto.BillGroupResult;
import com.tafhdev.split_bill_app.group.service.dto.CreateBillGroupCommand;
import com.tafhdev.split_bill_app.shared.application.exception.ConflictException;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyHashGenerator;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyService;
import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillGroupServiceTest {

    @Mock
    private BillGroupRepository billGroupRepository;

    @Mock
    private IdempotencyHashGenerator idempotencyHashGenerator;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private BillGroupApiMapper billGroupApiMapper;

    private BillGroupService billGroupService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-01-01T00:00:00Z"),
                ZoneOffset.UTC
        );

        billGroupService = new BillGroupService(
                billGroupRepository,
                idempotencyHashGenerator,
                idempotencyService,
                idGenerator,
                clock,
                billGroupApiMapper
        );
    }

    @Test
    void shouldCreateGroup() {

        UUID idempotencyId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID participantId1 = UUID.randomUUID();
        UUID participantId2 = UUID.randomUUID();

        String idempotencyKey = "idem-key-1";
        String requestHash = "request-hash";

        CreateBillGroupCommand command =
                new CreateBillGroupCommand(
                        idempotencyKey,
                        "Trip Bandung",
                        List.of("Taufik", "Andi")
                );

        BillGroupResponse response = mock(BillGroupResponse.class);

        when(idempotencyHashGenerator.generate(any(String.class)))
                .thenReturn(requestHash);

        when(idempotencyService.insertIfAbsent(any(Idempotency.class)))
                .thenReturn(true);

        when(idGenerator.generate())
                .thenReturn(idempotencyId)
                .thenReturn(groupId)
                .thenReturn(participantId1)
                .thenReturn(participantId2);

        when(billGroupRepository.save(any(BillGroup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(billGroupApiMapper.toResponse(any(BillGroup.class)))
                .thenReturn(response);

        // when
        BillGroupResult result =
                billGroupService.createGroup(command);

        // then
        assertThat(result.response())
                .isSameAs(response);

        assertThat(result.replay())
                .isFalse();

        verify(idempotencyHashGenerator)
                .generate(any(String.class));

        verify(idempotencyService)
                .insertIfAbsent(any(Idempotency.class));

        verify(billGroupRepository)
                .save(any(BillGroup.class));

        verify(billGroupApiMapper)
                .toResponse(any(BillGroup.class));

        verify(idempotencyService)
                .complete(
                        any(Idempotency.class),
                        eq(201),
                        same(response)
                );

        verify(idGenerator, times(4))
                .generate();

        verify(idempotencyService, never())
                .find(
                        any(IdempotencyScope.class),
                        any(String.class)
                );

        verify(idempotencyService, never())
                .getResponse(
                        any(Idempotency.class),
                        eq(BillGroupResponse.class)
                );
    }

    @Test
    void shouldReplayExistingGroupWhenIdempotencyKeyIsReusedWithSameRequest() {

        // given
        UUID idempotencyId = UUID.randomUUID();

        String idempotencyKey = "idem-key-1";
        String requestHash = "request-hash";

        CreateBillGroupCommand command =
                new CreateBillGroupCommand(
                        idempotencyKey,
                        "Trip Bandung",
                        List.of("Taufik", "Andi")
                );

        Idempotency idempotency = mock(Idempotency.class);
        BillGroupResponse response = mock(BillGroupResponse.class);

        when(idempotencyHashGenerator.generate(any(String.class)))
                .thenReturn(requestHash);

        when(idGenerator.generate())
                .thenReturn(idempotencyId);

        when(idempotencyService.insertIfAbsent(any(Idempotency.class)))
                .thenReturn(false);

        when(idempotencyService.find(
                IdempotencyScope.GROUP,
                idempotencyKey
        )).thenReturn(Optional.of(idempotency));

        when(idempotency.getRequestHash())
                .thenReturn(requestHash);

        when(idempotencyService.getResponse(
                idempotency,
                BillGroupResponse.class
        )).thenReturn(response);

        // when
        BillGroupResult result =
                billGroupService.createGroup(command);

        // then
        assertThat(result.response())
                .isSameAs(response);

        assertThat(result.replay())
                .isTrue();

        verify(idempotencyHashGenerator)
                .generate(any(String.class));

        verify(idGenerator)
                .generate();

        verify(idempotencyService)
                .insertIfAbsent(any(Idempotency.class));

        verify(idempotencyService)
                .find(
                        IdempotencyScope.GROUP,
                        idempotencyKey
                );

        verify(idempotencyService)
                .getResponse(
                        idempotency,
                        BillGroupResponse.class
                );

        verify(idempotencyService, never())
                .complete(
                        any(Idempotency.class),
                        anyInt(),
                        any()
                );

        verify(billGroupRepository, never())
                .save(any(BillGroup.class));

        verify(billGroupApiMapper, never())
                .toResponse(any(BillGroup.class));

        verify(idGenerator, times(1))
                .generate();
    }

    @Test
    void shouldRejectWhenIdempotencyKeyIsReusedWithDifferentRequest() {

        // given
        UUID idempotencyId = UUID.randomUUID();

        String idempotencyKey = "idem-key-1";

        CreateBillGroupCommand command =
                new CreateBillGroupCommand(
                        idempotencyKey,
                        "Trip Jakarta",
                        List.of("Taufik", "Andi")
                );

        Idempotency idempotency = mock(Idempotency.class);

        when(idempotencyHashGenerator.generate(any(String.class)))
                .thenReturn("new-request-hash");

        when(idGenerator.generate())
                .thenReturn(idempotencyId);

        when(idempotencyService.insertIfAbsent(any(Idempotency.class)))
                .thenReturn(false);

        when(idempotencyService.find(
                IdempotencyScope.GROUP,
                idempotencyKey
        )).thenReturn(Optional.of(idempotency));

        when(idempotency.getRequestHash())
                .thenReturn("old-request-hash");

        // when & then
        assertThatThrownBy(() ->
                billGroupService.createGroup(command)
        )
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "idempotency key reused with different request"
                );

        verify(idempotencyHashGenerator)
                .generate(any(String.class));

        verify(idGenerator)
                .generate();

        verify(idempotencyService)
                .insertIfAbsent(any(Idempotency.class));

        verify(idempotencyService)
                .find(
                        IdempotencyScope.GROUP,
                        idempotencyKey
                );

        verify(idempotencyService, never())
                .getResponse(
                        any(Idempotency.class),
                        eq(BillGroupResponse.class)
                );

        verify(idempotencyService, never())
                .complete(
                        any(Idempotency.class),
                        anyInt(),
                        any()
                );

        verify(billGroupRepository, never())
                .save(any(BillGroup.class));

        verify(billGroupApiMapper, never())
                .toResponse(any(BillGroup.class));

        verify(idGenerator, times(1))
                .generate();
    }

    @Test
    void shouldCreateParticipantsWithGeneratedIdsAndGroupId() {

        // given
        UUID idempotencyId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID participantId1 = UUID.randomUUID();
        UUID participantId2 = UUID.randomUUID();

        String idempotencyKey = "idem-key-1";
        String requestHash = "request-hash";

        CreateBillGroupCommand command =
                new CreateBillGroupCommand(
                        idempotencyKey,
                        "Trip Bandung",
                        List.of("Taufik", "Andi")
                );

        BillGroupResponse response = mock(BillGroupResponse.class);

        when(idempotencyHashGenerator.generate(any(String.class)))
                .thenReturn(requestHash);

        when(idGenerator.generate())
                .thenReturn(idempotencyId)
                .thenReturn(groupId)
                .thenReturn(participantId1)
                .thenReturn(participantId2);

        when(idempotencyService.insertIfAbsent(any(Idempotency.class)))
                .thenReturn(true);

        when(billGroupRepository.save(any(BillGroup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(billGroupApiMapper.toResponse(any(BillGroup.class)))
                .thenReturn(response);

        // when
        billGroupService.createGroup(command);

        // then
        var captor =
                org.mockito.ArgumentCaptor.forClass(BillGroup.class);

        verify(billGroupRepository)
                .save(captor.capture());

        BillGroup savedGroup = captor.getValue();

        assertThat(savedGroup.getId())
                .isEqualTo(groupId);

        assertThat(savedGroup.getName())
                .isEqualTo("Trip Bandung");

        assertThat(savedGroup.getCreatedAt())
                .isEqualTo(
                        Instant.parse("2026-01-01T00:00:00Z")
                );

        assertThat(savedGroup.getParticipants())
                .hasSize(2);

        assertThat(savedGroup.getParticipants())
                .extracting(Participant::getId)
                .containsExactly(
                        participantId1,
                        participantId2
                );

        assertThat(savedGroup.getParticipants())
                .extracting(Participant::getName)
                .containsExactly(
                        "Taufik",
                        "Andi"
                );

        assertThat(savedGroup.getParticipants())
                .allSatisfy(participant ->
                        assertThat(participant.getGroupId())
                                .isEqualTo(groupId)
                );

        verify(idempotencyService)
                .insertIfAbsent(any(Idempotency.class));

        verify(idempotencyService)
                .complete(
                        any(Idempotency.class),
                        eq(201),
                        same(response)
                );

        verify(idGenerator, times(4))
                .generate();
    }
}