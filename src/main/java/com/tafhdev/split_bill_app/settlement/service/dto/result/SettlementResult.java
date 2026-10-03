package com.tafhdev.split_bill_app.settlement.service.dto.result;

import java.util.List;
import java.util.UUID;

public record SettlementResult(

        UUID groupId,

        List<SettlementItemResult> settlements
) {
}
