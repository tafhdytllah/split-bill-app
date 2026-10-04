package com.tafhdev.split_bill_app.expense.service.dto;

import com.tafhdev.split_bill_app.shared.domain.Money;

import java.math.BigDecimal;
import java.util.UUID;

public record SplitParticipantCommand(

        UUID participantId,
        Money amount,
        BigDecimal percentage
) {
}
