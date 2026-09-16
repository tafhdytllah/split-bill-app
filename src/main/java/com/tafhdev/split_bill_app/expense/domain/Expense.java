package com.tafhdev.split_bill_app.expense.domain;

import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.domain.exception.Guard;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Expense {

    private final UUID id;
    private final UUID groupId;
    private final UUID paidBy;
    private final Money amount;
    private final ExpenseCategory category;
    private final SplitType splitType;
    private final List<ExpenseSplit> splits;
    private final Instant createdAt;

    private Expense(
            UUID id,
            UUID groupId,
            UUID paidBy,
            Money amount,
            ExpenseCategory category,
            SplitType splitType,
            List<ExpenseSplit> splits,
            Instant createdAt
    ) {
        this.id = id;
        this.groupId = groupId;
        this.paidBy = paidBy;
        this.amount = amount;
        this.category = category;
        this.splitType = splitType;
        this.splits = new ArrayList<>(splits);
        this.createdAt = createdAt;
    }

    public static Expense createNew(
            UUID id,
            UUID groupId,
            UUID paidBy,
            Money amount,
            ExpenseCategory category,
            SplitType splitType,
            List<ExpenseSplit> splits,
            Instant createdAt
    ) {
        validateInvariants(
                id,
                groupId,
                paidBy,
                amount,
                category,
                splitType,
                splits,
                createdAt
        );

        return new Expense(
                id,
                groupId,
                paidBy,
                amount,
                category,
                splitType,
                splits,
                createdAt
        );
    }

    public static Expense reconstitute(
            UUID id,
            UUID groupId,
            UUID paidBy,
            Money amount,
            ExpenseCategory category,
            SplitType splitType,
            List<ExpenseSplit> splits,
            Instant createdAt
    ) {
        return createNew(
                id,
                groupId,
                paidBy,
                amount,
                category,
                splitType,
                splits,
                createdAt
        );
    }

    private static void validateInvariants(
            UUID id,
            UUID groupId,
            UUID paidBy,
            Money amount,
            ExpenseCategory category,
            SplitType splitType,
            List<ExpenseSplit> splits,
            Instant createdAt
    ) {
        Guard.requireNotNull(id, "id");
        Guard.requireNotNull(groupId, "group id");
        Guard.requireNotNull(paidBy, "paid by");
        Guard.requireNotNull(amount, "amount");
        Guard.requireNotNull(category, "category");
        Guard.requireNotNull(splitType, "split type");
        Guard.requireNotNull(splits, "splits");
        Guard.requireNotNull(createdAt, "created at");

        validateAmount(amount);

        validateSplits(splits);
    }

    private static void validateAmount(Money amount) {
        if (!amount.isPositive()) {
            throw new DomainException(
                    "expense amount must be greater than zero"
            );
        }
    }

    private static void validateSplits(List<ExpenseSplit> splits) {
        if (splits.size() < 2) {
            throw new DomainException(
                    "expense must have at least two split members"
            );
        }

        long uniqueParticipantCount = splits.stream()
                .map(ExpenseSplit::getParticipantId)
                .distinct()
                .count();

        if (uniqueParticipantCount != splits.size()) {
            throw new DomainException(
                    "split participants must be unique"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getPaidBy() {
        return paidBy;
    }

    public Money getAmount() {
        return amount;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public List<ExpenseSplit> getSplits() {
        return Collections.unmodifiableList(splits);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
