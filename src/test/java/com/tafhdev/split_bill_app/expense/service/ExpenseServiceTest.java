package com.tafhdev.split_bill_app.expense.service;

import com.tafhdev.split_bill_app.expense.controller.dto.request.CreateExpenseRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.request.SplitParticipantRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.request.SplitRequest;
import com.tafhdev.split_bill_app.expense.domain.*;
import com.tafhdev.split_bill_app.expense.domain.calculator.SplitCalculator;
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

    private final SplitCalculatorResolver splitCalculatorResolver =
            mock(SplitCalculatorResolver.class);

    private final IdGenerator idGenerator =
            mock(IdGenerator.class);

    private final Clock clock =
            mock(Clock.class);

    private final SplitCalculator splitCalculator =
            mock(SplitCalculator.class);

    private final ExpenseService expenseService =
            new ExpenseService(
                    expenseRepository,
                    billGroupRepository,
                    splitCalculatorResolver,
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

        Instant createdAt =
                Instant.parse("2026-09-16T10:00:00Z");

        Money amount =
                Money.of(new BigDecimal("300000.00"));

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

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        participant1,
                        null,
                        null
                ),
                new SplitParticipant(
                        participant2,
                        null,
                        null
                ),
                new SplitParticipant(
                        participant3,
                        null,
                        null
                )
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
                        new SplitRequest(
                                SplitType.EQUAL,
                                List.of(
                                        new SplitParticipantRequest(
                                                participant1,
                                                null,
                                                null
                                        ),
                                        new SplitParticipantRequest(
                                                participant2,
                                                null,
                                                null
                                        ),
                                        new SplitParticipantRequest(
                                                participant3,
                                                null,
                                                null
                                        )
                                )
                        )
                );

        when(idGenerator.generate())
                .thenReturn(expenseId);

        when(clock.instant())
                .thenReturn(createdAt);

        when(billGroupRepository.findById(groupId))
                .thenReturn(Optional.of(billGroup));

        when(splitCalculatorResolver.resolve(
                SplitType.EQUAL
        )).thenReturn(splitCalculator);

        when(splitCalculator.calculate(
                amount,
                participants
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

        verify(splitCalculatorResolver)
                .resolve(SplitType.EQUAL);

        verify(splitCalculator)
                .calculate(amount, participants);

        verify(expenseRepository)
                .save(any(Expense.class));

        verify(billGroupRepository)
                .findById(groupId);
    }

    @Test
    void shouldCreateExpenseUsingExactSplit() {
        UUID expenseId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();

        Instant createdAt =
                Instant.parse("2026-09-16T10:00:00Z");

        Money amount =
                Money.of(new BigDecimal("300.00"));

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

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        participant1,
                        Money.of(new BigDecimal("210.00")),
                        null
                ),
                new SplitParticipant(
                        participant2,
                        Money.of(new BigDecimal("50.00")),
                        null
                ),
                new SplitParticipant(
                        participant3,
                        Money.of(new BigDecimal("40.00")),
                        null
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
                        new SplitRequest(
                                SplitType.EXACT,
                                List.of(
                                        new SplitParticipantRequest(
                                                participant1,
                                                new BigDecimal("210.00"),
                                                null
                                        ),
                                        new SplitParticipantRequest(
                                                participant2,
                                                new BigDecimal("50.00"),
                                                null
                                        ),
                                        new SplitParticipantRequest(
                                                participant3,
                                                new BigDecimal("40.00"),
                                                null
                                        )
                                )
                        )
                );

        when(idGenerator.generate())
                .thenReturn(expenseId);

        when(clock.instant())
                .thenReturn(createdAt);

        when(billGroupRepository.findById(groupId))
                .thenReturn(Optional.of(billGroup));

        when(splitCalculatorResolver.resolve(
                SplitType.EXACT
        )).thenReturn(splitCalculator);

        when(splitCalculator.calculate(
                amount,
                participants
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

        verify(splitCalculatorResolver)
                .resolve(SplitType.EXACT);

        verify(splitCalculator)
                .calculate(amount, participants);

        verify(expenseRepository)
                .save(any(Expense.class));

        verify(billGroupRepository)
                .findById(groupId);
    }

    @Test
    void shouldCreateExpenseUsingPercentageSplit() {
        UUID expenseId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        Instant createdAt =
                Instant.parse("2026-09-16T10:00:00Z");

        Money amount =
                Money.of(new BigDecimal("100.00"));

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

        List<SplitParticipant> participants = List.of(
                new SplitParticipant(
                        participant1,
                        null,
                        new BigDecimal("60")
                ),
                new SplitParticipant(
                        participant2,
                        null,
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
                        new SplitRequest(
                                SplitType.PERCENTAGE,
                                List.of(
                                        new SplitParticipantRequest(
                                                participant1,
                                                null,
                                                new BigDecimal("60")
                                        ),
                                        new SplitParticipantRequest(
                                                participant2,
                                                null,
                                                new BigDecimal("40")
                                        )
                                )
                        )
                );

        when(idGenerator.generate())
                .thenReturn(expenseId);

        when(clock.instant())
                .thenReturn(createdAt);

        when(billGroupRepository.findById(groupId))
                .thenReturn(Optional.of(billGroup));

        when(splitCalculatorResolver.resolve(
                SplitType.PERCENTAGE
        )).thenReturn(splitCalculator);

        when(splitCalculator.calculate(
                amount,
                participants
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

        verify(splitCalculatorResolver)
                .resolve(SplitType.PERCENTAGE);

        verify(splitCalculator)
                .calculate(amount, participants);

        verify(expenseRepository)
                .save(any(Expense.class));

        verify(billGroupRepository)
                .findById(groupId);
    }
}