package com.tafhdev.split_bill_app.payment.service;

import com.tafhdev.split_bill_app.audit.repository.AuditLogRepository;
import com.tafhdev.split_bill_app.expense.repository.ExpenseRepository;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.payment.controller.dto.PaymentResponse;
import com.tafhdev.split_bill_app.payment.controller.mapper.PaymentApiMapper;
import com.tafhdev.split_bill_app.payment.domain.Payment;
import com.tafhdev.split_bill_app.payment.repository.PaymentRepository;
import com.tafhdev.split_bill_app.payment.service.dto.CreatePaymentCommand;
import com.tafhdev.split_bill_app.payment.service.dto.PaymentResult;
import com.tafhdev.split_bill_app.shared.application.exception.ConflictException;
import com.tafhdev.split_bill_app.shared.application.exception.ResourceNotFoundException;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyHashGenerator;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyService;
import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;
import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BillGroupRepository billGroupRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private IdempotencyHashGenerator idempotencyHashGenerator;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private Clock clock;

    @Mock
    private PaymentApiMapper paymentApiMapper;

    @Mock
    private PaymentOutstandingCalculator paymentOutstandingCalculator;

    @Mock
    private BillGroup group;

    @Mock
    private Participant fromParticipant;

    @Mock
    private Participant toParticipant;

    @Mock
    private PaymentResponse paymentResponse;

    private PaymentService paymentService;

    private UUID groupId;
    private UUID fromParticipantId;
    private UUID toParticipantId;
    private UUID paymentId;
    private UUID auditLogId;

    private Money paymentAmount;
    private Money outstandingBalance;

    private Instant createdAt;

    @BeforeEach
    void setUp() {

        paymentService = new PaymentService(
                paymentRepository,
                billGroupRepository,
                auditLogRepository,
                expenseRepository,
                idempotencyHashGenerator,
                idempotencyService,
                idGenerator,
                clock,
                paymentApiMapper,
                paymentOutstandingCalculator
        );

        groupId = UUID.randomUUID();
        fromParticipantId = UUID.randomUUID();
        toParticipantId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        auditLogId = UUID.randomUUID();

        paymentAmount = Money.of(
                new java.math.BigDecimal("50000.00")
        );

        outstandingBalance = Money.of(
                new java.math.BigDecimal("100000.00")
        );

        createdAt = Instant.parse(
                "2026-01-01T10:00:00Z"
        );
    }

    @Test
    void shouldCreatePayment() {

        CreatePaymentCommand command = new CreatePaymentCommand(
                "payment-key",
                groupId,
                fromParticipantId,
                toParticipantId,
                paymentAmount
        );

        when(billGroupRepository.findByIdForUpdate(groupId))
                .thenReturn(Optional.of(group));

        when(idempotencyService.find(
                IdempotencyScope.PAYMENT,
                command.idempotencyKey()
        ))
                .thenReturn(Optional.empty());

        when(idempotencyHashGenerator.generate(any()))
                .thenReturn("request-hash");

        when(group.requireParticipant(fromParticipantId))
                .thenReturn(fromParticipant);

        when(group.requireParticipant(toParticipantId))
                .thenReturn(toParticipant);

        when(fromParticipant.getId())
                .thenReturn(fromParticipantId);

        when(toParticipant.getId())
                .thenReturn(toParticipantId);

        when(expenseRepository.findByGroupId(groupId))
                .thenReturn(List.of());

        when(paymentRepository.findByGroupId(groupId))
                .thenReturn(List.of());

        when(paymentOutstandingCalculator.calculate(
                fromParticipantId,
                toParticipantId,
                List.of(),
                List.of()
        ))
                .thenReturn(outstandingBalance);

        Idempotency idempotency = mock(Idempotency.class);

        when(idempotencyService.create(
                IdempotencyScope.PAYMENT,
                command.idempotencyKey(),
                "request-hash"
        ))
                .thenReturn(idempotency);

        when(idGenerator.generate())
                .thenReturn(paymentId)
                .thenReturn(auditLogId);

        when(clock.instant())
                .thenReturn(createdAt);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentApiMapper.toResponse(
                any(Payment.class),
                eq(fromParticipant),
                eq(toParticipant)
        ))
                .thenReturn(paymentResponse);

        PaymentResult result =
                paymentService.createPayment(command);

        assertThat(result.response())
                .isSameAs(paymentResponse);

        assertThat(result.replay())
                .isFalse();

        verify(billGroupRepository)
                .findByIdForUpdate(groupId);

        verify(paymentRepository)
                .save(any(Payment.class));

        verify(auditLogRepository)
                .save(any());

        verify(idempotencyService)
                .complete(
                        idempotency,
                        HttpStatus.CREATED.value(),
                        paymentResponse
                );
    }

    @Test
    void shouldReplayPaymentWhenIdempotencyKeyIsReused() {

        CreatePaymentCommand command = new CreatePaymentCommand(
                "payment-key",
                groupId,
                fromParticipantId,
                toParticipantId,
                paymentAmount
        );

        Idempotency idempotency = mock(Idempotency.class);

        when(billGroupRepository.findByIdForUpdate(groupId))
                .thenReturn(Optional.of(group));

        when(idempotencyService.find(
                IdempotencyScope.PAYMENT,
                command.idempotencyKey()
        ))
                .thenReturn(Optional.of(idempotency));

        when(idempotency.getRequestHash())
                .thenReturn("request-hash");

        when(idempotencyHashGenerator.generate(any()))
                .thenReturn("request-hash");

        when(idempotencyService.getResponse(
                idempotency,
                PaymentResponse.class
        ))
                .thenReturn(paymentResponse);

        PaymentResult result =
                paymentService.createPayment(command);

        assertThat(result.response())
                .isSameAs(paymentResponse);

        assertThat(result.replay())
                .isTrue();

        verify(billGroupRepository)
                .findByIdForUpdate(groupId);

        verifyNoInteractions(expenseRepository);
        verifyNoInteractions(paymentRepository);
        verifyNoInteractions(auditLogRepository);
        verifyNoInteractions(paymentOutstandingCalculator);
    }

    @Test
    void shouldRejectWhenIdempotencyKeyIsReusedWithDifferentRequest() {

        CreatePaymentCommand command = new CreatePaymentCommand(
                "payment-key",
                groupId,
                fromParticipantId,
                toParticipantId,
                paymentAmount
        );

        Idempotency idempotency = mock(Idempotency.class);

        when(billGroupRepository.findByIdForUpdate(groupId))
                .thenReturn(Optional.of(group));

        when(idempotencyService.find(
                IdempotencyScope.PAYMENT,
                command.idempotencyKey()
        ))
                .thenReturn(Optional.of(idempotency));

        when(idempotency.getRequestHash())
                .thenReturn("old-request-hash");

        when(idempotencyHashGenerator.generate(any()))
                .thenReturn("new-request-hash");

        assertThatThrownBy(() ->
                paymentService.createPayment(command)
        )
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "idempotency key reused with different request"
                );

        verify(billGroupRepository)
                .findByIdForUpdate(groupId);

        verifyNoInteractions(paymentRepository);
        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldThrowWhenGroupDoesNotExist() {

        CreatePaymentCommand command = new CreatePaymentCommand(
                "payment-key",
                groupId,
                fromParticipantId,
                toParticipantId,
                paymentAmount
        );

        when(billGroupRepository.findByIdForUpdate(groupId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                paymentService.createPayment(command)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("group not found");

        verify(billGroupRepository)
                .findByIdForUpdate(groupId);

        verifyNoInteractions(idempotencyService);
        verifyNoInteractions(idempotencyHashGenerator);
        verifyNoInteractions(expenseRepository);
        verifyNoInteractions(paymentRepository);
        verifyNoInteractions(auditLogRepository);
        verifyNoInteractions(paymentOutstandingCalculator);
    }

    @Test
    void shouldRejectWhenPaymentAmountExceedsOutstandingDebt() {

        CreatePaymentCommand command = new CreatePaymentCommand(
                "payment-key",
                groupId,
                fromParticipantId,
                toParticipantId,
                Money.of(
                        new java.math.BigDecimal("150000.00")
                )
        );

        when(billGroupRepository.findByIdForUpdate(groupId))
                .thenReturn(Optional.of(group));

        when(idempotencyService.find(
                IdempotencyScope.PAYMENT,
                command.idempotencyKey()
        ))
                .thenReturn(Optional.empty());

        when(idempotencyHashGenerator.generate(any()))
                .thenReturn("request-hash");

        when(group.requireParticipant(fromParticipantId))
                .thenReturn(fromParticipant);

        when(group.requireParticipant(toParticipantId))
                .thenReturn(toParticipant);

        when(expenseRepository.findByGroupId(groupId))
                .thenReturn(List.of());

        when(paymentRepository.findByGroupId(groupId))
                .thenReturn(List.of());

        when(paymentOutstandingCalculator.calculate(
                fromParticipantId,
                toParticipantId,
                List.of(),
                List.of()
        ))
                .thenReturn(outstandingBalance);

        assertThatThrownBy(() ->
                paymentService.createPayment(command)
        )
                .isInstanceOf(DomainException.class)
                .hasMessage(
                        "payment amount exceeds outstanding debt"
                );

        verify(billGroupRepository)
                .findByIdForUpdate(groupId);

        verify(paymentRepository)
                .findByGroupId(groupId);

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verifyNoInteractions(auditLogRepository);
        verifyNoInteractions(paymentApiMapper);

        verify(idempotencyService, never())
                .create(any(), any(), any());
    }
}