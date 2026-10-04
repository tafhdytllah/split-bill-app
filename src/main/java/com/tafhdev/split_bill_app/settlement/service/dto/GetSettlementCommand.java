package com.tafhdev.split_bill_app.settlement.service.dto;

import java.util.UUID;

public record GetSettlementCommand(

        UUID groupId
) {
}
