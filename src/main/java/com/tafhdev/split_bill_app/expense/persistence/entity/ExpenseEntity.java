package com.tafhdev.split_bill_app.expense.persistence.entity;

import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import com.tafhdev.split_bill_app.group.persistence.entity.ParticipantEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "expenses")
public class ExpenseEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private BillGroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false)
    private ParticipantEntity paidBy;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(length = 50, nullable = false)
    private String category;

    @Column(name = "split_type", length = 50, nullable = false)
    private String splitType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "expense",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private List<ExpenseSplitEntity> splits = new ArrayList<>();

    public void addSplit(ExpenseSplitEntity split) {
        splits.add(split);
    }

    protected ExpenseEntity() {
    }

    public ExpenseEntity(
            UUID id,
            BillGroupEntity group,
            ParticipantEntity paidBy,
            BigDecimal amount,
            String category,
            String splitType,
            Instant createdAt
    ) {
        this.id = id;
        this.group = group;
        this.paidBy = paidBy;
        this.amount = amount;
        this.category = category;
        this.splitType = splitType;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public BillGroupEntity getGroup() {
        return group;
    }

    public ParticipantEntity getPaidBy() {
        return paidBy;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getSplitType() {
        return splitType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ExpenseSplitEntity> getSplits() {
        return Collections.unmodifiableList(splits);
    }
}
