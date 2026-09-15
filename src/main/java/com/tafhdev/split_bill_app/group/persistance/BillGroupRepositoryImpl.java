package com.tafhdev.split_bill_app.group.persistance;

import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BillGroupRepositoryImpl implements BillGroupRepository {

    private final BillGroupJpaRepository billGroupJpaRepository;
    private final ParticipantJpaRepository participantJpaRepository;
    private final BillGroupMapper billGroupMapper;
    private final ParticipantMapper participantMapper;

    public BillGroupRepositoryImpl(
            BillGroupJpaRepository billGroupJpaRepository,
            ParticipantJpaRepository participantJpaRepository,
            BillGroupMapper billGroupMapper,
            ParticipantMapper participantMapper
    ) {
        this.billGroupJpaRepository = billGroupJpaRepository;
        this.participantJpaRepository = participantJpaRepository;
        this.billGroupMapper = billGroupMapper;
        this.participantMapper = participantMapper;
    }

    @Override
    public BillGroup save(BillGroup group) {

        BillGroupJpaEntity groupEntity =
                billGroupMapper.toEntity(group);

        billGroupJpaRepository.save(groupEntity);

        List<ParticipantJpaEntity> participantEntities =
                group.getParticipants().stream()
                        .map(participant ->
                                participantMapper.toEntity(
                                        participant,
                                        groupEntity
                                )
                        )
                        .toList();

        participantJpaRepository.saveAll(participantEntities);

        return group;
    }

    @Override
    public Optional<BillGroup> findById(UUID groupId) {
        return billGroupJpaRepository.findByIdWithParticipants(groupId)
                .map(billGroupMapper::toDomain);
    }
}
