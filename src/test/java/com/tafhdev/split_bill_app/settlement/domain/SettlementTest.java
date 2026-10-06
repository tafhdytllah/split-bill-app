package com.tafhdev.split_bill_app.settlement.domain;

import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementTest {

    private final UUID fromParticipantId = UUID.randomUUID();
    private final UUID toParticipantId = UUID.randomUUID();

    @Test
    void shouldCreateSettlement() {
        Money amount = Money.of(new BigDecimal("100000.00"));

        Settlement settlement = Settlement.createNew(
                fromParticipantId,
                toParticipantId,
                amount
        );

        assertThat(settlement.getFromParticipantId())
                .isEqualTo(fromParticipantId);

        assertThat(settlement.getToParticipantId())
                .isEqualTo(toParticipantId);

        assertThat(settlement.getAmount())
                .isEqualTo(amount);
    }

    @Test
    void shouldRejectWhenParticipantsAreSame() {
        assertThatThrownBy(() ->
                Settlement.createNew(
                        fromParticipantId,
                        fromParticipantId,
                        Money.of(new BigDecimal("100000.00"))
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("settlement participant must be different");
    }

    @Test
    void shouldRejectWhenAmountIsZero() {
        assertThatThrownBy(() ->
                Settlement.createNew(
                        fromParticipantId,
                        toParticipantId,
                        Money.zero()
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("settlement amount must be greater than zero");
    }

    @Test
    void shouldRejectWhenAmountIsNegative() {
        assertThatThrownBy(() ->
                Settlement.createNew(
                        fromParticipantId,
                        toParticipantId,
                        Money.of(new BigDecimal("-100000.00"))
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("settlement amount must be greater than zero");
    }
}