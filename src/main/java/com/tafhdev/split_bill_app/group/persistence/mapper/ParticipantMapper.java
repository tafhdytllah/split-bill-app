package com.tafhdev.split_bill_app.group.persistence.mapper;

import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import com.tafhdev.split_bill_app.group.persistence.entity.ParticipantEntity;
import org.springframework.stereotype.Component;

@Component
public class ParticipantMapper {

    public ParticipantEntity toEntity(
            Participant domain,
            BillGroupEntity groupEntity
    ) {
        return new ParticipantEntity(
                domain.getId(),
                groupEntity,
                domain.getName(),
                domain.getCreatedAt()
        );
    }

    public Participant toDomain(ParticipantEntity entity) {
        return Participant.reconstitute(
                entity.getId(),
                entity.getGroup().getId(),
                entity.getName(),
                entity.getCreatedAt()
        );
    }
}
