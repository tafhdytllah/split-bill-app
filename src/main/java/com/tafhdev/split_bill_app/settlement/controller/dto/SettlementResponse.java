package com.tafhdev.split_bill_app.settlement.controller.dto;

import java.util.List;
import java.util.UUID;

public record SettlementResponse(

        UUID groupId,

        List<BalanceItemResponse> balances,

        List<SettlementItemResponse> settlements
) {
}
