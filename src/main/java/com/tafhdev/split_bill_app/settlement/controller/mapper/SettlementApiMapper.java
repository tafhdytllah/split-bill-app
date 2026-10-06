package com.tafhdev.split_bill_app.settlement.controller.mapper;

import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.settlement.controller.dto.BalanceItemResponse;
import com.tafhdev.split_bill_app.settlement.controller.dto.SettlementItemResponse;
import com.tafhdev.split_bill_app.settlement.controller.dto.SettlementResponse;
import com.tafhdev.split_bill_app.settlement.domain.Balance;
import com.tafhdev.split_bill_app.settlement.domain.Settlement;
import com.tafhdev.split_bill_app.settlement.service.dto.GetSettlementCommand;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SettlementApiMapper {

    public GetSettlementCommand toCommand(UUID groupId) {
        return new GetSettlementCommand(groupId);
    }

    public SettlementResponse toResponse(
            BillGroup group,
            List<Balance> balances,
            List<Settlement> settlements
    ) {

        List<BalanceItemResponse> balanceItemResponses = balances.stream()
                .map(this::toBalanceItemResponse)
                .toList();

        List<SettlementItemResponse> settlementItemResponses = settlements.stream()
                .map(this::toSettlementItemResponse)
                .toList();

        return new SettlementResponse(
                group.getId(),
                balanceItemResponses,
                settlementItemResponses
        );
    }

    private BalanceItemResponse toBalanceItemResponse(Balance item) {
        return new BalanceItemResponse(
                item.getParticipantId(),
                item.getAmount().value()
        );
    }

    private SettlementItemResponse toSettlementItemResponse(Settlement item) {
        return new SettlementItemResponse(
                item.getFromParticipantId(),
                item.getToParticipantId(),
                item.getAmount().value()
        );
    }
}
