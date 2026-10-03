package com.tafhdev.split_bill_app.settlement.controller;

import com.tafhdev.split_bill_app.settlement.controller.dto.response.SettlementResponse;
import com.tafhdev.split_bill_app.settlement.controller.mapper.SettlementApiMapper;
import com.tafhdev.split_bill_app.settlement.service.SettlementService;
import com.tafhdev.split_bill_app.settlement.service.dto.command.GetSettlementCommand;
import com.tafhdev.split_bill_app.settlement.service.dto.result.SettlementResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController()
@RequestMapping("/api/groups/{groupId}/settlements")
public class SettlementController {

    private final SettlementService settlementService;
    private final SettlementApiMapper settlementApiMapper;

    public SettlementController(
            SettlementService settlementService,
            SettlementApiMapper settlementApiMapper
    ) {
        this.settlementService = settlementService;
        this.settlementApiMapper = settlementApiMapper;
    }

    @GetMapping
    public ResponseEntity<SettlementResponse> getSettlement(
            @PathVariable UUID groupId
    ) {
        GetSettlementCommand command = settlementApiMapper.toCommand(groupId);

        SettlementResult result = settlementService.getSettlement(command);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(settlementApiMapper.toResponse(result));
    }
}
