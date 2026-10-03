package com.tafhdev.split_bill_app.audit.persistence.repository;

import com.tafhdev.split_bill_app.audit.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, UUID> {

    List<AuditLogEntity> findByGroup_Id(UUID groupId);
}
