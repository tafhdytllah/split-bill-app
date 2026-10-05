package com.tafhdev.split_bill_app.audit.service;

import com.tafhdev.split_bill_app.audit.controller.dto.AuditLogResponse;
import com.tafhdev.split_bill_app.audit.controller.mapper.AuditLogApiMapper;
import com.tafhdev.split_bill_app.audit.domain.AuditLog;
import com.tafhdev.split_bill_app.audit.repository.AuditLogRepository;
import com.tafhdev.split_bill_app.audit.service.dto.GetAuditLogCommand;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.shared.application.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final BillGroupRepository billGroupRepository;
    private final AuditLogApiMapper auditLogApiMapper;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            BillGroupRepository billGroupRepository,
            AuditLogApiMapper auditLogApiMapper
    ) {
        this.auditLogRepository = auditLogRepository;
        this.billGroupRepository = billGroupRepository;
        this.auditLogApiMapper = auditLogApiMapper;
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLog(GetAuditLogCommand command) {

        BillGroup group = billGroupRepository.findById(command.groupId())
                .orElseThrow(() -> new ResourceNotFoundException("group not found"));

        List<AuditLog> auditLogs = auditLogRepository.findByGroupId(command.groupId());

        return auditLogApiMapper.toResponse(
                group,
                auditLogs
        );
    }

}
