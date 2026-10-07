package com.tafhdev.split_bill_app.audit.persistence.repository;

import com.tafhdev.split_bill_app.audit.domain.AuditLog;
import com.tafhdev.split_bill_app.audit.persistence.entity.AuditLogEntity;
import com.tafhdev.split_bill_app.audit.persistence.mapper.AuditLogMapper;
import com.tafhdev.split_bill_app.audit.repository.AuditLogRepository;
import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import com.tafhdev.split_bill_app.group.persistence.repository.BillGroupJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogJpaRepository auditLogJpaRepository;
    private final BillGroupJpaRepository billGroupJpaRepository;
    private final AuditLogMapper auditLogMapper;

    public AuditLogRepositoryImpl(
            AuditLogJpaRepository auditLogJpaRepository,
            BillGroupJpaRepository billGroupJpaRepository,
            AuditLogMapper auditLogMapper
    ) {
        this.auditLogJpaRepository = auditLogJpaRepository;
        this.billGroupJpaRepository = billGroupJpaRepository;
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {

        BillGroupEntity groupEntity = billGroupJpaRepository.getReferenceById(auditLog.getGroupId());

        AuditLogEntity auditLogEntity = auditLogMapper.toEntity(
                auditLog,
                groupEntity
        );

        AuditLogEntity savedAuditLog = auditLogJpaRepository.save(auditLogEntity);

        return auditLogMapper.toDomain(savedAuditLog);
    }

    @Override
    public List<AuditLog> findByGroupId(UUID groupId) {
        return auditLogJpaRepository.findByGroup_IdOrderByCreatedAtDesc(groupId).stream()
                .map(auditLogMapper::toDomain)
                .toList();
    }
}
