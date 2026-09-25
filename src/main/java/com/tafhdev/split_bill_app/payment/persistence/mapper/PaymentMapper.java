package com.tafhdev.split_bill_app.payment.persistence.mapper;

import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import com.tafhdev.split_bill_app.group.persistence.entity.ParticipantEntity;
import com.tafhdev.split_bill_app.payment.domain.Payment;
import com.tafhdev.split_bill_app.payment.persistence.entity.PaymentEntity;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentEntity toEntity(
            Payment domain,
            BillGroupEntity groupEntity,
            ParticipantEntity fromParticipantEntity,
            ParticipantEntity toParticipantEntity
    ) {
        return new PaymentEntity(
                domain.getId(),
                groupEntity,
                fromParticipantEntity,
                toParticipantEntity,
                domain.getAmount().value(),
                domain.getCreatedAt()
        );
    }

    public Payment toDomain(PaymentEntity entity) {
        return Payment.reconstitute(
                entity.getId(),
                entity.getGroup().getId(),
                entity.getFromParticipant().getId(),
                entity.getToParticipant().getId(),
                Money.of(entity.getAmount()),
                entity.getCreatedAt()
        );
    }
}
