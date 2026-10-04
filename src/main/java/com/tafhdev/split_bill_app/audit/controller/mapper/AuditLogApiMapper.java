package com.tafhdev.split_bill_app.audit.controller.mapper;

import com.tafhdev.split_bill_app.audit.controller.dto.AuditLogItemResponse;
import com.tafhdev.split_bill_app.audit.controller.dto.AuditLogResponse;
import com.tafhdev.split_bill_app.audit.domain.AuditLog;
import com.tafhdev.split_bill_app.audit.service.dto.GetAuditLogCommand;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AuditLogApiMapper {

    public GetAuditLogCommand toCommand(UUID groupId) {
        return new GetAuditLogCommand(groupId);
    }

    public AuditLogResponse toResponse(
            BillGroup group,
            List<AuditLog> auditLogs
    ) {

        List<AuditLogItemResponse> itemResults = auditLogs.stream()
                .map(this::toItemResponse)
                .toList();

        return new AuditLogResponse(
                group.getId(),
                itemResults
        );
    }

    private AuditLogItemResponse toItemResponse(AuditLog item) {
        return new AuditLogItemResponse(
                item.getId(),
                item.getAction(),
                item.getEntityType(),
                item.getEntityId(),
                item.getCreatedAt()
        );
    }
}
