package com.tafhdev.split_bill_app.expense.service;

import com.tafhdev.split_bill_app.audit.repository.AuditLogRepository;
import com.tafhdev.split_bill_app.expense.controller.dto.ExpenseResponse;
import com.tafhdev.split_bill_app.expense.controller.mapper.ExpenseApiMapper;
import com.tafhdev.split_bill_app.expense.domain.*;
import com.tafhdev.split_bill_app.expense.domain.calculator.SplitCalculator;
import com.tafhdev.split_bill_app.expense.repository.ExpenseRepository;
import com.tafhdev.split_bill_app.expense.service.dto.CreateExpenseCommand;
import com.tafhdev.split_bill_app.expense.service.dto.ExpenseResult;
import com.tafhdev.split_bill_app.expense.service.dto.SplitCommand;
import com.tafhdev.split_bill_app.expense.service.dto.SplitParticipantCommand;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyHashGenerator;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyService;
import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;
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

    private final AuditLogRepository auditLogRepository =
            mock(AuditLogRepository.class);

    private final IdempotencyService idempotencyService =
            mock(IdempotencyService.class);

    private final IdempotencyHashGenerator idempotencyHashGenerator =
            mock(IdempotencyHashGenerator.class);

    private final SplitCalculatorResolver splitCalculatorResolver =
            mock(SplitCalculatorResolver.class);

    private final IdGenerator idGenerator =
            mock(IdGenerator.class);

    private final Clock clock =
            mock(Clock.class);

    private final ExpenseApiMapper expenseApiMapper =
            mock(ExpenseApiMapper.class);

    private final SplitCalculator splitCalculator =
            mock(SplitCalculator.class);

    private final ExpenseService expenseService =
            new ExpenseService(
                    expenseRepository,
                    billGroupRepository,
                    auditLogRepository,
                    idempotencyService,
                    idempotencyHashGenerator,
                    splitCalculatorResolver,
                    idGenerator,
                    clock,
                    expenseApiMapper
            );

    @Test
    void shouldCreateExpenseUsingEqualSplit() {
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();

        UUID expenseId = UUID.randomUUID();
        UUID auditLogId = UUID.randomUUID();
        UUID idempotencyId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Instant createdAt =
                Instant.parse("2026-09-16T10:00:00Z");

        Money amount =
                Money.of(new BigDecimal("300000.00"));

        String idempotencyKey = "expense-equal-001";
        String requestHash = "request-hash-equal";

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

        List<SplitParticipantCommand> participants = List.of(
                new SplitParticipantCommand(
                        participant1,
                        null,
                        null
                ),
                new SplitParticipantCommand(
                        participant2,
                        null,
                        null
                ),
                new SplitParticipantCommand(
                        participant3,
                        null,
                        null
                )
        );

        List<SplitParticipant> calculatorParticipants = List.of(
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

        CreateExpenseCommand command =
                new CreateExpenseCommand(
                        idempotencyKey,
                        groupId,
                        participant1,
                        amount,
                        ExpenseCategory.FOOD,
                        new SplitCommand(
                                ExpenseSplitType.EQUAL,
                                participants
                        )
                );

        ExpenseResponse response = mock(ExpenseResponse.class);

        Idempotency idempotency = mock(Idempotency.class);

        when(idempotencyService.find(
                IdempotencyScope.EXPENSE,
                idempotencyKey
        )).thenReturn(Optional.empty());

        when(idempotencyHashGenerator.generate(any(String.class)))
                .thenReturn(requestHash);

        when(idempotencyService.create(
                IdempotencyScope.EXPENSE,
                idempotencyKey,
                requestHash
        )).thenReturn(idempotency);

        when(idGenerator.generate())
                .thenReturn(expenseId, auditLogId, idempotencyId);

        when(clock.instant())
                .thenReturn(createdAt);

        when(billGroupRepository.findById(groupId))
                .thenReturn(Optional.of(billGroup));

        when(splitCalculatorResolver.resolve(
                ExpenseSplitType.EQUAL
        )).thenReturn(splitCalculator);

        when(splitCalculator.calculate(
                amount,
                calculatorParticipants
        )).thenReturn(splits);

        Expense expectedExpense = Expense.createNew(
                expenseId,
                groupId,
                participant1,
                amount,
                ExpenseCategory.FOOD,
                ExpenseSplitType.EQUAL,
                splits,
                createdAt
        );

        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(expectedExpense);

        when(expenseApiMapper.toResponse(
                expectedExpense,
                billGroup.getParticipants()
        )).thenReturn(response);

        ExpenseResult result =
                expenseService.createExpense(command);

        assertThat(result.response())
                .isEqualTo(response);

        assertThat(result.reply())
                .isFalse();

        verify(idempotencyService)
                .find(
                        IdempotencyScope.EXPENSE,
                        idempotencyKey
                );

        verify(idempotencyHashGenerator)
                .generate(any(String.class));

        verify(idempotencyService)
                .create(
                        IdempotencyScope.EXPENSE,
                        idempotencyKey,
                        requestHash
                );

        verify(billGroupRepository)
                .findById(groupId);

        verify(splitCalculatorResolver)
                .resolve(ExpenseSplitType.EQUAL);

        verify(splitCalculator)
                .calculate(amount, calculatorParticipants);

        verify(expenseRepository)
                .save(any(Expense.class));

        verify(auditLogRepository)
                .save(any());

        verify(expenseApiMapper)
                .toResponse(
                        expectedExpense,
                        billGroup.getParticipants()
                );

        verify(idempotencyService)
                .complete(
                        idempotency,
                        201,
                        response
                );
    }

    @Test
    void shouldCreateExpenseUsingExactSplit() {
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();

        UUID expenseId = UUID.randomUUID();
        UUID auditLogId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Instant createdAt =
                Instant.parse("2026-09-16T10:00:00Z");

        Money amount =
                Money.of(new BigDecimal("300.00"));

        String idempotencyKey = "expense-exact-001";
        String requestHash = "request-hash-exact";

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

        List<SplitParticipantCommand> participants = List.of(
                new SplitParticipantCommand(
                        participant1,
                        Money.of(new BigDecimal("210.00")),
                        null
                ),
                new SplitParticipantCommand(
                        participant2,
                        Money.of(new BigDecimal("50.00")),
                        null
                ),
                new SplitParticipantCommand(
                        participant3,
                        Money.of(new BigDecimal("40.00")),
                        null
                )
        );

        List<SplitParticipant> calculatorParticipants = List.of(
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

        CreateExpenseCommand command =
                new CreateExpenseCommand(
                        idempotencyKey,
                        groupId,
                        participant2,
                        amount,
                        ExpenseCategory.FOOD,
                        new SplitCommand(
                                ExpenseSplitType.EXACT,
                                participants
                        )
                );

        ExpenseResponse response = mock(ExpenseResponse.class);

        Idempotency idempotency = mock(Idempotency.class);

        when(idempotencyService.find(
                IdempotencyScope.EXPENSE,
                idempotencyKey
        )).thenReturn(Optional.empty());

        when(idempotencyHashGenerator.generate(any(String.class)))
                .thenReturn(requestHash);

        when(idempotencyService.create(
                IdempotencyScope.EXPENSE,
                idempotencyKey,
                requestHash
        )).thenReturn(idempotency);

        when(idGenerator.generate())
                .thenReturn(expenseId, auditLogId);

        when(clock.instant())
                .thenReturn(createdAt);

        when(billGroupRepository.findById(groupId))
                .thenReturn(Optional.of(billGroup));

        when(splitCalculatorResolver.resolve(
                ExpenseSplitType.EXACT
        )).thenReturn(splitCalculator);

        when(splitCalculator.calculate(
                amount,
                calculatorParticipants
        )).thenReturn(splits);

        Expense expectedExpense = Expense.createNew(
                expenseId,
                groupId,
                participant2,
                amount,
                ExpenseCategory.FOOD,
                ExpenseSplitType.EXACT,
                splits,
                createdAt
        );

        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(expectedExpense);

        when(expenseApiMapper.toResponse(
                expectedExpense,
                billGroup.getParticipants()
        )).thenReturn(response);

        ExpenseResult result =
                expenseService.createExpense(command);

        assertThat(result.response())
                .isEqualTo(response);

        assertThat(result.reply())
                .isFalse();

        verify(idempotencyService)
                .find(
                        IdempotencyScope.EXPENSE,
                        idempotencyKey
                );

        verify(idempotencyHashGenerator)
                .generate(any(String.class));

        verify(idempotencyService)
                .create(
                        IdempotencyScope.EXPENSE,
                        idempotencyKey,
                        requestHash
                );

        verify(billGroupRepository)
                .findById(groupId);

        verify(splitCalculatorResolver)
                .resolve(ExpenseSplitType.EXACT);

        verify(splitCalculator)
                .calculate(amount, calculatorParticipants);

        verify(expenseRepository)
                .save(any(Expense.class));

        verify(auditLogRepository)
                .save(any());

        verify(expenseApiMapper)
                .toResponse(
                        expectedExpense,
                        billGroup.getParticipants()
                );

        verify(idempotencyService)
                .complete(
                        idempotency,
                        201,
                        response
                );
    }

    @Test
    void shouldCreateExpenseUsingPercentageSplit() {
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        UUID expenseId = UUID.randomUUID();
        UUID auditLogId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        Instant createdAt =
                Instant.parse("2026-09-16T10:00:00Z");

        Money amount =
                Money.of(new BigDecimal("100.00"));

        String idempotencyKey = "expense-percentage-001";
        String requestHash = "request-hash-percentage";

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

        List<SplitParticipantCommand> participants = List.of(
                new SplitParticipantCommand(
                        participant1,
                        null,
                        new BigDecimal("60")
                ),
                new SplitParticipantCommand(
                        participant2,
                        null,
                        new BigDecimal("40")
                )
        );

        List<SplitParticipant> calculatorParticipants = List.of(
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

        CreateExpenseCommand command =
                new CreateExpenseCommand(
                        idempotencyKey,
                        groupId,
                        participant2,
                        amount,
                        ExpenseCategory.FOOD,
                        new SplitCommand(
                                ExpenseSplitType.PERCENTAGE,
                                participants
                        )
                );

        ExpenseResponse response = mock(ExpenseResponse.class);

        Idempotency idempotency = mock(Idempotency.class);

        when(idempotencyService.find(
                IdempotencyScope.EXPENSE,
                idempotencyKey
        )).thenReturn(Optional.empty());

        when(idempotencyHashGenerator.generate(any(String.class)))
                .thenReturn(requestHash);

        when(idempotencyService.create(
                IdempotencyScope.EXPENSE,
                idempotencyKey,
                requestHash
        )).thenReturn(idempotency);

        when(idGenerator.generate())
                .thenReturn(expenseId, auditLogId);

        when(clock.instant())
                .thenReturn(createdAt);

        when(billGroupRepository.findById(groupId))
                .thenReturn(Optional.of(billGroup));

        when(splitCalculatorResolver.resolve(
                ExpenseSplitType.PERCENTAGE
        )).thenReturn(splitCalculator);

        when(splitCalculator.calculate(
                amount,
                calculatorParticipants
        )).thenReturn(splits);

        Expense expectedExpense = Expense.createNew(
                expenseId,
                groupId,
                participant2,
                amount,
                ExpenseCategory.FOOD,
                ExpenseSplitType.PERCENTAGE,
                splits,
                createdAt
        );

        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(expectedExpense);

        when(expenseApiMapper.toResponse(
                expectedExpense,
                billGroup.getParticipants()
        )).thenReturn(response);

        ExpenseResult result =
                expenseService.createExpense(command);

        assertThat(result.response())
                .isEqualTo(response);

        assertThat(result.reply())
                .isFalse();

        verify(idempotencyService)
                .find(
                        IdempotencyScope.EXPENSE,
                        idempotencyKey
                );

        verify(idempotencyHashGenerator)
                .generate(any(String.class));

        verify(idempotencyService)
                .create(
                        IdempotencyScope.EXPENSE,
                        idempotencyKey,
                        requestHash
                );

        verify(billGroupRepository)
                .findById(groupId);

        verify(splitCalculatorResolver)
                .resolve(ExpenseSplitType.PERCENTAGE);

        verify(splitCalculator)
                .calculate(amount, calculatorParticipants);

        verify(expenseRepository)
                .save(any(Expense.class));

        verify(auditLogRepository)
                .save(any());

        verify(expenseApiMapper)
                .toResponse(
                        expectedExpense,
                        billGroup.getParticipants()
                );

        verify(idempotencyService)
                .complete(
                        idempotency,
                        201,
                        response
                );
    }
}