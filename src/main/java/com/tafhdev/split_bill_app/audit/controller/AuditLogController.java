package com.tafhdev.split_bill_app.audit.controller;

import com.tafhdev.split_bill_app.audit.controller.dto.AuditLogResponse;
import com.tafhdev.split_bill_app.audit.controller.mapper.AuditLogApiMapper;
import com.tafhdev.split_bill_app.audit.service.AuditLogService;
import com.tafhdev.split_bill_app.audit.service.dto.GetAuditLogCommand;
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
@RequestMapping("/api/groups/{groupId}/audit")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final AuditLogApiMapper auditLogApiMapper;

    public AuditLogController(
            AuditLogService auditLogService,
            AuditLogApiMapper auditLogApiMapper
    ) {
        this.auditLogService = auditLogService;
        this.auditLogApiMapper = auditLogApiMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLog(
            @PathVariable UUID groupId
    ) {

        GetAuditLogCommand command = auditLogApiMapper.toCommand(groupId);

        AuditLogResponse response = auditLogService.getAuditLog(command);

        return ResponseFactory.ok(response);
    }

}
