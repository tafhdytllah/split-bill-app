package com.tafhdev.split_bill_app.expense.domain;

import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpenseSplitTest {

    @Test
    void shouldCreateExpenseSplit() {
        UUID id = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        Money amount = Money.of(new BigDecimal("100.00"));

        ExpenseSplit split = ExpenseSplit.createNew(
                id,
                participantId,
                amount
        );

        assertThat(split.getId())
                .isEqualTo(id);

        assertThat(split.getParticipantId())
                .isEqualTo(participantId);

        assertThat(split.getAmount())
                .isEqualTo(amount);
    }

    @Test
    void shouldAllowZeroAmount() {
        ExpenseSplit split = ExpenseSplit.createNew(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Money.of(new BigDecimal("0.00"))
        );

        assertThat(split.getAmount())
                .isEqualTo(Money.of(new BigDecimal("0.00")));
    }

    @Test
    void shouldRejectNullId() {
        assertThatThrownBy(() ->
                ExpenseSplit.createNew(
                        null,
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("100.00"))
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("id must not be null");
    }

    @Test
    void shouldRejectNullParticipantId() {
        assertThatThrownBy(() ->
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        null,
                        Money.of(new BigDecimal("100.00"))
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("participant id must not be null");
    }

    @Test
    void shouldRejectNullAmount() {
        assertThatThrownBy(() ->
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("amount must not be null");
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() ->
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("-1.00"))
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("split amount must not be negative");
    }

    @Test
    void shouldReconstituteExpenseSplit() {
        UUID id = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        Money amount = Money.of(new BigDecimal("100.00"));

        ExpenseSplit split = ExpenseSplit.reconstitute(
                id,
                participantId,
                amount
        );

        assertThat(split.getId())
                .isEqualTo(id);

        assertThat(split.getParticipantId())
                .isEqualTo(participantId);

        assertThat(split.getAmount())
                .isEqualTo(amount);
    }
}