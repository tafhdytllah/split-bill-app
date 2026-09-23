package com.tafhdev.split_bill_app.expense.service;

import com.tafhdev.split_bill_app.expense.controller.dto.request.CreateExpenseRequest;
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
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BillGroupRepository billGroupRepository;
    private final ExactSplitCalculator exactSplitCalculator;
    private final PercentageSplitCalculator percentageSplitCalculator;
    private final EqualSplitCalculator equalSplitCalculator;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            BillGroupRepository billGroupRepository,
            ExactSplitCalculator exactSplitCalculator,
            PercentageSplitCalculator percentageSplitCalculator,
            EqualSplitCalculator equalSplitCalculator,
            IdGenerator idGenerator,
            Clock clock
    ) {
        this.expenseRepository = expenseRepository;
        this.billGroupRepository = billGroupRepository;
        this.exactSplitCalculator = exactSplitCalculator;
        this.percentageSplitCalculator = percentageSplitCalculator;
        this.equalSplitCalculator = equalSplitCalculator;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Transactional
    public CreateExpenseResult createExpense(
            UUID groupId,
            CreateExpenseRequest request
    ) {
        UUID expenseId = idGenerator.generate();
        Instant createdAt = Instant.now(clock);
        Money amount = Money.of(request.amount());

        BillGroup billGroup = billGroupRepository.findById(groupId)
                .orElseThrow(() -> new DomainException("bill group not found"));

        Set<UUID> participantIds = billGroup.getParticipants()
                .stream()
                .map(Participant::getId)
                .collect(Collectors.toSet());

        validateParticipants(
                participantIds,
                request
        );

        List<ExpenseSplit> splits = switch (request.splitType()) {
            case EQUAL -> equalSplitCalculator.calculate(
                    amount,
                    request.split().participants()
            );

            case EXACT -> exactSplitCalculator.calculate(
                    amount,
                    request.split().exactSplits().stream()
                            .map(split -> new ExactSplit(
                                    split.participantId(),
                                    Money.of(split.amount())
                            ))
                            .toList()
            );

            case PERCENTAGE -> percentageSplitCalculator.calculate(
                    amount,
                    request.split().percentageSplits().stream()
                            .map(split -> new PercentageSplit(
                                    split.participantId(),
                                    split.percentage()
                            ))
                            .toList()
            );
        };

        Expense expense = Expense.createNew(
                expenseId,
                groupId,
                request.paidBy(),
                amount,
                request.category(),
                request.splitType(),
                splits,
                createdAt
        );

        Expense savedExpense = expenseRepository.save(expense);

        return new CreateExpenseResult(
                savedExpense,
                billGroup.getParticipants()
        );
    }

    private void validateParticipants(
            Set<UUID> groupParticipantIds,
            CreateExpenseRequest request
    ) {
        if (!groupParticipantIds.contains(request.paidBy())) {
            throw new DomainException(
                    "paid by participant does not belong to group"
            );
        }

        switch (request.splitType()) {
            case EQUAL -> request.split().participants()
                    .forEach(participantId ->
                            validateParticipant(
                                    groupParticipantIds,
                                    participantId
                            )
                    );

            case EXACT -> request.split().exactSplits()
                    .forEach(split ->
                            validateParticipant(
                                    groupParticipantIds,
                                    split.participantId()
                            )
                    );

            case PERCENTAGE -> request.split().percentageSplits()
                    .forEach(split ->
                            validateParticipant(
                                    groupParticipantIds,
                                    split.participantId()
                            )
                    );
        }
    }

    private void validateParticipant(
            Set<UUID> groupParticipantIds,
            UUID participantId
    ) {
        if (!groupParticipantIds.contains(participantId)) {
            throw new DomainException(
                    "participant does not belong to group"
            );
        }
    }
}
