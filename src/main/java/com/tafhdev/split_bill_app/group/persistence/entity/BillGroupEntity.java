package com.tafhdev.split_bill_app.group.persistence.entity;

import com.tafhdev.split_bill_app.expense.persistence.entity.ExpenseEntity;
import com.tafhdev.split_bill_app.payment.persistence.entity.PaymentEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "groups")
public class BillGroupEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "group",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<ParticipantEntity> participants = new ArrayList<>();

    public void addParticipant(ParticipantEntity participant) {
        participants.add(participant);
    }

    @OneToMany(
            mappedBy = "group",
            fetch = FetchType.LAZY
    )
    private List<ExpenseEntity> expenses = new ArrayList<>();

    @OneToMany(
            mappedBy = "group",
            fetch = FetchType.LAZY
    )
    private List<PaymentEntity> payments = new ArrayList<>();

    protected BillGroupEntity() {
    }

    public BillGroupEntity(UUID id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ParticipantEntity> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public List<ExpenseEntity> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    public List<PaymentEntity> getPayments() {
        return Collections.unmodifiableList(payments);
    }
}
