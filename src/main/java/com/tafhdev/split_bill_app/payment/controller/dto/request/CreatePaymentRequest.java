package com.tafhdev.split_bill_app.payment.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(

        @NotNull
        UUID fromParticipantId,

        @NotNull
        UUID toParticipantId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount
) {
}
