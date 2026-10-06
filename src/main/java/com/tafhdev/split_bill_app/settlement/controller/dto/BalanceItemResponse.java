package com.tafhdev.split_bill_app.settlement.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceItemResponse(

        UUID participantId,

        BigDecimal amount
) {
}
