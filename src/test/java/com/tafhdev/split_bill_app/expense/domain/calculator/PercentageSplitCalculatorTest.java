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

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        participant1,
                        null,
                        new BigDecimal("50")
                ),
                new SplitParticipant(
                        participant2,
                        null,
                        new BigDecimal("30")
                ),
                new SplitParticipant(
                        participant3,
                        null,
                        new BigDecimal("20")
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

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("33.33")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("33.33")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("33.34")
                )
        );

        List<ExpenseSplit> result = calculator.calculate(
                Money.of(new BigDecimal("100.00")),
                participants
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

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("33.33")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("33.33")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("33.34")
                )
        );

        List<ExpenseSplit> result = calculator.calculate(
                Money.of(new BigDecimal("0.05")),
                participants
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

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("100")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("0")
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
                        null,
                        new BigDecimal("50")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("50")
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
                        null,
                        new BigDecimal("100")
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
                        "percentage split requires at least two participants"
                );
    }

    @Test
    void shouldRejectNullParticipant() {
        List<SplitParticipant> participants = Arrays.asList(
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("50")
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
                        null,
                        new BigDecimal("50")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("50")
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
    void shouldRejectNullPercentage() {
        List<SplitParticipant> participants = Arrays.asList(
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        null
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("50")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        participants
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("split percentage must not be null");
    }

    @Test
    void shouldRejectAmountInPercentageSplit() {
        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        Money.of(new BigDecimal("50.00")),
                        new BigDecimal("50")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("50")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        participants
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("percentage split must not have amount");
    }

    @Test
    void shouldRejectNegativePercentage() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("-10")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("110")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        participants
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("percentage must be between 0 and 100");
    }

    @Test
    void shouldRejectPercentageGreaterThan100() {
        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("101")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("-1")
                )
        );

        assertThatThrownBy(() ->
                calculator.calculate(
                        Money.of(new BigDecimal("100.00")),
                        participants
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("percentage must be between 0 and 100");
    }

    @Test
    void shouldRejectPercentageWithMoreThanTwoDecimalPlaces() {
        when(idGenerator.generate())
                .thenReturn(
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("50.001")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("49.999")
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
                        "percentage must have at most 2 decimal places"
                );
    }

    @Test
    void shouldRejectTotalPercentageLessThan100() {
        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("40")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("50")
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
                        "percentage split must equal 100%"
                );
    }

    @Test
    void shouldRejectTotalPercentageGreaterThan100() {
        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("60")
                ),
                new SplitParticipant(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("50")
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
                        "percentage split must equal 100%"
                );
    }

    @Test
    void shouldRejectDuplicateParticipants() {
        UUID participantId = UUID.randomUUID();

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        participantId,
                        null,
                        new BigDecimal("50")
                ),
                new SplitParticipant(
                        participantId,
                        null,
                        new BigDecimal("50")
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
}