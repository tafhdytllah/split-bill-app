package com.tafhdev.split_bill_app.expense.persistence.entity;

import com.tafhdev.split_bill_app.group.persistence.entity.ParticipantEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "expense_splits")
public class ExpenseSplitEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private ExpenseEntity expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private ParticipantEntity participant;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    protected ExpenseSplitEntity() {
    }

    public ExpenseSplitEntity(
            UUID id,
            ExpenseEntity expense,
            ParticipantEntity participant,
            BigDecimal amount
    ) {
        this.id = id;
        this.expense = expense;
        this.participant = participant;
        this.amount = amount;
    }

    public UUID getId() {
        return id;
    }

    public ExpenseEntity getExpense() {
        return expense;
    }

    public ParticipantEntity getParticipant() {
        return participant;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
