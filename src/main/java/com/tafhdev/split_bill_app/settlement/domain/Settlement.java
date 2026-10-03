package com.tafhdev.split_bill_app.settlement.domain;

import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.domain.exception.Guard;

import java.util.UUID;

public final class Settlement {

    private final UUID fromParticipantId;
    private final UUID toParticipantId;
    private final Money amount;

    private Settlement(
            UUID fromParticipantId,
            UUID toParticipantId,
            Money amount
    ) {
        this.fromParticipantId = fromParticipantId;
        this.toParticipantId = toParticipantId;
        this.amount = amount;
    }

    public static Settlement createNew(
            UUID fromParticipantId,
            UUID toParticipantId,
            Money amount
    ) {
        validateInvariants(
                fromParticipantId,
                toParticipantId,
                amount
        );

        return new Settlement(
                fromParticipantId,
                toParticipantId,
                amount
        );
    }

    private static void validateInvariants(
            UUID fromParticipantId,
            UUID toParticipantId,
            Money amount
    ) {
        Guard.requireNotNull(fromParticipantId, "from participant id");
        Guard.requireNotNull(toParticipantId, "to participant id");

        validateDifferentParticipants(
                fromParticipantId,
                toParticipantId
        );

        validateAmount(amount);
    }

    private static void validateDifferentParticipants(
            UUID fromParticipantId,
            UUID toParticipantId
    ) {
        if (fromParticipantId.equals(toParticipantId)) {
            throw new DomainException("settlement participant must be different");
        }
    }

    private static void validateAmount(Money amount) {
        if (!amount.isPositive()) {
            throw new DomainException("settlement amount must be greater than zero");
        }
    }

    public UUID getFromParticipantId() {
        return fromParticipantId;
    }

    public UUID getToParticipantId() {
        return toParticipantId;
    }

    public Money getAmount() {
        return amount;
    }
}
