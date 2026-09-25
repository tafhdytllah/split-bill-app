package com.tafhdev.split_bill_app.payment.persistence.repository;

import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import com.tafhdev.split_bill_app.group.persistence.entity.ParticipantEntity;
import com.tafhdev.split_bill_app.group.persistence.repository.BillGroupJpaRepository;
import com.tafhdev.split_bill_app.group.persistence.repository.ParticipantJpaRepository;
import com.tafhdev.split_bill_app.payment.domain.Payment;
import com.tafhdev.split_bill_app.payment.persistence.entity.PaymentEntity;
import com.tafhdev.split_bill_app.payment.persistence.mapper.PaymentMapper;
import com.tafhdev.split_bill_app.payment.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final BillGroupJpaRepository billGroupJpaRepository;
    private final ParticipantJpaRepository participantJpaRepository;
    private final PaymentMapper paymentMapper;

    public PaymentRepositoryImpl(
            PaymentJpaRepository paymentJpaRepository,
            BillGroupJpaRepository billGroupJpaRepository,
            ParticipantJpaRepository participantJpaRepository,
            PaymentMapper paymentMapper
    ) {
        this.paymentJpaRepository = paymentJpaRepository;
        this.billGroupJpaRepository = billGroupJpaRepository;
        this.participantJpaRepository = participantJpaRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public Payment save(Payment payment) {

        BillGroupEntity billGroupEntity = billGroupJpaRepository
                .getReferenceById(payment.getGroupId());

        ParticipantEntity fromParticipantEntity = participantJpaRepository
                .getReferenceById(payment.getFromParticipantId());

        ParticipantEntity toParticipantEntity = participantJpaRepository
                .getReferenceById(payment.getToParticipantId());

        PaymentEntity paymentEntity = paymentMapper.toEntity(
                payment,
                billGroupEntity,
                fromParticipantEntity,
                toParticipantEntity
        );

        PaymentEntity savedPayment = paymentJpaRepository.save(paymentEntity);

        return paymentMapper.toDomain(savedPayment);
    }

    @Override
    public List<Payment> findByGroupId(UUID groupId) {
        return paymentJpaRepository.findByGroup_Id(groupId)
                .stream()
                .map(paymentMapper::toDomain)
                .toList();
    }
}
