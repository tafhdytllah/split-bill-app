package com.tafhdev.split_bill_app.expense.service;

import com.tafhdev.split_bill_app.expense.controller.dto.request.CreateExpenseRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.request.ExactSplitRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.request.PercentageSplitRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.request.SplitRequest;
import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.expense.domain.SplitType;
import com.tafhdev.split_bill_app.expense.domain.split.*;
import com.tafhdev.split_bill_app.expense.repository.ExpenseRepository;
import com.tafhdev.split_bill_app.expense.service.dto.CreateExpenseResult;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {

    private final ExpenseRepository expenseRepository =
            mock(ExpenseRepository.class);

    private final BillGroupRepository billGroupRepository =
            mock(BillGroupRepository.class);

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
                    billGroupRepository,
                    exactSplitCalculator,
                    percentageSplitCalculator,
                    equalSplitCalculator,
                    idGenerator,
                    clock
            );

    @Test
    void shouldCreateExpenseUsingEqualSplit() {
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-09-16T10:00:00Z");

        Money amount = Money.of(new BigDecimal("300000.00"));

        Participant participant1Entity = Participant.createNew(
                participant1,
                groupId,
                "Taufik",
                createdAt
        );

        Participant participant2Entity = Participant.createNew(
                participant2,
                groupId,
                "Toni",
                createdAt
        );

        Participant participant3Entity = Participant.createNew(
                participant3,
                groupId,
                "Joko",
                createdAt
        );

        BillGroup billGroup = BillGroup.createNew(
                groupId,
                "Test Group",
                List.of(
                        participant1Entity,
                        participant2Entity,
                        participant3Entity
                ),
                createdAt
        );

        List<UUID> participantIds = List.of(
                participant1,
                participant2,
                participant3
        );

        List<ExpenseSplit> splits = List.of(
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        participant1,
                        Money.of(new BigDecimal("100000.00"))
                ),
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        participant2,
                        Money.of(new BigDecimal("100000.00"))
                ),
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        participant3,
                        Money.of(new BigDecimal("100000.00"))
                )
        );

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        participant1,
                        new BigDecimal("300000.00"),
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        new SplitRequest(
                                List.of(
                                        participant1,
                                        participant2,
                                        participant3
                                ),
                                null,
                                null
                        )
                );

        when(idGenerator.generate())
                .thenReturn(expenseId);

        when(clock.instant())
                .thenReturn(createdAt);

        when(billGroupRepository.findById(groupId))
                .thenReturn(Optional.of(billGroup));

        when(equalSplitCalculator.calculate(
                amount,
                participantIds
        )).thenReturn(splits);

        Expense expectedExpense = Expense.createNew(
                expenseId,
                groupId,
                participant1,
                amount,
                ExpenseCategory.FOOD,
                SplitType.EQUAL,
                splits,
                createdAt
        );

        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(expectedExpense);

        CreateExpenseResult result =
                expenseService.createExpense(
                        groupId,
                        request
                );

        assertThat(result.expense())
                .isEqualTo(expectedExpense);

        assertThat(result.participants())
                .containsExactly(
                        participant1Entity,
                        participant2Entity,
                        participant3Entity
                );

        verify(equalSplitCalculator)
                .calculate(amount, participantIds);

        verify(expenseRepository)
                .save(any(Expense.class));

        verify(billGroupRepository)
                .findById(groupId);

        verifyNoInteractions(
                exactSplitCalculator,
                percentageSplitCalculator
        );
    }

    @Test
    void shouldCreateExpenseUsingExactSplit() {
        UUID expenseId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-09-16T10:00:00Z");

        Money amount = Money.of(new BigDecimal("300.00"));

        Participant participant1Entity = Participant.createNew(
                participant1,
                groupId,
                "Taufik",
                createdAt
        );

        Participant participant2Entity = Participant.createNew(
                participant2,
                groupId,
                "Caky",
                createdAt
        );

        Participant participant3Entity = Participant.createNew(
                participant3,
                groupId,
                "Yoni",
                createdAt
        );

        BillGroup billGroup = BillGroup.createNew(
                groupId,
                "Test Group",
                List.of(
                        participant1Entity,
                        participant2Entity,
                        participant3Entity
                ),
                createdAt
        );

        List<ExactSplitRequest> exactSplitRequests = List.of(
                new ExactSplitRequest(
                        participant1,
                        new BigDecimal("210.00")
                ),
                new ExactSplitRequest(
                        participant2,
                        new BigDecimal("50.00")
                ),
                new ExactSplitRequest(
                        participant3,
                        new BigDecimal("40.00")
                )
        );

        List<ExactSplit> exactSplits = List.of(
                new ExactSplit(
                        participant1,
                        Money.of(new BigDecimal("210.00"))
                ),
                new ExactSplit(
                        participant2,
                        Money.of(new BigDecimal("50.00"))
                ),
                new ExactSplit(
                        participant3,
                        Money.of(new BigDecimal("40.00"))
                )
        );

        List<ExpenseSplit> splits = List.of(
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        participant1,
                        Money.of(new BigDecimal("210.00"))
                ),
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        participant2,
                        Money.of(new BigDecimal("50.00"))
                ),
                ExpenseSplit.createNew(
                        UUID.randomUUID(),
                        participant3,
                        Money.of(new BigDecimal("40.00"))
                )
        );

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        participant2,
                        new BigDecimal("300.00"),
                        ExpenseCategory.FOOD,
                        SplitType.EXACT,
                        new SplitRequest(
                                null,
                                exactSplitRequests,
                                null
                        )
                );

        when(idGenerator.generate())
                .thenReturn(expenseId);

        when(clock.instant())
                .thenReturn(createdAt);

        when(billGroupRepository.findById(groupId))
                .thenReturn(Optional.of(billGroup));

        when(exactSplitCalculator.calculate(
                amount,
                exactSplits
        )).thenReturn(splits);

        Expense expectedExpense = Expense.createNew(
                expenseId,
                groupId,
                participant2,
                amount,
                ExpenseCategory.FOOD,
                SplitType.EXACT,
                splits,
                createdAt
        );

        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(expectedExpense);

        CreateExpenseResult result =
                expenseService.createExpense(
                        groupId,
                        request
                );

        assertThat(result.expense())
                .isEqualTo(expectedExpense);

        assertThat(result.participants())
                .containsExactly(
                        participant1Entity,
                        participant2Entity,
                        participant3Entity
                );

        verify(exactSplitCalculator)
                .calculate(amount, exactSplits);

        verify(expenseRepository)
                .save(any(Expense.class));

        verify(billGroupRepository)
                .findById(groupId);

        verifyNoInteractions(
                equalSplitCalculator,
                percentageSplitCalculator
        );
    }

    @Test
    void shouldCreateExpenseUsingPercentageSplit() {
        UUID expenseId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        Instant createdAt =
                Instant.parse("2026-09-16T10:00:00Z");

        Money amount = Money.of(
                new BigDecimal("100.00")
        );

        Participant participant1Entity = Participant.createNew(
                participant1,
                groupId,
                "Taufik",
                createdAt
        );

        Participant participant2Entity = Participant.createNew(
                participant2,
                groupId,
                "Budi",
                createdAt
        );

        BillGroup billGroup = BillGroup.createNew(
                groupId,
                "Test Group",
                List.of(
                        participant1Entity,
                        participant2Entity
                ),
                createdAt
        );

        List<PercentageSplitRequest> percentageSplitRequests = List.of(
                new PercentageSplitRequest(
                        participant1,
                        new BigDecimal("60")
                ),
                new PercentageSplitRequest(
                        participant2,
                        new BigDecimal("40")
                )
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

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        participant2,
                        new BigDecimal("100.00"),
                        ExpenseCategory.FOOD,
                        SplitType.PERCENTAGE,
                        new SplitRequest(
                                null,
                                null,
                                percentageSplitRequests
                        )
                );

        when(idGenerator.generate())
                .thenReturn(expenseId);

        when(clock.instant())
                .thenReturn(createdAt);

        when(billGroupRepository.findById(groupId))
                .thenReturn(Optional.of(billGroup));

        when(percentageSplitCalculator.calculate(
                amount,
                percentageSplits
        )).thenReturn(splits);

        Expense expectedExpense = Expense.createNew(
                expenseId,
                groupId,
                participant2,
                amount,
                ExpenseCategory.FOOD,
                SplitType.PERCENTAGE,
                splits,
                createdAt
        );

        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(expectedExpense);

        CreateExpenseResult result =
                expenseService.createExpense(
                        groupId,
                        request
                );

        assertThat(result.expense())
                .isEqualTo(expectedExpense);

        assertThat(result.participants())
                .containsExactly(
                        participant1Entity,
                        participant2Entity
                );

        verify(percentageSplitCalculator)
                .calculate(amount, percentageSplits);

        verify(expenseRepository)
                .save(any(Expense.class));

        verify(billGroupRepository)
                .findById(groupId);

        verifyNoInteractions(
                equalSplitCalculator,
                exactSplitCalculator
        );
    }
}