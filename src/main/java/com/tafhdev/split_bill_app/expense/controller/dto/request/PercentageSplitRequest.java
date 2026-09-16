package com.tafhdev.split_bill_app.expense.controller.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record PercentageSplitRequest(
        UUID participantId,
        BigDecimal percentage
) {
}
