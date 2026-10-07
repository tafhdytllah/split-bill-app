package com.tafhdev.split_bill_app.shared.application.service;

import com.tafhdev.split_bill_app.payment.controller.dto.ParticipantResponse;
import com.tafhdev.split_bill_app.payment.controller.dto.PaymentResponse;
import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import com.tafhdev.split_bill_app.shared.repository.IdempotencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private ObjectMapper objectMapper;

    private IdempotencyService idempotencyService;

    private final UUID id = UUID.randomUUID();

    private final IdempotencyScope scope =
            IdempotencyScope.PAYMENT;

    private final String idempotencyKey =
            "idem-123";

    private final String requestHash =
            "a".repeat(64);

    private final Instant createdAt =
            Instant.parse("2026-10-07T10:00:00Z");

    @BeforeEach
    void setUp() {

        Clock clock = Clock.fixed(
                createdAt,
                ZoneOffset.UTC
        );

        idempotencyService = new IdempotencyService(
                idempotencyRepository,
                idGenerator,
                clock,
                objectMapper
        );
    }

    @Test
    void shouldFindExistingIdempotency() {

        Idempotency idempotency = Idempotency.createNew(
                id,
                scope,
                idempotencyKey,
                requestHash,
                createdAt
        );

        when(idempotencyRepository.findByScopeAndKey(
                scope,
                idempotencyKey
        )).thenReturn(Optional.of(idempotency));

        Optional<Idempotency> result =
                idempotencyService.find(
                        scope,
                        idempotencyKey
                );

        assertThat(result)
                .isPresent()
                .contains(idempotency);

        verify(idempotencyRepository)
                .findByScopeAndKey(
                        scope,
                        idempotencyKey
                );
    }

    @Test
    void shouldReturnEmptyWhenIdempotencyDoesNotExist() {

        when(idempotencyRepository.findByScopeAndKey(
                scope,
                idempotencyKey
        )).thenReturn(Optional.empty());

        Optional<Idempotency> result =
                idempotencyService.find(
                        scope,
                        idempotencyKey
                );

        assertThat(result).isEmpty();

        verify(idempotencyRepository)
                .findByScopeAndKey(
                        scope,
                        idempotencyKey
                );
    }

    @Test
    void shouldCreateAndSaveIdempotency() {

        when(idGenerator.generate())
                .thenReturn(id);

        Idempotency idempotency = Idempotency.createNew(
                id,
                scope,
                idempotencyKey,
                requestHash,
                createdAt
        );

        when(idempotencyRepository.saveAndFlush(
                any(Idempotency.class)
        )).thenReturn(idempotency);

        Idempotency result =
                idempotencyService.create(
                        scope,
                        idempotencyKey,
                        requestHash
                );

        assertThat(result)
                .isEqualTo(idempotency);

        assertThat(result.getId())
                .isEqualTo(id);

        assertThat(result.getScope())
                .isEqualTo(scope);

        assertThat(result.getIdempotencyKey())
                .isEqualTo(idempotencyKey);

        assertThat(result.getRequestHash())
                .isEqualTo(requestHash);

        assertThat(result.getCreatedAt())
                .isEqualTo(createdAt);

        assertThat(result.getResponseCode())
                .isNull();

        assertThat(result.getResponseBody())
                .isNull();

        verify(idGenerator)
                .generate();

        verify(idempotencyRepository)
                .saveAndFlush(any(Idempotency.class));
    }

    @Test
    void shouldCompleteAndSaveIdempotency() throws Exception {

        Idempotency idempotency = Idempotency.createNew(
                id,
                scope,
                idempotencyKey,
                requestHash,
                createdAt
        );

        UUID paymentId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID fromParticipantId = UUID.randomUUID();
        UUID toParticipantId = UUID.randomUUID();

        PaymentResponse response = new PaymentResponse(
                paymentId,
                groupId,
                new ParticipantResponse(
                        fromParticipantId,
                        "Taufik"
                ),
                new ParticipantResponse(
                        toParticipantId,
                        "Andi"
                ),
                new BigDecimal("50000.00"),
                createdAt
        );

        String responseBody = """
                {
                  "id": "%s",
                  "groupId": "%s",
                  "fromParticipant": {
                    "id": "%s",
                    "name": "Taufik"
                  },
                  "toParticipant": {
                    "id": "%s",
                    "name": "Andi"
                  },
                  "amount": "50000.00",
                  "createdAt": "%s"
                }
                """.formatted(
                paymentId,
                groupId,
                fromParticipantId,
                toParticipantId,
                createdAt
        );

        when(objectMapper.writeValueAsString(response))
                .thenReturn(responseBody);

        idempotencyService.complete(
                idempotency,
                201,
                response
        );

        assertThat(idempotency.getResponseCode())
                .isEqualTo(201);

        assertThat(idempotency.getResponseBody())
                .isEqualTo(responseBody);

        verify(objectMapper)
                .writeValueAsString(response);

        verify(idempotencyRepository)
                .save(idempotency);
    }

    @Test
    void shouldThrowWhenResponseSerializationFails()
            throws Exception {

        Idempotency idempotency = Idempotency.createNew(
                id,
                scope,
                idempotencyKey,
                requestHash,
                createdAt
        );

        RuntimeException cause =
                new RuntimeException("serialization failed");

        when(objectMapper.writeValueAsString(any()))
                .thenThrow(cause);

        assertThatThrownBy(() ->
                idempotencyService.complete(
                        idempotency,
                        201,
                        new Object()
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "failed to serialize idempotency response"
                )
                .hasCause(cause);

        verify(idempotencyRepository, never())
                .save(any(Idempotency.class));
    }

    @Test
    void shouldGetResponse() throws Exception {

        String responseBody = """
                {
                  "id": "00000000-0000-0000-0000-000000000001",
                  "groupId": "00000000-0000-0000-0000-000000000002",
                  "fromParticipant": {
                    "id": "00000000-0000-0000-0000-000000000003",
                    "name": "Taufik"
                  },
                  "toParticipant": {
                    "id": "00000000-0000-0000-0000-000000000004",
                    "name": "Andi"
                  },
                  "amount": "50000.00",
                  "createdAt": "2026-10-07T10:00:00Z"
                }
                """;

        Idempotency idempotency =
                Idempotency.reconstitute(
                        id,
                        scope,
                        idempotencyKey,
                        requestHash,
                        201,
                        responseBody,
                        createdAt
                );

        PaymentResponse expectedResponse =
                new PaymentResponse(
                        UUID.fromString(
                                "00000000-0000-0000-0000-000000000001"
                        ),
                        UUID.fromString(
                                "00000000-0000-0000-0000-000000000002"
                        ),
                        new ParticipantResponse(
                                UUID.fromString(
                                        "00000000-0000-0000-0000-000000000003"
                                ),
                                "Taufik"
                        ),
                        new ParticipantResponse(
                                UUID.fromString(
                                        "00000000-0000-0000-0000-000000000004"
                                ),
                                "Andi"
                        ),
                        new BigDecimal("50000.00"),
                        createdAt
                );

        when(objectMapper.readValue(
                responseBody,
                PaymentResponse.class
        )).thenReturn(expectedResponse);

        PaymentResponse result =
                idempotencyService.getResponse(
                        idempotency,
                        PaymentResponse.class
                );

        assertThat(result)
                .isEqualTo(expectedResponse);

        verify(objectMapper)
                .readValue(
                        responseBody,
                        PaymentResponse.class
                );
    }

    @Test
    void shouldThrowWhenResponseDeserializationFails()
            throws Exception {

        String responseBody = """
                {"invalid-json"
                """;

        Idempotency idempotency =
                Idempotency.reconstitute(
                        id,
                        scope,
                        idempotencyKey,
                        requestHash,
                        201,
                        responseBody,
                        createdAt
                );

        RuntimeException cause =
                new RuntimeException(
                        "deserialization failed"
                );

        when(objectMapper.readValue(
                responseBody,
                PaymentResponse.class
        )).thenThrow(cause);

        assertThatThrownBy(() ->
                idempotencyService.getResponse(
                        idempotency,
                        PaymentResponse.class
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "failed to deserialize idempotency response"
                )
                .hasCause(cause);
    }
}