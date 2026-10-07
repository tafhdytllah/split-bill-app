package com.tafhdev.split_bill_app.settlement.persistence.projection;

import java.math.BigDecimal;
import java.util.UUID;

public record SettlementBalanceProjection(

        UUID participantId,

        String name,

        BigDecimal totalPaid,

        BigDecimal totalOwed,

        BigDecimal totalSent,

        BigDecimal totalReceived,

        BigDecimal balance
) {
}
