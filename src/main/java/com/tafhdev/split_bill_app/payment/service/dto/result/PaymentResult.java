package com.tafhdev.split_bill_app.payment.service.dto.result;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResult(

        UUID id,
        UUID groupId,
        ParticipantResult fromParticipant,
        ParticipantResult toParticipant,
        BigDecimal amount,
        Instant createdAt
) {
}
