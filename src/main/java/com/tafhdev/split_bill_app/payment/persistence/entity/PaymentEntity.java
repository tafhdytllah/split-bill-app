package com.tafhdev.split_bill_app.payment.persistence.entity;

import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import com.tafhdev.split_bill_app.group.persistence.entity.ParticipantEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private BillGroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_participant_id", nullable = false)
    private ParticipantEntity fromParticipant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_participant_id", nullable = false)
    private ParticipantEntity toParticipant;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PaymentEntity() {
    }

    public PaymentEntity(
            UUID id,
            BillGroupEntity group,
            ParticipantEntity fromParticipant,
            ParticipantEntity toParticipant,
            BigDecimal amount,
            Instant createdAt
    ) {
        this.id = id;
        this.group = group;
        this.fromParticipant = fromParticipant;
        this.toParticipant = toParticipant;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public BillGroupEntity getGroup() {
        return group;
    }

    public ParticipantEntity getFromParticipant() {
        return fromParticipant;
    }

    public ParticipantEntity getToParticipant() {
        return toParticipant;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
