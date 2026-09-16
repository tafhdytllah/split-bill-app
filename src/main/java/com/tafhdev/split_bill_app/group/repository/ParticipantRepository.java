package com.tafhdev.split_bill_app.group.repository;

import com.tafhdev.split_bill_app.group.domain.Participant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipantRepository {

    Optional<Participant> findById(UUID id);

    List<Participant> findAllByIds(
            List<UUID> ids,
            UUID groupId
    );
}
