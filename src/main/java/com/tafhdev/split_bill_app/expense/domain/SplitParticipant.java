package com.tafhdev.split_bill_app.expense.domain;

import com.tafhdev.split_bill_app.shared.domain.Money;

import java.math.BigDecimal;
import java.util.UUID;

public record SplitParticipant(

        UUID participantId,
        Money amount,
        BigDecimal percentage
) {
}
