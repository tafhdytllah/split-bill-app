package com.tafhdev.split_bill_app.group.persistence.mapper;

import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.persistence.entity.ParticipantEntity;
import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BillGroupMapper {

    private final ParticipantMapper participantMapper;

    public BillGroupMapper(
            ParticipantMapper participantMapper
    ) {
        this.participantMapper = participantMapper;
    }

    public BillGroupEntity toEntity(BillGroup domain) {
        BillGroupEntity entity = new BillGroupEntity(
                domain.getId(),
                domain.getName(),
                domain.getCreatedAt()
        );

        for (Participant participant : domain.getParticipants()) {
            ParticipantEntity participantEntity =
                    participantMapper.toEntity(
                            participant,
                            entity
                    );

            entity.addParticipant(participantEntity);
        }

        return entity;
    }

    public BillGroup toDomain(BillGroupEntity entity) {

        List<Participant> participants =
                entity.getParticipants().stream()
                        .map(participantMapper::toDomain)
                        .toList();

        return BillGroup.reconstitute(
                entity.getId(),
                entity.getName(),
                participants,
                entity.getCreatedAt()
        );
    }
}
