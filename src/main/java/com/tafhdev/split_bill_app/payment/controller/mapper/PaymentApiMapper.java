package com.tafhdev.split_bill_app.payment.controller.mapper;

import com.tafhdev.split_bill_app.payment.controller.dto.request.CreatePaymentRequest;
import com.tafhdev.split_bill_app.payment.controller.dto.response.ParticipantResponse;
import com.tafhdev.split_bill_app.payment.controller.dto.response.PaymentResponse;
import com.tafhdev.split_bill_app.payment.service.dto.command.CreatePaymentCommand;
import com.tafhdev.split_bill_app.payment.service.dto.result.PaymentResult;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentApiMapper {

    public CreatePaymentCommand toCommand(
            UUID groupId,
            CreatePaymentRequest request
    ) {
        return new CreatePaymentCommand(
                groupId,
                request.fromParticipantId(),
                request.toParticipantId(),
                Money.of(request.amount())
        );
    }

    public PaymentResponse toResponse(PaymentResult result) {
        return new PaymentResponse(
                result.id(),
                result.groupId(),
                new ParticipantResponse(
                        result.fromParticipant().id(),
                        result.fromParticipant().name()
                ),
                new ParticipantResponse(
                        result.toParticipant().id(),
                        result.toParticipant().name()
                ),
                result.amount(),
                result.createdAt()
        );
    }
}
