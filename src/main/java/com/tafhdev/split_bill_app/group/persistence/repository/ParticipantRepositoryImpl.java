package com.tafhdev.split_bill_app.group.persistence.repository;

import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.persistence.mapper.ParticipantMapper;
import com.tafhdev.split_bill_app.group.repository.ParticipantRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ParticipantRepositoryImpl implements ParticipantRepository {

    private final ParticipantJpaRepository participantJpaRepository;
    private final ParticipantMapper participantMapper;

    public ParticipantRepositoryImpl(
            ParticipantJpaRepository participantJpaRepository,
            ParticipantMapper participantMapper
    ) {
        this.participantJpaRepository = participantJpaRepository;
        this.participantMapper = participantMapper;
    }

    @Override
    public Optional<Participant> findById(UUID id) {
        return participantJpaRepository.findById(id)
                .map(participantMapper::toDomain);
    }

    @Override
    public List<Participant> findAllByIds(List<UUID> ids, UUID groupId) {
        return participantJpaRepository
                .findAllByIdInAndGroupId(ids, groupId)
                .stream()
                .map(participantMapper::toDomain)
                .toList();
    }
}
