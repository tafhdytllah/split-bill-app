package com.tafhdev.split_bill_app.settlement.controller;

import com.tafhdev.split_bill_app.settlement.controller.dto.SettlementResponse;
import com.tafhdev.split_bill_app.settlement.controller.mapper.SettlementApiMapper;
import com.tafhdev.split_bill_app.settlement.service.SettlementService;
import com.tafhdev.split_bill_app.settlement.service.dto.GetSettlementCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettlementController.class)
@AutoConfigureMockMvc(addFilters = false)
class SettlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SettlementService settlementService;

    @MockitoBean
    private SettlementApiMapper settlementApiMapper;

    @Test
    void shouldGetSettlement() throws Exception {
        UUID groupId = UUID.randomUUID();

        GetSettlementCommand command =
                new GetSettlementCommand(groupId);

        SettlementResponse response =
                mock(SettlementResponse.class);

        when(settlementApiMapper.toCommand(groupId))
                .thenReturn(command);

        when(settlementService.getSettlement(command))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/groups/{groupId}/settlements", groupId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());

        verify(settlementApiMapper)
                .toCommand(groupId);

        verify(settlementService)
                .getSettlement(command);
    }

    @Test
    void shouldReturnNotFoundWhenGroupDoesNotExist() throws Exception {
        UUID groupId = UUID.randomUUID();

        GetSettlementCommand command =
                new GetSettlementCommand(groupId);

        when(settlementApiMapper.toCommand(groupId))
                .thenReturn(command);

        when(settlementService.getSettlement(command))
                .thenThrow(new com.tafhdev.split_bill_app.shared.application.exception.ResourceNotFoundException(
                        "group not found"
                ));

        mockMvc.perform(
                        get("/api/groups/{groupId}/settlements", groupId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.code")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.errors.message")
                        .value("group not found"));

        verify(settlementApiMapper)
                .toCommand(groupId);

        verify(settlementService)
                .getSettlement(command);
    }

    @Test
    void shouldRejectInvalidGroupId() throws Exception {

        mockMvc.perform(
                        get("/api/groups/{groupId}/settlements", "invalid-uuid")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code")
                        .value("BAD_REQUEST"));

        verifyNoInteractions(
                settlementApiMapper,
                settlementService
        );
    }
}