package com.tafhdev.split_bill_app.group.persistance;

import com.tafhdev.split_bill_app.group.domain.Participant;
import org.springframework.stereotype.Component;

@Component
public class ParticipantMapper {

    public ParticipantJpaEntity toEntity(
            Participant domain,
            BillGroupJpaEntity groupEntity
    ) {
        return new ParticipantJpaEntity(
                domain.getId(),
                groupEntity,
                domain.getName(),
                domain.getCreatedAt()
        );
    }

    public Participant toDomain(ParticipantJpaEntity entity) {
        return Participant.reconstitute(
                entity.getId(),
                entity.getGroup().getId(),
                entity.getName(),
                entity.getCreatedAt()
        );
    }
}
