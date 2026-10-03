package com.tafhdev.split_bill_app.settlement.domain;

import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.Guard;

import java.util.UUID;

public final class Balance {

    private final UUID participantId;
    private final Money amount;

    private Balance(
            UUID participantId,
            Money amount
    ) {
        this.participantId = participantId;
        this.amount = amount;
    }

    public static Balance of(
            UUID participantId,
            Money amount
    ) {
        Guard.requireNotNull(participantId, "participant id");
        Guard.requireNotNull(amount, "balance amount");

        return new Balance(
                participantId,
                amount
        );
    }

    public Balance add(Money amount) {
        Guard.requireNotNull(amount, "amount");

        return new Balance(
                participantId,
                this.amount.add(amount)
        );
    }

    public Balance subtract(Money amount) {
        Guard.requireNotNull(amount, "amount");

        return new Balance(
                participantId,
                this.amount.subtract(amount)
        );
    }

    public Balance negate() {
        return new Balance(
                participantId,
                amount.negate()
        );
    }

    public boolean isCreditor() {
        return amount.isPositive();
    }

    public boolean isDebtor() {
        return amount.isNegative();
    }

    public boolean isSettled() {
        return amount.isZero();
    }

    public UUID getParticipantId() {
        return participantId;
    }

    public Money getAmount() {
        return amount;
    }
}
