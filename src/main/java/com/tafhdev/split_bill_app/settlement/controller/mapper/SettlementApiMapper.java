package com.tafhdev.split_bill_app.settlement.controller.mapper;

import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.settlement.controller.dto.SettlementItemResponse;
import com.tafhdev.split_bill_app.settlement.controller.dto.SettlementResponse;
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
            List<Settlement> settlements
    ) {

        List<SettlementItemResponse> items = settlements.stream()
                .map(this::toItemResponse)
                .toList();

        return new SettlementResponse(
                group.getId(),
                items
        );
    }

    private SettlementItemResponse toItemResponse(Settlement item) {
        return new SettlementItemResponse(
                item.getFromParticipantId(),
                item.getToParticipantId(),
                item.getAmount().value()
        );
    }
}
