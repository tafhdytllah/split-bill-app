package com.tafhdev.split_bill_app.expense.domain.split;

import java.math.BigDecimal;
import java.util.UUID;

public record PercentageSplit(
        UUID participantId,
        BigDecimal percentage
) {
}
