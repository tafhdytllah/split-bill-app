package com.tafhdev.split_bill_app.settlement.controller.dto;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.util.UUID;

public record SettlementItemResponse(

        UUID fromParticipantId,

        UUID toParticipantId,

        @JsonSerialize(using = ToStringSerializer.class)
        BigDecimal amount
) {
}
