package com.tafhdev.split_bill_app.expense.domain.split;

import com.tafhdev.split_bill_app.shared.domain.Money;

import java.util.UUID;

public record ExactSplit(
        UUID participantId,
        Money amount
) {
}
