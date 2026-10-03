package com.tafhdev.split_bill_app.audit.controller.dto.response;

import java.util.List;
import java.util.UUID;

public record AuditLogResponse(

        UUID groupId,

        List<AuditLogItemResponse> auditLogs
) {
}
