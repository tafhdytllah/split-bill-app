package com.tafhdev.split_bill_app.audit.controller;

import com.tafhdev.split_bill_app.audit.controller.dto.AuditLogItemResponse;
import com.tafhdev.split_bill_app.audit.controller.dto.AuditLogResponse;
import com.tafhdev.split_bill_app.audit.controller.mapper.AuditLogApiMapper;
import com.tafhdev.split_bill_app.audit.domain.AuditAction;
import com.tafhdev.split_bill_app.audit.domain.AuditEntityType;
import com.tafhdev.split_bill_app.audit.service.AuditLogService;
import com.tafhdev.split_bill_app.audit.service.dto.GetAuditLogCommand;
import com.tafhdev.split_bill_app.shared.application.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private AuditLogApiMapper auditLogApiMapper;

    @Test
    void shouldGetAuditLog() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        GetAuditLogCommand command =
                new GetAuditLogCommand(groupId);

        AuditLogResponse response =
                new AuditLogResponse(
                        groupId,
                        List.of(
                                new AuditLogItemResponse(
                                        UUID.randomUUID(),
                                        AuditAction.CREATED,
                                        AuditEntityType.EXPENSE,
                                        expenseId,
                                        Instant.parse("2026-10-07T10:00:00Z")
                                ),
                                new AuditLogItemResponse(
                                        UUID.randomUUID(),
                                        AuditAction.CREATED,
                                        AuditEntityType.PAYMENT,
                                        paymentId,
                                        Instant.parse("2026-10-07T10:05:00Z")
                                )
                        )
                );

        when(auditLogApiMapper.toCommand(groupId))
                .thenReturn(command);

        when(auditLogService.getAuditLog(command))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/groups/{groupId}/audit", groupId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.data.auditLogs")
                        .isArray())
                .andExpect(jsonPath("$.data.auditLogs[0].action")
                        .value("CREATED"))
                .andExpect(jsonPath("$.data.auditLogs[0].entityType")
                        .value("EXPENSE"))
                .andExpect(jsonPath("$.data.auditLogs[0].entityId")
                        .value(expenseId.toString()))
                .andExpect(jsonPath("$.data.auditLogs[1].action")
                        .value("CREATED"))
                .andExpect(jsonPath("$.data.auditLogs[1].entityType")
                        .value("PAYMENT"))
                .andExpect(jsonPath("$.data.auditLogs[1].entityId")
                        .value(paymentId.toString()));

        verify(auditLogApiMapper)
                .toCommand(groupId);

        verify(auditLogService)
                .getAuditLog(command);
    }

    @Test
    void shouldReturnEmptyAuditLogs() throws Exception {

        UUID groupId = UUID.randomUUID();

        GetAuditLogCommand command =
                new GetAuditLogCommand(groupId);

        AuditLogResponse response =
                new AuditLogResponse(
                        groupId,
                        List.of()
                );

        when(auditLogApiMapper.toCommand(groupId))
                .thenReturn(command);

        when(auditLogService.getAuditLog(command))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/groups/{groupId}/audit", groupId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.data.auditLogs")
                        .isArray());

        verify(auditLogApiMapper)
                .toCommand(groupId);

        verify(auditLogService)
                .getAuditLog(command);
    }

    @Test
    void shouldReturnNotFoundWhenGroupDoesNotExist() throws Exception {

        UUID groupId = UUID.randomUUID();

        GetAuditLogCommand command =
                new GetAuditLogCommand(groupId);

        when(auditLogApiMapper.toCommand(groupId))
                .thenReturn(command);

        when(auditLogService.getAuditLog(command))
                .thenThrow(
                        new ResourceNotFoundException("group not found")
                );

        mockMvc.perform(
                        get("/api/groups/{groupId}/audit", groupId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.code")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.errors.message")
                        .value("group not found"));

        verify(auditLogApiMapper)
                .toCommand(groupId);

        verify(auditLogService)
                .getAuditLog(command);
    }

    @Test
    void shouldReturnBadRequestWhenGroupIdIsInvalid() throws Exception {

        mockMvc.perform(
                        get("/api/groups/{groupId}/audit", "invalid-uuid")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code")
                        .value("BAD_REQUEST"));

        verifyNoInteractions(auditLogApiMapper);
        verifyNoInteractions(auditLogService);
    }
}