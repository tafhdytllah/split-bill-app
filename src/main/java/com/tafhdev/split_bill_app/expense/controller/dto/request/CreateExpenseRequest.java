package com.tafhdev.split_bill_app.expense.controller.dto.request;

import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.SplitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateExpenseRequest(

        @NotNull
        UUID paidBy,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        @NotNull
        ExpenseCategory category,

        @NotNull
        SplitType splitType,

        List<UUID> participants,

        List<ExactSplitRequest> exactSplits,

        List<PercentageSplitRequest> percentageSplits
) {
}