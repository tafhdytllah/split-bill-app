package com.tafhdev.split_bill_app.settlement.service.dto.result;

import java.math.BigDecimal;
import java.util.UUID;

public record SettlementItemResult(

        UUID fromParticipantId,

        UUID toParticipantId,

        BigDecimal amount
) {
}
