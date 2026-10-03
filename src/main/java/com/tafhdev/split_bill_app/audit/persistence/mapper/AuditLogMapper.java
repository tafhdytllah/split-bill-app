package com.tafhdev.split_bill_app.audit.persistence.mapper;

import com.tafhdev.split_bill_app.audit.domain.AuditAction;
import com.tafhdev.split_bill_app.audit.domain.AuditEntityType;
import com.tafhdev.split_bill_app.audit.domain.AuditLog;
import com.tafhdev.split_bill_app.audit.persistence.entity.AuditLogEntity;
import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogEntity toEntity(
            AuditLog domain,
            BillGroupEntity groupEntity
    ) {
        return new AuditLogEntity(
                domain.getId(),
                groupEntity,
                domain.getAction().name(),
                domain.getEntityType().name(),
                domain.getEntityId(),
                domain.getCreatedAt()
        );
    }

    public AuditLog toDomain(AuditLogEntity entity) {
        return AuditLog.reconstitute(
                entity.getId(),
                entity.getGroup().getId(),
                AuditAction.valueOf(entity.getAction()),
                AuditEntityType.valueOf(entity.getEntityType()),
                entity.getEntityId(),
                entity.getCreatedAt()
        );
    }
}
