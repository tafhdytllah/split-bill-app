package com.tafhdev.split_bill_app.payment.controller.dto;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(

        UUID id,

        UUID groupId,

        ParticipantResponse fromParticipant,

        ParticipantResponse toParticipant,

        @JsonSerialize(using = ToStringSerializer.class)
        BigDecimal amount,

        Instant createdAt
) {
}
