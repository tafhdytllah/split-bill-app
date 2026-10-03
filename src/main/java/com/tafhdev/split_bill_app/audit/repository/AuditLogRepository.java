package com.tafhdev.split_bill_app.audit.repository;

import com.tafhdev.split_bill_app.audit.domain.AuditLog;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    List<AuditLog> findByGroupId(UUID groupId);
}
