package com.tafhdev.split_bill_app.expense.persistence.projection;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExpenseProjection(
        UUID expenseId,
        UUID groupId,
        UUID paidBy,
        BigDecimal amount,
        String category,
        String splitType,
        Instant createdAt,
        UUID splitId,
        UUID participantId,
        BigDecimal splitAmount
) {
}
