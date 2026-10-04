package com.tafhdev.split_bill_app.audit.controller.dto;

import com.tafhdev.split_bill_app.audit.domain.AuditAction;
import com.tafhdev.split_bill_app.audit.domain.AuditEntityType;

import java.time.Instant;
import java.util.UUID;

public record AuditLogItemResponse(

        UUID id,
        AuditAction action,
        AuditEntityType entityType,
        UUID entityId,
        Instant createdAt
) {
}
