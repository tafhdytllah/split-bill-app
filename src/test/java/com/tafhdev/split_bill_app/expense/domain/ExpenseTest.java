package com.tafhdev.split_bill_app.expense.domain;

import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpenseTest {

    private static final UUID EXPENSE_ID = UUID.randomUUID();
    private static final UUID GROUP_ID = UUID.randomUUID();
    private static final UUID PAID_BY = UUID.randomUUID();
    private static final UUID PARTICIPANT_1 = UUID.randomUUID();
    private static final UUID PARTICIPANT_2 = UUID.randomUUID();
    private static final Instant CREATED_AT =
            Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void shouldCreateExpense() {
        Money amount = Money.of(new BigDecimal("300000.00"));

        ExpenseSplit split1 = ExpenseSplit.createNew(
                UUID.randomUUID(),
                PARTICIPANT_1,
                Money.of(new BigDecimal("150000.00"))
        );

        ExpenseSplit split2 = ExpenseSplit.createNew(
                UUID.randomUUID(),
                PARTICIPANT_2,
                Money.of(new BigDecimal("150000.00"))
        );

        Expense expense = Expense.createNew(
                EXPENSE_ID,
                GROUP_ID,
                PAID_BY,
                amount,
                ExpenseCategory.FOOD,
                SplitType.EQUAL,
                List.of(split1, split2),
                CREATED_AT
        );

        assertThat(expense.getId())
                .isEqualTo(EXPENSE_ID);

        assertThat(expense.getGroupId())
                .isEqualTo(GROUP_ID);

        assertThat(expense.getPaidBy())
                .isEqualTo(PAID_BY);

        assertThat(expense.getAmount())
                .isEqualTo(amount);

        assertThat(expense.getCategory())
                .isEqualTo(ExpenseCategory.FOOD);

        assertThat(expense.getSplitType())
                .isEqualTo(SplitType.EQUAL);

        assertThat(expense.getSplits())
                .containsExactly(split1, split2);

        assertThat(expense.getCreatedAt())
                .isEqualTo(CREATED_AT);
    }

    @Test
    void shouldRejectNullId() {
        assertThatThrownBy(() ->
                Expense.createNew(
                        null,
                        GROUP_ID,
                        PAID_BY,
                        Money.of(new BigDecimal("100.00")),
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        validSplits(),
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("id must not be null");
    }

    @Test
    void shouldRejectNullGroupId() {
        assertThatThrownBy(() ->
                Expense.createNew(
                        EXPENSE_ID,
                        null,
                        PAID_BY,
                        Money.of(new BigDecimal("100.00")),
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        validSplits(),
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("group id must not be null");
    }

    @Test
    void shouldRejectNullPaidBy() {
        assertThatThrownBy(() ->
                Expense.createNew(
                        EXPENSE_ID,
                        GROUP_ID,
                        null,
                        Money.of(new BigDecimal("100.00")),
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        validSplits(),
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("paid by must not be null");
    }

    @Test
    void shouldRejectNullAmount() {
        assertThatThrownBy(() ->
                Expense.createNew(
                        EXPENSE_ID,
                        GROUP_ID,
                        PAID_BY,
                        null,
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        validSplits(),
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("amount must not be null");
    }

    @Test
    void shouldRejectZeroAmount() {
        assertThatThrownBy(() ->
                Expense.createNew(
                        EXPENSE_ID,
                        GROUP_ID,
                        PAID_BY,
                        Money.of(new BigDecimal("0.00")),
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        validSplits(),
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("expense amount must be greater than zero");
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() ->
                Expense.createNew(
                        EXPENSE_ID,
                        GROUP_ID,
                        PAID_BY,
                        Money.of(new BigDecimal("-100.00")),
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        validSplits(),
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("expense amount must be greater than zero");
    }

    @Test
    void shouldRejectNullCategory() {
        assertThatThrownBy(() ->
                Expense.createNew(
                        EXPENSE_ID,
                        GROUP_ID,
                        PAID_BY,
                        Money.of(new BigDecimal("100.00")),
                        null,
                        SplitType.EQUAL,
                        validSplits(),
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("category must not be null");
    }

    @Test
    void shouldRejectNullSplitType() {
        assertThatThrownBy(() ->
                Expense.createNew(
                        EXPENSE_ID,
                        GROUP_ID,
                        PAID_BY,
                        Money.of(new BigDecimal("100.00")),
                        ExpenseCategory.FOOD,
                        null,
                        validSplits(),
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("split type must not be null");
    }

    @Test
    void shouldRejectNullSplits() {
        assertThatThrownBy(() ->
                Expense.createNew(
                        EXPENSE_ID,
                        GROUP_ID,
                        PAID_BY,
                        Money.of(new BigDecimal("100.00")),
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        null,
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("splits must not be null");
    }

    @Test
    void shouldRejectLessThanTwoSplits() {
        List<ExpenseSplit> splits = List.of(
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        PARTICIPANT_1,
                        Money.of(new BigDecimal("100.00"))
                )
        );

        assertThatThrownBy(() ->
                Expense.createNew(
                        EXPENSE_ID,
                        GROUP_ID,
                        PAID_BY,
                        Money.of(new BigDecimal("100.00")),
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        splits,
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("expense must have at least two split members");
    }

    @Test
    void shouldRejectDuplicateSplitParticipants() {
        ExpenseSplit split1 = ExpenseSplit.createNew(
                UUID.randomUUID(),
                PARTICIPANT_1,
                Money.of(new BigDecimal("50.00"))
        );

        ExpenseSplit split2 = ExpenseSplit.createNew(
                UUID.randomUUID(),
                PARTICIPANT_1,
                Money.of(new BigDecimal("50.00"))
        );

        assertThatThrownBy(() ->
                Expense.createNew(
                        EXPENSE_ID,
                        GROUP_ID,
                        PAID_BY,
                        Money.of(new BigDecimal("100.00")),
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        List.of(split1, split2),
                        CREATED_AT
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("split participants must be unique");
    }

    @Test
    void shouldRejectNullCreatedAt() {
        assertThatThrownBy(() ->
                Expense.createNew(
                        EXPENSE_ID,
                        GROUP_ID,
                        PAID_BY,
                        Money.of(new BigDecimal("100.00")),
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        validSplits(),
                        null
                )
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("created at must not be null");
    }

    @Test
    void shouldReturnUnmodifiableSplits() {
        Expense expense = Expense.createNew(
                EXPENSE_ID,
                GROUP_ID,
                PAID_BY,
                Money.of(new BigDecimal("100.00")),
                ExpenseCategory.FOOD,
                SplitType.EQUAL,
                validSplits(),
                CREATED_AT
        );

        assertThatThrownBy(() ->
                expense.getSplits().clear()
        )
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldReconstituteExpense() {
        List<ExpenseSplit> splits = validSplits();

        Expense expense = Expense.reconstitute(
                EXPENSE_ID,
                GROUP_ID,
                PAID_BY,
                Money.of(new BigDecimal("100.00")),
                ExpenseCategory.FOOD,
                SplitType.EQUAL,
                splits,
                CREATED_AT
        );

        assertThat(expense.getId())
                .isEqualTo(EXPENSE_ID);

        assertThat(expense.getSplits())
                .containsExactlyElementsOf(splits);
    }

    private static List<ExpenseSplit> validSplits() {
        return List.of(
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        PARTICIPANT_1,
                        Money.of(new BigDecimal("50.00"))
                ),
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        PARTICIPANT_2,
                        Money.of(new BigDecimal("50.00"))
                )
        );
    }
}