package com.tafhdev.split_bill_app.expense.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ExactSplitRequest(
        @NotNull UUID participantId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount
) {
}
