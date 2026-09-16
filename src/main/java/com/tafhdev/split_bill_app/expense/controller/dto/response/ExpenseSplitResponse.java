package com.tafhdev.split_bill_app.expense.controller.dto.response;

import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseSplitResponse(
        UUID id,
        UUID participantId,

        @JsonSerialize(using = ToStringSerializer.class)
        BigDecimal amount
) {

    public static ExpenseSplitResponse from(
            ExpenseSplit expenseSplit
    ) {
        return new ExpenseSplitResponse(
                expenseSplit.getId(),
                expenseSplit.getParticipantId(),
                expenseSplit.getAmount().value()
        );
    }
}
