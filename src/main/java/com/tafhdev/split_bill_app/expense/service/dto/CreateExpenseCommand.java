package com.tafhdev.split_bill_app.expense.service.dto;

import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.shared.domain.Money;

import java.util.UUID;

public record CreateExpenseCommand(

        String idempotencyKey,

        UUID groupId,

        UUID paidByParticipantId,

        Money amount,

        ExpenseCategory category,

        SplitCommand split
) {
}
