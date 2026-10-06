package com.tafhdev.split_bill_app.settlement.controller;

import com.tafhdev.split_bill_app.settlement.controller.dto.SettlementResponse;
import com.tafhdev.split_bill_app.settlement.controller.mapper.SettlementApiMapper;
import com.tafhdev.split_bill_app.settlement.service.SettlementService;
import com.tafhdev.split_bill_app.settlement.service.dto.GetSettlementCommand;
import com.tafhdev.split_bill_app.shared.infrastructure.web.response.ApiResponse;
import com.tafhdev.split_bill_app.shared.infrastructure.web.response.ResponseFactory;
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
    public ResponseEntity<ApiResponse<SettlementResponse>> getSettlement(
            @PathVariable UUID groupId
    ) {
        GetSettlementCommand command = settlementApiMapper.toCommand(groupId);

        SettlementResponse response = settlementService.getSettlement(command);

        return ResponseFactory.ok(response);
    }
}
