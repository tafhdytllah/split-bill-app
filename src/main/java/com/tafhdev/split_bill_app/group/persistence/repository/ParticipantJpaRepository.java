package com.tafhdev.split_bill_app.group.persistence.repository;

import com.tafhdev.split_bill_app.group.persistence.entity.ParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipantJpaRepository extends JpaRepository<ParticipantEntity, UUID> {

    List<ParticipantEntity> findAllByIdInAndGroupId(
            List<UUID> ids,
            UUID groupId
    );

    Optional<ParticipantEntity> findByIdAndGroupId(
            UUID id,
            UUID groupId
    );
}
