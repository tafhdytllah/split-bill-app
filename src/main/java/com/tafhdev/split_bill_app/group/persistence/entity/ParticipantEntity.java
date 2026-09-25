package com.tafhdev.split_bill_app.group.persistence.entity;

import com.tafhdev.split_bill_app.expense.persistence.entity.ExpenseEntity;
import com.tafhdev.split_bill_app.expense.persistence.entity.ExpenseSplitEntity;
import com.tafhdev.split_bill_app.payment.persistence.entity.PaymentEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "participants")
public class ParticipantEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private BillGroupEntity group;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "paidBy",
            fetch = FetchType.LAZY
    )
    private List<ExpenseEntity> paidExpenses = new ArrayList<>();

    @OneToMany(
            mappedBy = "participant",
            fetch = FetchType.LAZY
    )
    private List<ExpenseSplitEntity> expenseSplits = new ArrayList<>();

    @OneToMany(
            mappedBy = "fromParticipant",
            fetch = FetchType.LAZY
    )
    private List<PaymentEntity> sentPayments = new ArrayList<>();

    @OneToMany(
            mappedBy = "toParticipant",
            fetch = FetchType.LAZY
    )
    private List<PaymentEntity> receivedPayments = new ArrayList<>();

    protected ParticipantEntity() {
    }

    public ParticipantEntity(
            UUID id,
            BillGroupEntity group,
            String name,
            Instant createdAt
    ) {
        this.id = id;
        this.group = group;
        this.name = name;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public BillGroupEntity getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ExpenseEntity> getPaidExpenses() {
        return Collections.unmodifiableList(paidExpenses);
    }

    public List<ExpenseSplitEntity> getExpenseSplits() {
        return Collections.unmodifiableList(expenseSplits);
    }

    public List<PaymentEntity> getSentPayments() {
        return Collections.unmodifiableList(sentPayments);
    }

    public List<PaymentEntity> getReceivedPayments() {
        return Collections.unmodifiableList(receivedPayments);
    }
}
