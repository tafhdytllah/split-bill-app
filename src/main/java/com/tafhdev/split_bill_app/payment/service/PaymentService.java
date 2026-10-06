package com.tafhdev.split_bill_app.payment.service;

import com.tafhdev.split_bill_app.audit.domain.AuditAction;
import com.tafhdev.split_bill_app.audit.domain.AuditEntityType;
import com.tafhdev.split_bill_app.audit.domain.AuditLog;
import com.tafhdev.split_bill_app.audit.repository.AuditLogRepository;
import com.tafhdev.split_bill_app.expense.domain.Expense;
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
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyRequestBuilder;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyService;
import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;
import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillGroupRepository billGroupRepository;
    private final AuditLogRepository auditLogRepository;
    private final ExpenseRepository expenseRepository;
    private final IdempotencyHashGenerator idempotencyHashGenerator;
    private final IdempotencyService idempotencyService;
    private final IdGenerator idGenerator;
    private final Clock clock;
    private final PaymentApiMapper paymentApiMapper;
    private final PaymentOutstandingCalculator paymentOutstandingCalculator;

    public PaymentService(
            PaymentRepository paymentRepository,
            BillGroupRepository billGroupRepository,
            AuditLogRepository auditLogRepository,
            ExpenseRepository expenseRepository,
            IdempotencyHashGenerator idempotencyHashGenerator,
            IdempotencyService idempotencyService,
            IdGenerator idGenerator,
            Clock clock,
            PaymentApiMapper paymentApiMapper,
            PaymentOutstandingCalculator paymentOutstandingCalculator
    ) {
        this.paymentRepository = paymentRepository;
        this.billGroupRepository = billGroupRepository;
        this.auditLogRepository = auditLogRepository;
        this.expenseRepository = expenseRepository;
        this.idempotencyHashGenerator = idempotencyHashGenerator;
        this.idempotencyService = idempotencyService;
        this.idGenerator = idGenerator;
        this.clock = clock;
        this.paymentApiMapper = paymentApiMapper;
        this.paymentOutstandingCalculator = paymentOutstandingCalculator;
    }

    @Transactional
    public PaymentResult createPayment(CreatePaymentCommand command) {

        String request = IdempotencyRequestBuilder.build(
                command.groupId(),
                command.fromParticipantId(),
                command.toParticipantId(),
                command.amount()
        );

        String requestHash = idempotencyHashGenerator.generate(request);

        Optional<Idempotency> existing = idempotencyService.find(
                IdempotencyScope.PAYMENT,
                command.idempotencyKey()
        );

        if (existing.isPresent()) {

            Idempotency idempotency = existing.get();

            boolean sameHash = MessageDigest.isEqual(
                    idempotency.getRequestHash().getBytes(StandardCharsets.UTF_8),
                    requestHash.getBytes(StandardCharsets.UTF_8)
            );

            if (!sameHash) {
                throw new ConflictException(
                        "idempotency key reused with different request"
                );
            }

            PaymentResponse response = idempotencyService.getResponse(
                    idempotency,
                    PaymentResponse.class
            );

            return new PaymentResult(
                    response,
                    true
            );
        }

        BillGroup group = billGroupRepository.findById(command.groupId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "group not found"
                        ));

        Participant fromParticipant = group.requireParticipant(command.fromParticipantId());

        Participant toParticipant = group.requireParticipant(command.toParticipantId());

        List<Expense> expenses = expenseRepository.findByGroupId(command.groupId());

        List<Payment> payments = paymentRepository.findByGroupId(command.groupId());

        Money outstandingBalance = paymentOutstandingCalculator.calculate(
                command.fromParticipantId(),
                command.toParticipantId(),
                expenses,
                payments
        );

        if (command.amount().value().compareTo(outstandingBalance.value()) > 0) {
            throw new DomainException("payment amount exceeds outstanding debt");
        }

        Idempotency idempotency = idempotencyService.create(
                IdempotencyScope.PAYMENT,
                command.idempotencyKey(),
                requestHash
        );

        Payment payment = Payment.createNew(
                idGenerator.generate(),
                command.groupId(),
                fromParticipant.getId(),
                toParticipant.getId(),
                command.amount(),
                Instant.now(clock)
        );

        Payment savedPayment = paymentRepository.save(payment);

        AuditLog auditLog = AuditLog.createNew(
                idGenerator.generate(),
                command.groupId(),
                AuditAction.CREATED,
                AuditEntityType.PAYMENT,
                savedPayment.getId(),
                Instant.now(clock)
        );

        auditLogRepository.save(auditLog);

        PaymentResponse response = paymentApiMapper.toResponse(
                savedPayment,
                fromParticipant,
                toParticipant
        );

        idempotencyService.complete(
                idempotency,
                HttpStatus.CREATED.value(),
                response
        );

        return new PaymentResult(
                response,
                false
        );
    }
}
