package com.tafhdev.split_bill_app.payment.controller.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(

        UUID id,

        UUID groupId,

        ParticipantResponse fromParticipant,

        ParticipantResponse toParticipant,

        BigDecimal amount,

        Instant createdAt
) {
}
