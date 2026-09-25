package com.tafhdev.split_bill_app.expense.domain;

import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.domain.exception.Guard;

import java.util.UUID;

public class ExpenseSplit {

    private final UUID id;
    private final UUID participantId;
    private final Money amount;

    private ExpenseSplit(
            UUID id,
            UUID participantId,
            Money amount
    ) {
        this.id = id;
        this.participantId = participantId;
        this.amount = amount;
    }

    public static ExpenseSplit createNew(
            UUID id,
            UUID participantId,
            Money amount
    ) {
        validateInvariants(
                id,
                participantId,
                amount
        );

        return new ExpenseSplit(
                id,
                participantId,
                amount
        );
    }

    public static ExpenseSplit reconstitute(
            UUID id,
            UUID participantId,
            Money amount
    ) {
        return createNew(
                id,
                participantId,
                amount
        );
    }

    private static void validateInvariants(
            UUID id,
            UUID participantId,
            Money amount
    ) {
        Guard.requireNotNull(id, "id");
        Guard.requireNotNull(participantId, "participant id");
        Guard.requireNotNull(amount, "amount");

        validateAmount(amount);
    }

    private static void validateAmount(Money amount) {
        if (amount.isNegative()) {
            throw new DomainException("split amount must not be negative");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getParticipantId() {
        return participantId;
    }

    public Money getAmount() {
        return amount;
    }
}
