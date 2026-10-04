package com.tafhdev.split_bill_app.payment.service.dto;

import com.tafhdev.split_bill_app.shared.domain.Money;

import java.util.UUID;

public record CreatePaymentCommand(

        String idempotencyKey,
        UUID groupId,
        UUID fromParticipantId,
        UUID toParticipantId,
        Money amount
) {
}
