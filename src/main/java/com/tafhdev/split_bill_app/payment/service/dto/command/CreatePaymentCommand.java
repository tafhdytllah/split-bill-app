package com.tafhdev.split_bill_app.payment.service.dto.command;

import com.tafhdev.split_bill_app.shared.domain.Money;

import java.util.UUID;

public record CreatePaymentCommand(

        UUID groupId,
        UUID fromParticipantId,
        UUID toParticipantId,
        Money amount
) {
}
