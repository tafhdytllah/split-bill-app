package com.tafhdev.split_bill_app.payment.persistence.repository;

import com.tafhdev.split_bill_app.payment.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

    List<PaymentEntity> findByGroup_Id(UUID groupId);
}
