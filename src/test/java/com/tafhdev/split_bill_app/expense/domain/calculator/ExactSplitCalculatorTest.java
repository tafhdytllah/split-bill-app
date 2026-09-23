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

class ExactSplitCalculatorTest {

    private final IdGenerator idGenerator = mock(IdGenerator.class);

    private final ExactSplitCalculator calculator =
            new ExactSplitCalculator(idGenerator);

    @Test
    void shouldSplitAmountExactly() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        participant1,
                        Money.of(new BigDecimal("150.00")),
                        null
                ),
                new SplitParticipant(
                        participant2,
                        Money.of(new BigDecimal("100.00")),
                        null
                ),
                new SplitParticipant(
                        participant3,
                        Money.of(new BigDecimal("50.00")),
                        null
                )
        );

        List<ExpenseSplit> result = calculator.calculate(
                Money.of(new BigDecimal("300.00")),
                participants
        );

        assertThat(result)
                .extracting(ExpenseSplit::getAmount)
                .containsExactly(
                        Money.of(new BigDecimal("150.00")),
                        Money.of(new BigDecimal("100.00")),
                        Money.of(new BigDecimal("50.00"))
                );
    }

    @Test
    void shouldPreserveTotalExpenseAmount() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("150.25")),
                        null
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("149.75")),
                        null
                )
        );

        List<ExpenseSplit> result = calculator.calculate(
                Money.of(new BigDecimal("300.00")),
                participants
        );

        Money total = result.stream()
                .map(ExpenseSplit::getAmount)
                .reduce(
                        Money.of(BigDecimal.ZERO),
                        Money::add
                );

        assertThat(total)
                .isEqualTo(Money.of(new BigDecimal("300.00")));
    }

    @Test
    void shouldAllowZeroAmount() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("100.00")),
                        null
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("0.00")),
                        null
                )
        );

        List<ExpenseSplit> result = calculator.calculate(
                Money.of(new BigDecimal("100.00")),
                participants
        );

        assertThat(result)
                .extracting(ExpenseSplit::getAmount)
                .containsExactly(
                        Money.of(new BigDecimal("100.00")),
                        Money.of(new BigDecimal("0.00"))
                );
    }

    @Test
    void shouldRejectNullExpenseAmount() {
        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("50.00")),
                        null
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("50.00")),
                        null
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        null,
                        participants
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
        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("100.00")),
                        null
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        participants
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage(
                        "exact split requires at least two participants"
                );
    }

    @Test
    void shouldRejectNullParticipant() {
        List<SplitParticipant> participants = Arrays.asList(
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("50.00")),
                        null
                ),
                null
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        participants
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("split participant must not be null");
    }

    @Test
    void shouldRejectNullParticipantId() {
        List<SplitParticipant> participants = Arrays.asList(
                new SplitParticipant(
                        null,
                        Money.of(new BigDecimal("50.00")),
                        null
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("50.00")),
                        null
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        participants
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("participant id must not be null");
    }

    @Test
    void shouldRejectNullAmount() {
        List<SplitParticipant> participants = Arrays.asList(
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        null
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("50.00")),
                        null
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        participants
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("split amount must not be null");
    }

    @Test
    void shouldRejectNegativeAmount() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("-50.00")),
                        null
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("150.00")),
                        null
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        participants
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("split amount must not be negative");
    }

    @Test
    void shouldRejectDuplicateParticipants() {
        UUID participantId = UUID.randomUUID();

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        participantId,
                        Money.of(new BigDecimal("50.00")),
                        null
                ),
                new SplitParticipant(
                        participantId,
                        Money.of(new BigDecimal("50.00")),
                        null
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        participants
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("split participants must be unique");
    }

    @Test
    void shouldRejectPercentageInExactSplit() {
        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("50.00")),
                        new BigDecimal("50")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("50.00")),
                        null
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        participants
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("exact split must not have percentage");
    }

    @Test
    void shouldRejectTotalSplitAmountLessThanExpenseAmount() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("40.00")),
                        null
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("50.00")),
                        null
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        participants
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage(
                        "exact split amounts must equal expense amount"
                );
    }

    @Test
    void shouldRejectTotalSplitAmountGreaterThanExpenseAmount() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("60.00")),
                        null
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("50.00")),
                        null
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        participants
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage(
                        "exact split amounts must equal expense amount"
                );
    }
}