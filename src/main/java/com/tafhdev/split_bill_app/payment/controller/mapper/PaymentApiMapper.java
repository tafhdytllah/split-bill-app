package com.tafhdev.split_bill_app.payment.controller.mapper;

import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.payment.controller.dto.CreatePaymentRequest;
import com.tafhdev.split_bill_app.payment.controller.dto.ParticipantResponse;
import com.tafhdev.split_bill_app.payment.controller.dto.PaymentResponse;
import com.tafhdev.split_bill_app.payment.domain.Payment;
import com.tafhdev.split_bill_app.payment.service.dto.CreatePaymentCommand;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentApiMapper {

    public CreatePaymentCommand toCommand(
            String idempotencyKey,
            UUID groupId,
            CreatePaymentRequest request
    ) {
        return new CreatePaymentCommand(
                idempotencyKey,
                groupId,
                request.fromParticipantId(),
                request.toParticipantId(),
                Money.of(request.amount())
        );
    }

    public PaymentResponse toResponse(
            Payment payment,
            Participant fromParticipant,
            Participant toParticipant
    ) {
        return new PaymentResponse(
                payment.getId(),
                payment.getGroupId(),
                new ParticipantResponse(
                        fromParticipant.getId(),
                        fromParticipant.getName()
                ),
                new ParticipantResponse(
                        toParticipant.getId(),
                        toParticipant.getName()
                ),
                payment.getAmount().value(),
                payment.getCreatedAt()
        );
    }
}
