package com.tafhdev.split_bill_app.expense.domain.calculator;

import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.expense.domain.SplitParticipant;
import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EqualSplitCalculatorTest {

    private final IdGenerator idGenerator = mock(IdGenerator.class);

    private final EqualSplitCalculator calculator =
            new EqualSplitCalculator(idGenerator);

    @Test
    void shouldSplitAmountEqually() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();

        List<ExpenseSplit> splits = calculator.calculate(
                Money.of(new BigDecimal("300.00")),
                List.of(
                        new SplitParticipant(participant1, null, null),
                        new SplitParticipant(participant2, null, null),
                        new SplitParticipant(participant3, null, null)
                )
        );

        assertThat(splits)
                .hasSize(3);

        assertThat(splits.get(0).getParticipantId())
                .isEqualTo(participant1);

        assertThat(splits.get(0).getAmount())
                .isEqualTo(Money.of(new BigDecimal("100.00")));

        assertThat(splits.get(1).getAmount())
                .isEqualTo(Money.of(new BigDecimal("100.00")));

        assertThat(splits.get(2).getAmount())
                .isEqualTo(Money.of(new BigDecimal("100.00")));
    }

    @Test
    void shouldPutRoundingRemainderOnLastParticipant() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();

        List<ExpenseSplit> splits = calculator.calculate(
                Money.of(new BigDecimal("100.00")),
                List.of(
                        new SplitParticipant(participant1, null, null),
                        new SplitParticipant(participant2, null, null),
                        new SplitParticipant(participant3, null, null)
                )
        );

        assertThat(splits.get(0).getAmount())
                .isEqualTo(Money.of(new BigDecimal("33.33")));

        assertThat(splits.get(1).getAmount())
                .isEqualTo(Money.of(new BigDecimal("33.33")));

        assertThat(splits.get(2).getAmount())
                .isEqualTo(Money.of(new BigDecimal("33.34")));
    }

    @Test
    void shouldPreserveTotalAmount() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        List<ExpenseSplit> splits = calculator.calculate(
                Money.of(new BigDecimal("100.00")),
                List.of(
                        new SplitParticipant(
                                UUID.randomUUID(),
                                null,
                                null
                        ),
                        new SplitParticipant(
                                UUID.randomUUID(),
                                null,
                                null
                        ),
                        new SplitParticipant(
                                UUID.randomUUID(),
                                null,
                                null
                        )
                )
        );

        Money total = splits.stream()
                .map(ExpenseSplit::getAmount)
                .reduce(
                        Money.of(BigDecimal.ZERO),
                        Money::add
                );

        assertThat(total)
                .isEqualTo(Money.of(new BigDecimal("100.00")));
    }

    @Test
    void shouldRejectNullExpenseAmount() {
        assertThatThrownBy(() ->
                calculator.calculate(
                        null,
                        List.of(
                                new SplitParticipant(
                                        UUID.randomUUID(),
                                        null,
                                        null
                                ),
                                new SplitParticipant(
                                        UUID.randomUUID(),
                                        null,
                                        null
                                )
                        )
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("expense amount must not be null");
    }

    @Test
    void shouldRejectNullParticipants() {
        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        null
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("participants must not be null");
    }

    @Test
    void shouldRejectLessThanTwoParticipants() {
        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        List.of(
                                new SplitParticipant(
                                        UUID.randomUUID(),
                                        null,
                                        null
                                )
                        )
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("equal split requires at least two participants");
    }

    @Test
    void shouldRejectDuplicateParticipants() {
        UUID participantId = UUID.randomUUID();

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        List.of(
                                new SplitParticipant(
                                        participantId,
                                        null,
                                        null
                                ),
                                new SplitParticipant(
                                        participantId,
                                        null,
                                        null
                                )
                        )
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("split participants must be unique");
    }

    @Test
    void shouldRejectNullParticipantId() {
        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        Arrays.asList(
                                new SplitParticipant(
                                        UUID.randomUUID(),
                                        null,
                                        null
                                ),
                                new SplitParticipant(
                                        null,
                                        null,
                                        null
                                )
                        )
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("participant id must not be null");
    }

    @Test
    void shouldRejectAmountInEqualSplit() {
        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        List.of(
                                new SplitParticipant(
                                        UUID.randomUUID(),
                                        Money.of(new BigDecimal("50.00")),
                                        null
                                ),
                                new SplitParticipant(
                                        UUID.randomUUID(),
                                        null,
                                        null
                                )
                        )
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage(
                        "equal split must not have amount or percentage"
                );
    }

    @Test
    void shouldRejectPercentageInEqualSplit() {
        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        List.of(
                                new SplitParticipant(
                                        UUID.randomUUID(),
                                        null,
                                        new BigDecimal("50")
                                ),
                                new SplitParticipant(
                                        UUID.randomUUID(),
                                        null,
                                        null
                                )
                        )
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage(
                        "equal split must not have amount or percentage"
                );
    }
}