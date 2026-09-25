package com.tafhdev.split_bill_app.payment.domain;

import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.domain.exception.Guard;

import java.time.Instant;
import java.util.UUID;

public class Payment {

    private final UUID id;
    private final UUID groupId;
    private final UUID fromParticipantId;
    private final UUID toParticipantId;
    private final Money amount;
    private final Instant createdAt;

    private Payment(
            UUID id,
            UUID groupId,
            UUID fromParticipantId,
            UUID toParticipantId,
            Money amount,
            Instant createdAt
    ) {
        this.id = id;
        this.groupId = groupId;
        this.fromParticipantId = fromParticipantId;
        this.toParticipantId = toParticipantId;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public static Payment createNew(
            UUID id,
            UUID groupId,
            UUID fromParticipantId,
            UUID toParticipantId,
            Money amount,
            Instant createdAt
    ) {

        validateInvariants(
                id,
                groupId,
                fromParticipantId,
                toParticipantId,
                amount,
                createdAt
        );

        return new Payment(
                id,
                groupId,
                fromParticipantId,
                toParticipantId,
                amount,
                createdAt
        );
    }

    public static Payment reconstitute(
            UUID id,
            UUID groupId,
            UUID fromParticipantId,
            UUID toParticipantId,
            Money amount,
            Instant createdAt
    ) {
        validateInvariants(
                id,
                groupId,
                fromParticipantId,
                toParticipantId,
                amount,
                createdAt
        );

        return new Payment(
                id,
                groupId,
                fromParticipantId,
                toParticipantId,
                amount,
                createdAt
        );
    }

    private static void validateInvariants(
            UUID id,
            UUID groupId,
            UUID fromParticipantId,
            UUID toParticipantId,
            Money amount,
            Instant createdAt
    ) {
        Guard.requireNotNull(id, "id");
        Guard.requireNotNull(groupId, "group id");
        Guard.requireNotNull(fromParticipantId, "from participant id");
        Guard.requireNotNull(toParticipantId, "to participant id");
        Guard.requireNotNull(amount, "amount");
        Guard.requireNotNull(createdAt, "created at");

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
            throw new DomainException("payment participants must be different");
        }
    }

    private static void validateAmount(Money amount) {
        if (!amount.isPositive()) {
            throw new DomainException("payment amount must be greater than zero");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
