package com.tafhdev.split_bill_app.payment.domain;

import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private final UUID paymentId = UUID.randomUUID();
    private final UUID groupId = UUID.randomUUID();
    private final UUID fromParticipantId = UUID.randomUUID();
    private final UUID toParticipantId = UUID.randomUUID();
    private final Money amount = Money.of(new BigDecimal("50000.00"));
    private final Instant createdAt = Instant.parse(
            "2026-01-01T10:00:00Z"
    );

    @Test
    void shouldCreatePayment() {

        Payment payment = Payment.createNew(
                paymentId,
                groupId,
                fromParticipantId,
                toParticipantId,
                amount,
                createdAt
        );

        assertThat(payment.getId())
                .isEqualTo(paymentId);

        assertThat(payment.getGroupId())
                .isEqualTo(groupId);

        assertThat(payment.getFromParticipantId())
                .isEqualTo(fromParticipantId);

        assertThat(payment.getToParticipantId())
                .isEqualTo(toParticipantId);

        assertThat(payment.getAmount())
                .isEqualTo(amount);

        assertThat(payment.getCreatedAt())
                .isEqualTo(createdAt);
    }

    @Test
    void shouldReconstitutePayment() {

        Payment payment = Payment.reconstitute(
                paymentId,
                groupId,
                fromParticipantId,
                toParticipantId,
                amount,
                createdAt
        );

        assertThat(payment.getId())
                .isEqualTo(paymentId);

        assertThat(payment.getGroupId())
                .isEqualTo(groupId);

        assertThat(payment.getFromParticipantId())
                .isEqualTo(fromParticipantId);

        assertThat(payment.getToParticipantId())
                .isEqualTo(toParticipantId);

        assertThat(payment.getAmount())
                .isEqualTo(amount);

        assertThat(payment.getCreatedAt())
                .isEqualTo(createdAt);
    }

    @Test
    void shouldRejectWhenParticipantsAreSame() {

        UUID participantId = UUID.randomUUID();

        assertThatThrownBy(() ->
                Payment.createNew(
                        paymentId,
                        groupId,
                        participantId,
                        participantId,
                        amount,
                        createdAt
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("payment participants must be different");
    }

    @Test
    void shouldRejectWhenAmountIsZero() {

        Money zero = Money.zero();

        assertThatThrownBy(() ->
                Payment.createNew(
                        paymentId,
                        groupId,
                        fromParticipantId,
                        toParticipantId,
                        zero,
                        createdAt
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("payment amount must be greater than zero");
    }

    @Test
    void shouldRejectWhenAmountIsNegative() {

        Money negative = Money.of(new BigDecimal("-10000.00"));

        assertThatThrownBy(() ->
                Payment.createNew(
                        paymentId,
                        groupId,
                        fromParticipantId,
                        toParticipantId,
                        negative,
                        createdAt
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("payment amount must be greater than zero");
    }
}