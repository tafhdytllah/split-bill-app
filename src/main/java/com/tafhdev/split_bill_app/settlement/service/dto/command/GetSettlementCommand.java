package com.tafhdev.split_bill_app.settlement.service.dto.command;

import java.util.UUID;

public record GetSettlementCommand(

        UUID groupId
) {
}
