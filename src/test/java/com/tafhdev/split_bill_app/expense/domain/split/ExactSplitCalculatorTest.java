package com.tafhdev.split_bill_app.expense.domain.split;

import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
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

        List<ExactSplit> exactSplits = List.of(
                new ExactSplit(
                        participant1,
                        Money.of(new BigDecimal("150.00"))
                ),
                new ExactSplit(
                        participant2,
                        Money.of(new BigDecimal("100.00"))
                ),
                new ExactSplit(
                        participant3,
                        Money.of(new BigDecimal("50.00"))
                )
        );

        List<ExpenseSplit> result = calculator.calculate(
                Money.of(new BigDecimal("300.00")),
                exactSplits
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

        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        List<ExactSplit> exactSplits = List.of(
                new ExactSplit(
                        participant1,
                        Money.of(new BigDecimal("150.25"))
                ),
                new ExactSplit(
                        participant2,
                        Money.of(new BigDecimal("149.75"))
                )
        );

        List<ExpenseSplit> result = calculator.calculate(
                Money.of(new BigDecimal("300.00")),
                exactSplits
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

        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        List<ExactSplit> exactSplits = List.of(
                new ExactSplit(
                        participant1,
                        Money.of(new BigDecimal("100.00"))
                ),
                new ExactSplit(
                        participant2,
                        Money.of(new BigDecimal("0.00"))
                )
        );

        List<ExpenseSplit> result = calculator.calculate(
                Money.of(new BigDecimal("100.00")),
                exactSplits
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
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        List<ExactSplit> exactSplits = List.of(
                new ExactSplit(
                        participant1,
                        Money.of(new BigDecimal("50.00"))
                ),
                new ExactSplit(
                        participant2,
                        Money.of(new BigDecimal("50.00"))
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        null,
                        exactSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("expense amount must not be null");
    }

    @Test
    void shouldRejectNullExactSplits() {
        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        null
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("exact splits must not be null");
    }

    @Test
    void shouldRejectLessThanTwoParticipants() {
        UUID participantId = UUID.randomUUID();

        List<ExactSplit> exactSplits = List.of(
                new ExactSplit(
                        participantId,
                        Money.of(new BigDecimal("100.00"))
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        exactSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage(
                        "exact split requires at least two participants"
                );
    }

    @Test
    void shouldRejectNullExactSplit() {
        UUID participantId = UUID.randomUUID();

        List<ExactSplit> exactSplits = Arrays.asList(
                new ExactSplit(
                        participantId,
                        Money.of(new BigDecimal("50.00"))
                ),
                null
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        exactSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("exact split must not be null");
    }

    @Test
    void shouldRejectNullParticipantId() {
        List<ExactSplit> exactSplits = Arrays.asList(
                new ExactSplit(
                        null,
                        Money.of(new BigDecimal("50.00"))
                ),
                new ExactSplit(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("50.00"))
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        exactSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("participant id must not be null");
    }

    @Test
    void shouldRejectNullAmount() {
        List<ExactSplit> exactSplits = Arrays.asList(
                new ExactSplit(
                        UUID.randomUUID(),
                        null
                ),
                new ExactSplit(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("50.00"))
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        exactSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("amount must not be null");
    }

    @Test
    void shouldRejectNegativeAmount() {
        List<ExactSplit> exactSplits = List.of(
                new ExactSplit(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("-50.00"))
                ),
                new ExactSplit(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("150.00"))
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        exactSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("split amount must not be negative");
    }

    @Test
    void shouldRejectDuplicateParticipants() {
        UUID participantId = UUID.randomUUID();

        List<ExactSplit> exactSplits = List.of(
                new ExactSplit(
                        participantId,
                        Money.of(new BigDecimal("50.00"))
                ),
                new ExactSplit(
                        participantId,
                        Money.of(new BigDecimal("50.00"))
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        exactSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("split participants must be unique");
    }

    @Test
    void shouldRejectTotalSplitAmountLessThanExpenseAmount() {
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        List<ExactSplit> exactSplits = List.of(
                new ExactSplit(
                        participant1,
                        Money.of(new BigDecimal("40.00"))
                ),
                new ExactSplit(
                        participant2,
                        Money.of(new BigDecimal("50.00"))
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        exactSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage(
                        "total split amount must equal expense amount"
                );
    }

    @Test
    void shouldRejectTotalSplitAmountGreaterThanExpenseAmount() {
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        List<ExactSplit> exactSplits = List.of(
                new ExactSplit(
                        participant1,
                        Money.of(new BigDecimal("60.00"))
                ),
                new ExactSplit(
                        participant2,
                        Money.of(new BigDecimal("50.00"))
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        exactSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage(
                        "total split amount must equal expense amount"
                );
    }
}