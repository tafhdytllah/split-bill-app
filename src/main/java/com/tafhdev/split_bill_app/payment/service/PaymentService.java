package com.tafhdev.split_bill_app.payment.service;

import com.tafhdev.split_bill_app.audit.domain.AuditAction;
import com.tafhdev.split_bill_app.audit.domain.AuditEntityType;
import com.tafhdev.split_bill_app.audit.domain.AuditLog;
import com.tafhdev.split_bill_app.audit.repository.AuditLogRepository;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.payment.domain.Payment;
import com.tafhdev.split_bill_app.payment.repository.PaymentRepository;
import com.tafhdev.split_bill_app.payment.service.dto.command.CreatePaymentCommand;
import com.tafhdev.split_bill_app.payment.service.dto.result.ParticipantResult;
import com.tafhdev.split_bill_app.payment.service.dto.result.PaymentResult;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillGroupRepository billGroupRepository;
    private final AuditLogRepository auditLogRepository;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public PaymentService(
            PaymentRepository paymentRepository,
            BillGroupRepository billGroupRepository,
            AuditLogRepository auditLogRepository,
            IdGenerator idGenerator,
            Clock clock
    ) {
        this.paymentRepository = paymentRepository;
        this.billGroupRepository = billGroupRepository;
        this.auditLogRepository = auditLogRepository;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Transactional
    public PaymentResult createPayment(CreatePaymentCommand command) {

        BillGroup group = billGroupRepository.findById(command.groupId())
                .orElseThrow(() -> new DomainException("group not found"));

        Participant fromParticipant = group.requireParticipant(command.fromParticipantId());

        Participant toParticipant = group.requireParticipant(command.toParticipantId());

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

        return toResult(
                savedPayment,
                fromParticipant,
                toParticipant
        );
    }

    private PaymentResult toResult(
            Payment payment,
            Participant fromParticipant,
            Participant toParticipant
    ) {
        return new PaymentResult(
                payment.getId(),
                payment.getGroupId(),
                new ParticipantResult(
                        fromParticipant.getId(),
                        fromParticipant.getName()
                ),
                new ParticipantResult(
                        toParticipant.getId(),
                        toParticipant.getName()
                ),
                payment.getAmount().value(),
                payment.getCreatedAt()
        );
    }
}
