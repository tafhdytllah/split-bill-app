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

class PercentageSplitCalculatorTest {

    private final IdGenerator idGenerator = mock(IdGenerator.class);

    private final PercentageSplitCalculator calculator =
            new PercentageSplitCalculator(idGenerator);

    @Test
    void shouldSplitAmountBasedOnPercentage() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();

        List<PercentageSplit> percentageSplits = List.of(
                new PercentageSplit(
                        participant1,
                        new BigDecimal("50")
                ),
                new PercentageSplit(
                        participant2,
                        new BigDecimal("30")
                ),
                new PercentageSplit(
                        participant3,
                        new BigDecimal("20")
                )
        );

        List<ExpenseSplit> result = calculator.calculate(
                Money.of(new BigDecimal("300.00")),
                percentageSplits
        );

        assertThat(result)
                .extracting(ExpenseSplit::getAmount)
                .containsExactly(
                        Money.of(new BigDecimal("150.00")),
                        Money.of(new BigDecimal("90.00")),
                        Money.of(new BigDecimal("60.00"))
                );
    }

    @Test
    void shouldPreserveTotalExpenseAmount() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();

        List<PercentageSplit> percentageSplits = List.of(
                new PercentageSplit(
                        participant1,
                        new BigDecimal("33.33")
                ),
                new PercentageSplit(
                        participant2,
                        new BigDecimal("33.33")
                ),
                new PercentageSplit(
                        participant3,
                        new BigDecimal("33.34")
                )
        );

        List<ExpenseSplit> result = calculator.calculate(
                Money.of(new BigDecimal("100.00")),
                percentageSplits
        );

        Money total = result.stream()
                .map(ExpenseSplit::getAmount)
                .reduce(
                        Money.of(BigDecimal.ZERO),
                        Money::add
                );

        assertThat(total)
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

        List<PercentageSplit> percentageSplits = List.of(
                new PercentageSplit(
                        participant1,
                        new BigDecimal("33.33")
                ),
                new PercentageSplit(
                        participant2,
                        new BigDecimal("33.33")
                ),
                new PercentageSplit(
                        participant3,
                        new BigDecimal("33.34")
                )
        );

        List<ExpenseSplit> result = calculator.calculate(
                Money.of(new BigDecimal("0.05")),
                percentageSplits
        );

        assertThat(result)
                .extracting(ExpenseSplit::getAmount)
                .containsExactly(
                        Money.of(new BigDecimal("0.02")),
                        Money.of(new BigDecimal("0.02")),
                        Money.of(new BigDecimal("0.01"))
                );
    }

    @Test
    void shouldAllowZeroPercentage() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        List<PercentageSplit> percentageSplits = List.of(
                new PercentageSplit(
                        participant1,
                        new BigDecimal("100")
                ),
                new PercentageSplit(
                        participant2,
                        new BigDecimal("0")
                )
        );

        List<ExpenseSplit> result = calculator.calculate(
                Money.of(new BigDecimal("100.00")),
                percentageSplits
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

        List<PercentageSplit> percentageSplits = List.of(
                new PercentageSplit(
                        participant1,
                        new BigDecimal("50")
                ),
                new PercentageSplit(
                        participant2,
                        new BigDecimal("50")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        null,
                        percentageSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("expense amount must not be null");
    }

    @Test
    void shouldRejectNullPercentageSplits() {
        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        null
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("percentage splits must not be null");
    }

    @Test
    void shouldRejectLessThanTwoParticipants() {
        UUID participantId = UUID.randomUUID();

        List<PercentageSplit> percentageSplits = List.of(
                new PercentageSplit(
                        participantId,
                        new BigDecimal("100")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        percentageSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage(
                        "percentage split requires at least two participants"
                );
    }

    @Test
    void shouldRejectNullPercentageSplit() {
        UUID participantId = UUID.randomUUID();

        List<PercentageSplit> percentageSplits = Arrays.asList(
                new PercentageSplit(
                        participantId,
                        new BigDecimal("50")
                ),
                null
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        percentageSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("percentage split must not be null");
    }

    @Test
    void shouldRejectNullParticipantId() {
        List<PercentageSplit> percentageSplits = Arrays.asList(
                new PercentageSplit(
                        null,
                        new BigDecimal("50")
                ),
                new PercentageSplit(
                        UUID.randomUUID(),
                        new BigDecimal("50")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        percentageSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("participant id must not be null");
    }

    @Test
    void shouldRejectNullPercentage() {
        List<PercentageSplit> percentageSplits = Arrays.asList(
                new PercentageSplit(
                        UUID.randomUUID(),
                        null
                ),
                new PercentageSplit(
                        UUID.randomUUID(),
                        new BigDecimal("50")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        percentageSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("percentage must not be null");
    }

    @Test
    void shouldRejectNegativePercentage() {
        List<PercentageSplit> percentageSplits = List.of(
                new PercentageSplit(
                        UUID.randomUUID(),
                        new BigDecimal("-10")
                ),
                new PercentageSplit(
                        UUID.randomUUID(),
                        new BigDecimal("110")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        percentageSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("percentage must be between 0 and 100");
    }

    @Test
    void shouldRejectPercentageGreaterThan100() {
        List<PercentageSplit> percentageSplits = List.of(
                new PercentageSplit(
                        UUID.randomUUID(),
                        new BigDecimal("101")
                ),
                new PercentageSplit(
                        UUID.randomUUID(),
                        new BigDecimal("-1")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        percentageSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("percentage must be between 0 and 100");
    }

    @Test
    void shouldRejectPercentageWithMoreThanTwoDecimalPlaces() {
        List<PercentageSplit> percentageSplits = List.of(
                new PercentageSplit(
                        UUID.randomUUID(),
                        new BigDecimal("50.001")
                ),
                new PercentageSplit(
                        UUID.randomUUID(),
                        new BigDecimal("49.999")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        percentageSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage(
                        "percentage must have at most 2 decimal places"
                );
    }

    @Test
    void shouldRejectTotalPercentageLessThan100() {
        List<PercentageSplit> percentageSplits = List.of(
                new PercentageSplit(
                        UUID.randomUUID(),
                        new BigDecimal("40")
                ),
                new PercentageSplit(
                        UUID.randomUUID(),
                        new BigDecimal("50")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        percentageSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("total percentage must equal 100");
    }

    @Test
    void shouldRejectTotalPercentageGreaterThan100() {
        List<PercentageSplit> percentageSplits = List.of(
                new PercentageSplit(
                        UUID.randomUUID(),
                        new BigDecimal("60")
                ),
                new PercentageSplit(
                        UUID.randomUUID(),
                        new BigDecimal("50")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        percentageSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("total percentage must equal 100");
    }

    @Test
    void shouldRejectDuplicateParticipants() {
        UUID participantId = UUID.randomUUID();

        List<PercentageSplit> percentageSplits = List.of(
                new PercentageSplit(
                        participantId,
                        new BigDecimal("50")
                ),
                new PercentageSplit(
                        participantId,
                        new BigDecimal("50")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        percentageSplits
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("split participants must be unique");
    }
}