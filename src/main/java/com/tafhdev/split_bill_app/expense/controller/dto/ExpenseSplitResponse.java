package com.tafhdev.split_bill_app.expense.controller.dto;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseSplitResponse(

        UUID id,

        UUID participantId,

        String name,

        @JsonSerialize(using = ToStringSerializer.class)
        BigDecimal amount
) {
}
