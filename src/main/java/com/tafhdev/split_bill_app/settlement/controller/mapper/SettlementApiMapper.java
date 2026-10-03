package com.tafhdev.split_bill_app.settlement.controller.mapper;

import com.tafhdev.split_bill_app.settlement.controller.dto.response.SettlementItemResponse;
import com.tafhdev.split_bill_app.settlement.controller.dto.response.SettlementResponse;
import com.tafhdev.split_bill_app.settlement.service.dto.command.GetSettlementCommand;
import com.tafhdev.split_bill_app.settlement.service.dto.result.SettlementItemResult;
import com.tafhdev.split_bill_app.settlement.service.dto.result.SettlementResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SettlementApiMapper {

    public GetSettlementCommand toCommand(UUID groupId) {
        return new GetSettlementCommand(groupId);
    }

    public SettlementResponse toResponse(SettlementResult result) {

        List<SettlementItemResponse> settlements = result.settlements().stream()
                .map(this::toItemResponse)
                .toList();

        return new SettlementResponse(
                result.groupId(),
                settlements
        );
    }

    private SettlementItemResponse toItemResponse(SettlementItemResult result) {
        return new SettlementItemResponse(
                result.fromParticipantId(),
                result.toParticipantId(),
                result.amount()
        );
    }
}
