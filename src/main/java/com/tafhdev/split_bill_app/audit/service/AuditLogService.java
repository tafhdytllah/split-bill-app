package com.tafhdev.split_bill_app.audit.service;

import com.tafhdev.split_bill_app.audit.domain.AuditLog;
import com.tafhdev.split_bill_app.audit.repository.AuditLogRepository;
import com.tafhdev.split_bill_app.audit.service.dto.command.GetAuditLogCommand;
import com.tafhdev.split_bill_app.audit.service.dto.result.AuditLogItemResult;
import com.tafhdev.split_bill_app.audit.service.dto.result.AuditLogResult;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final BillGroupRepository billGroupRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            BillGroupRepository billGroupRepository
    ) {
        this.auditLogRepository = auditLogRepository;
        this.billGroupRepository = billGroupRepository;
    }

    @Transactional(readOnly = true)
    public AuditLogResult getAuditLog(GetAuditLogCommand command) {

        BillGroup group = billGroupRepository.findById(command.groupId())
                .orElseThrow(() -> new DomainException("group not found"));

        List<AuditLog> auditLogs = auditLogRepository.findByGroupId(command.groupId());

        return toResult(
                command.groupId(),
                auditLogs
        );
    }

    private AuditLogResult toResult(
            UUID groupId,
            List<AuditLog> auditLogs
    ) {

        List<AuditLogItemResult> items = auditLogs.stream()
                .map(auditLog -> new AuditLogItemResult(
                        auditLog.getId(),
                        auditLog.getAction(),
                        auditLog.getEntityType(),
                        auditLog.getEntityId(),
                        auditLog.getCreatedAt()
                ))
                .toList();

        return new AuditLogResult(
                groupId,
                items
        );
    }


}
