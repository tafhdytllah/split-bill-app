package com.tafhdev.split_bill_app.group.persistance;

import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BillGroupMapper {

    private final ParticipantMapper participantMapper;

    public BillGroupMapper(ParticipantMapper participantMapper) {
        this.participantMapper = participantMapper;
    }

    public BillGroupJpaEntity toEntity(BillGroup domain) {
        return new BillGroupJpaEntity(
                domain.getId(),
                domain.getName(),
                domain.getCreatedAt()
        );
    }

    public BillGroup toDomain(BillGroupJpaEntity entity) {

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
