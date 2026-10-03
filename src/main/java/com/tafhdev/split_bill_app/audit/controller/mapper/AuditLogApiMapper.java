package com.tafhdev.split_bill_app.audit.controller.mapper;

import com.tafhdev.split_bill_app.audit.controller.dto.response.AuditLogItemResponse;
import com.tafhdev.split_bill_app.audit.controller.dto.response.AuditLogResponse;
import com.tafhdev.split_bill_app.audit.service.dto.command.GetAuditLogCommand;
import com.tafhdev.split_bill_app.audit.service.dto.result.AuditLogItemResult;
import com.tafhdev.split_bill_app.audit.service.dto.result.AuditLogResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AuditLogApiMapper {

    public GetAuditLogCommand toCommand(UUID groupId) {
        return new GetAuditLogCommand(groupId);
    }

    public AuditLogResponse toResponse(AuditLogResult result) {

        List<AuditLogItemResponse> itemResults = result.auditLogs().stream()
                .map(this::toItemResponse)
                .toList();

        return new AuditLogResponse(
                result.groupId(),
                itemResults
        );
    }

    private AuditLogItemResponse toItemResponse(AuditLogItemResult itemResult) {
        return new AuditLogItemResponse(
                itemResult.id(),
                itemResult.action(),
                itemResult.entityType(),
                itemResult.entityId(),
                itemResult.createdAt()
        );
    }
}
