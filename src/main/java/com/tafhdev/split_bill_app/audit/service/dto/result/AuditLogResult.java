package com.tafhdev.split_bill_app.audit.service.dto.result;

import com.tafhdev.split_bill_app.audit.domain.AuditAction;
import com.tafhdev.split_bill_app.audit.domain.AuditEntityType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuditLogResult(

        UUID groupId,

        List<AuditLogItemResult> auditLogs
) {
}
