package com.tafhdev.split_bill_app.expense.controller.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SplitParticipantRequest(

        @NotNull UUID participantId,

        @DecimalMin(value = "0.01")
        BigDecimal amount,

        @DecimalMin(value = "0.01")
        @DecimalMax(value = "100.00")
        BigDecimal percentage
) {
}
