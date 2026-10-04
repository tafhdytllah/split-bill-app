package com.tafhdev.split_bill_app.payment.service;

import com.tafhdev.split_bill_app.audit.domain.AuditAction;
import com.tafhdev.split_bill_app.audit.domain.AuditEntityType;
import com.tafhdev.split_bill_app.audit.domain.AuditLog;
import com.tafhdev.split_bill_app.audit.repository.AuditLogRepository;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.payment.controller.dto.PaymentResponse;
import com.tafhdev.split_bill_app.payment.controller.mapper.PaymentApiMapper;
import com.tafhdev.split_bill_app.payment.domain.Payment;
import com.tafhdev.split_bill_app.payment.repository.PaymentRepository;
import com.tafhdev.split_bill_app.payment.service.dto.CreatePaymentCommand;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyHashGenerator;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyRequestBuilder;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyService;
import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillGroupRepository billGroupRepository;
    private final AuditLogRepository auditLogRepository;
    private final IdempotencyHashGenerator idempotencyHashGenerator;
    private final IdempotencyService idempotencyService;
    private final IdGenerator idGenerator;
    private final Clock clock;
    private final PaymentApiMapper paymentApiMapper;

    public PaymentService(
            PaymentRepository paymentRepository,
            BillGroupRepository billGroupRepository,
            AuditLogRepository auditLogRepository,
            IdempotencyHashGenerator idempotencyHashGenerator,
            IdempotencyService idempotencyService,
            IdGenerator idGenerator,
            Clock clock,
            PaymentApiMapper paymentApiMapper
    ) {
        this.paymentRepository = paymentRepository;
        this.billGroupRepository = billGroupRepository;
        this.auditLogRepository = auditLogRepository;
        this.idempotencyHashGenerator = idempotencyHashGenerator;
        this.idempotencyService = idempotencyService;
        this.idGenerator = idGenerator;
        this.clock = clock;
        this.paymentApiMapper = paymentApiMapper;
    }

    @Transactional
    public PaymentResponse createPayment(CreatePaymentCommand command) {

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
                throw new DomainException(
                        "idempotency key reused with different request"
                );
            }

            return idempotencyService.getResponse(
                    idempotency,
                    PaymentResponse.class
            );
        }

        BillGroup group = billGroupRepository.findById(command.groupId())
                .orElseThrow(() -> new DomainException("group not found"));

        Participant fromParticipant = group.requireParticipant(command.fromParticipantId());

        Participant toParticipant = group.requireParticipant(command.toParticipantId());

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

        return response;
    }
}
