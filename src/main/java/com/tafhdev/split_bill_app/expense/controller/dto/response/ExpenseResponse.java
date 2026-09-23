package com.tafhdev.split_bill_app.expense.controller.dto.response;

import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.SplitType;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExpenseResponse(

        UUID id,
        UUID groupId,
        UUID paidBy,

        @JsonSerialize(using = ToStringSerializer.class)
        BigDecimal amount,

        ExpenseCategory category,
        SplitType splitType,
        List<ExpenseSplitResponse> splits,
        Instant createdAt
) {
}
