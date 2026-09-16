package com.tafhdev.split_bill_app.expense.service;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.expense.domain.SplitType;
import com.tafhdev.split_bill_app.expense.domain.split.*;
import com.tafhdev.split_bill_app.expense.repository.ExpenseRepository;
import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {

    private final ExpenseRepository expenseRepository =
            mock(ExpenseRepository.class);

    private final IdGenerator idGenerator =
            mock(IdGenerator.class);

    private final Clock clock =
            mock(Clock.class);

    private final EqualSplitCalculator equalSplitCalculator =
            mock(EqualSplitCalculator.class);

    private final ExactSplitCalculator exactSplitCalculator =
            mock(ExactSplitCalculator.class);

    private final PercentageSplitCalculator percentageSplitCalculator =
            mock(PercentageSplitCalculator.class);

    private final ExpenseService expenseService =
            new ExpenseService(
                    expenseRepository,
                    exactSplitCalculator,
                    percentageSplitCalculator,
                    equalSplitCalculator,
                    idGenerator,
                    clock
            );

    @Test
    void shouldCreateExpenseUsingEqualSplit() {
        UUID expenseId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        Instant createdAt =
                Instant.parse("2026-09-16T10:00:00Z");

        Money amount = Money.of(
                new BigDecimal("100.00")
        );

        List<UUID> participantIds =
                List.of(participant1, participant2);

        List<ExpenseSplit> splits = List.of(
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        participant1,
                        Money.of(new BigDecimal("50.00"))
                ),
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        participant2,
                        Money.of(new BigDecimal("50.00"))
                )
        );

        when(idGenerator.generate())
                .thenReturn(expenseId);

        when(clock.instant())
                .thenReturn(createdAt);

        when(equalSplitCalculator.calculate(
                amount,
                participantIds
        )).thenReturn(splits);

        Expense expectedExpense = Expense.createNew(
                expenseId,
                groupId,
                paidBy,
                amount,
                ExpenseCategory.FOOD,
                SplitType.EQUAL,
                splits,
                createdAt
        );

        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(expectedExpense);

        Expense result = expenseService.createExpense(
                groupId,
                paidBy,
                amount,
                ExpenseCategory.FOOD,
                SplitType.EQUAL,
                participantIds,
                null,
                null
        );

        assertThat(result)
                .isEqualTo(expectedExpense);

        verify(equalSplitCalculator)
                .calculate(amount, participantIds);

        verify(expenseRepository)
                .save(any(Expense.class));

        verifyNoInteractions(
                exactSplitCalculator,
                percentageSplitCalculator
        );
    }

    @Test
    void shouldCreateExpenseUsingExactSplit() {
        UUID expenseId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        Instant createdAt =
                Instant.parse("2026-09-16T10:00:00Z");

        Money amount = Money.of(
                new BigDecimal("100.00")
        );

        List<ExactSplit> exactSplits = List.of(
                new ExactSplit(
                        participant1,
                        Money.of(new BigDecimal("60.00"))
                ),
                new ExactSplit(
                        participant2,
                        Money.of(new BigDecimal("40.00"))
                )
        );

        List<ExpenseSplit> splits = List.of(
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        participant1,
                        Money.of(new BigDecimal("60.00"))
                ),
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        participant2,
                        Money.of(new BigDecimal("40.00"))
                )
        );

        when(idGenerator.generate())
                .thenReturn(expenseId);

        when(clock.instant())
                .thenReturn(createdAt);

        when(exactSplitCalculator.calculate(
                amount,
                exactSplits
        )).thenReturn(splits);

        Expense expectedExpense = Expense.createNew(
                expenseId,
                groupId,
                paidBy,
                amount,
                ExpenseCategory.FOOD,
                SplitType.EXACT,
                splits,
                createdAt
        );

        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(expectedExpense);

        Expense result = expenseService.createExpense(
                groupId,
                paidBy,
                amount,
                ExpenseCategory.FOOD,
                SplitType.EXACT,
                null,
                exactSplits,
                null
        );

        assertThat(result)
                .isEqualTo(expectedExpense);

        verify(exactSplitCalculator)
                .calculate(amount, exactSplits);

        verify(expenseRepository)
                .save(any(Expense.class));

        verifyNoInteractions(
                equalSplitCalculator,
                percentageSplitCalculator
        );
    }

    @Test
    void shouldCreateExpenseUsingPercentageSplit() {
        UUID expenseId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        Instant createdAt =
                Instant.parse("2026-09-16T10:00:00Z");

        Money amount = Money.of(
                new BigDecimal("100.00")
        );

        List<PercentageSplit> percentageSplits = List.of(
                new PercentageSplit(
                        participant1,
                        new BigDecimal("60")
                ),
                new PercentageSplit(
                        participant2,
                        new BigDecimal("40")
                )
        );

        List<ExpenseSplit> splits = List.of(
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        participant1,
                        Money.of(new BigDecimal("60.00"))
                ),
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        participant2,
                        Money.of(new BigDecimal("40.00"))
                )
        );

        when(idGenerator.generate())
                .thenReturn(expenseId);

        when(clock.instant())
                .thenReturn(createdAt);

        when(percentageSplitCalculator.calculate(
                amount,
                percentageSplits
        )).thenReturn(splits);

        Expense expectedExpense = Expense.createNew(
                expenseId,
                groupId,
                paidBy,
                amount,
                ExpenseCategory.FOOD,
                SplitType.PERCENTAGE,
                splits,
                createdAt
        );

        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(expectedExpense);

        Expense result = expenseService.createExpense(
                groupId,
                paidBy,
                amount,
                ExpenseCategory.FOOD,
                SplitType.PERCENTAGE,
                null,
                null,
                percentageSplits
        );

        assertThat(result)
                .isEqualTo(expectedExpense);

        verify(percentageSplitCalculator)
                .calculate(amount, percentageSplits);

        verify(expenseRepository)
                .save(any(Expense.class));

        verifyNoInteractions(
                equalSplitCalculator,
                exactSplitCalculator
        );
    }
}