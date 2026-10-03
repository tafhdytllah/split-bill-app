package com.tafhdev.split_bill_app.expense.service;

import com.tafhdev.split_bill_app.audit.domain.AuditAction;
import com.tafhdev.split_bill_app.audit.domain.AuditEntityType;
import com.tafhdev.split_bill_app.audit.domain.AuditLog;
import com.tafhdev.split_bill_app.audit.repository.AuditLogRepository;
import com.tafhdev.split_bill_app.expense.controller.dto.request.CreateExpenseRequest;
import com.tafhdev.split_bill_app.expense.domain.*;
import com.tafhdev.split_bill_app.expense.domain.calculator.SplitCalculator;
import com.tafhdev.split_bill_app.expense.repository.ExpenseRepository;
import com.tafhdev.split_bill_app.expense.service.dto.CreateExpenseResult;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BillGroupRepository billGroupRepository;
    private final AuditLogRepository auditLogRepository;
    private final SplitCalculatorResolver splitCalculatorResolver;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            BillGroupRepository billGroupRepository,
            AuditLogRepository auditLogRepository,
            SplitCalculatorResolver splitCalculatorResolver,
            IdGenerator idGenerator,
            Clock clock
    ) {
        this.expenseRepository = expenseRepository;
        this.billGroupRepository = billGroupRepository;
        this.auditLogRepository = auditLogRepository;
        this.splitCalculatorResolver = splitCalculatorResolver;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Transactional
    public CreateExpenseResult createExpense(
            UUID groupId,
            CreateExpenseRequest request
    ) {
        Money amount = Money.of(request.amount());

        BillGroup billGroup = billGroupRepository.findById(groupId)
                .orElseThrow(() -> new DomainException("bill group not found"));

        Participant payer = billGroup.requireParticipant(request.paidBy());

        request.split().participants()
                .forEach(participant ->
                        billGroup.requireParticipant(
                                participant.participantId()
                        ));

        List<SplitParticipant> participants =
                request.split().participants()
                        .stream()
                        .map(participant -> new SplitParticipant(
                                participant.participantId(),
                                participant.amount() != null ? Money.of(participant.amount()) : null,
                                participant.percentage()
                        ))
                        .toList();

        SplitCalculator calculator =
                splitCalculatorResolver.resolve(
                        request.split().type()
                );

        List<ExpenseSplit> splits =
                calculator.calculate(
                        amount,
                        participants
                );

        Expense expense = Expense.createNew(
                idGenerator.generate(),
                groupId,
                payer.getId(),
                amount,
                request.category(),
                request.split().type(),
                splits,
                Instant.now(clock)
        );

        Expense savedExpense = expenseRepository.save(expense);

        AuditLog auditLog = AuditLog.createNew(
                idGenerator.generate(),
                groupId,
                AuditAction.CREATED,
                AuditEntityType.EXPENSE,
                savedExpense.getId(),
                Instant.now(clock)
        );

        auditLogRepository.save(auditLog);

        return new CreateExpenseResult(
                savedExpense,
                billGroup.getParticipants()
        );
    }
}
