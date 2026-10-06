package com.tafhdev.split_bill_app.settlement.domain;

import com.tafhdev.split_bill_app.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceTest {

    private final UUID participantId = UUID.randomUUID();

    @Test
    void shouldCreateBalance() {
        Money amount = Money.of(new BigDecimal("100000.00"));

        Balance balance = Balance.of(participantId, amount);

        assertThat(balance.getParticipantId())
                .isEqualTo(participantId);

        assertThat(balance.getAmount())
                .isEqualTo(amount);
    }

    @Test
    void shouldAddAmount() {
        Balance balance = Balance.of(
                participantId,
                Money.of(new BigDecimal("100000.00"))
        );

        Balance result = balance.add(
                Money.of(new BigDecimal("50000.00"))
        );

        assertThat(result.getParticipantId())
                .isEqualTo(participantId);

        assertThat(result.getAmount())
                .isEqualTo(Money.of(new BigDecimal("150000.00")));

        assertThat(balance.getAmount())
                .isEqualTo(Money.of(new BigDecimal("100000.00")));
    }

    @Test
    void shouldSubtractAmount() {
        Balance balance = Balance.of(
                participantId,
                Money.of(new BigDecimal("100000.00"))
        );

        Balance result = balance.subtract(
                Money.of(new BigDecimal("40000.00"))
        );

        assertThat(result.getAmount())
                .isEqualTo(Money.of(new BigDecimal("60000.00")));

        assertThat(result.getParticipantId())
                .isEqualTo(participantId);
    }

    @Test
    void shouldNegateAmount() {
        Balance balance = Balance.of(
                participantId,
                Money.of(new BigDecimal("100000.00"))
        );

        Balance result = balance.negate();

        assertThat(result.getParticipantId())
                .isEqualTo(participantId);

        assertThat(result.getAmount())
                .isEqualTo(Money.of(new BigDecimal("-100000.00")));
    }

    @Test
    void shouldIdentifyCreditor() {
        Balance balance = Balance.of(
                participantId,
                Money.of(new BigDecimal("100000.00"))
        );

        assertThat(balance.isCreditor())
                .isTrue();

        assertThat(balance.isDebtor())
                .isFalse();

        assertThat(balance.isSettled())
                .isFalse();
    }

    @Test
    void shouldIdentifyDebtor() {
        Balance balance = Balance.of(
                participantId,
                Money.of(new BigDecimal("-100000.00"))
        );

        assertThat(balance.isCreditor())
                .isFalse();

        assertThat(balance.isDebtor())
                .isTrue();

        assertThat(balance.isSettled())
                .isFalse();
    }

    @Test
    void shouldIdentifySettledBalance() {
        Balance balance = Balance.of(
                participantId,
                Money.zero()
        );

        assertThat(balance.isCreditor())
                .isFalse();

        assertThat(balance.isDebtor())
                .isFalse();

        assertThat(balance.isSettled())
                .isTrue();
    }
}