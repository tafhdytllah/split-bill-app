package com.tafhdev.split_bill_app.group.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParticipantJpaRepository extends JpaRepository<ParticipantJpaEntity, UUID> {
}
