package com.tafhdev.split_bill_app.expense.service;

import com.tafhdev.split_bill_app.audit.domain.AuditAction;
import com.tafhdev.split_bill_app.audit.domain.AuditEntityType;
import com.tafhdev.split_bill_app.audit.domain.AuditLog;
import com.tafhdev.split_bill_app.audit.repository.AuditLogRepository;
import com.tafhdev.split_bill_app.expense.controller.dto.ExpenseResponse;
import com.tafhdev.split_bill_app.expense.controller.mapper.ExpenseApiMapper;
import com.tafhdev.split_bill_app.expense.domain.*;
import com.tafhdev.split_bill_app.expense.domain.calculator.SplitCalculator;
import com.tafhdev.split_bill_app.expense.repository.ExpenseRepository;
import com.tafhdev.split_bill_app.expense.service.dto.CreateExpenseCommand;
import com.tafhdev.split_bill_app.expense.service.dto.ExpenseResult;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.shared.application.exception.ConflictException;
import com.tafhdev.split_bill_app.shared.application.exception.ResourceNotFoundException;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyHashGenerator;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyRequestBuilder;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyService;
import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BillGroupRepository billGroupRepository;
    private final AuditLogRepository auditLogRepository;
    private final IdempotencyService idempotencyService;
    private final IdempotencyHashGenerator idempotencyHashGenerator;
    private final SplitCalculatorResolver splitCalculatorResolver;
    private final IdGenerator idGenerator;
    private final Clock clock;
    private final ExpenseApiMapper expenseApiMapper;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            BillGroupRepository billGroupRepository,
            AuditLogRepository auditLogRepository,
            IdempotencyService idempotencyService,
            IdempotencyHashGenerator idempotencyHashGenerator,
            SplitCalculatorResolver splitCalculatorResolver,
            IdGenerator idGenerator,
            Clock clock,
            ExpenseApiMapper expenseApiMapper
    ) {
        this.expenseRepository = expenseRepository;
        this.billGroupRepository = billGroupRepository;
        this.auditLogRepository = auditLogRepository;
        this.idempotencyService = idempotencyService;
        this.idempotencyHashGenerator = idempotencyHashGenerator;
        this.splitCalculatorResolver = splitCalculatorResolver;
        this.idGenerator = idGenerator;
        this.clock = clock;
        this.expenseApiMapper = expenseApiMapper;
    }

    @Transactional
    public ExpenseResult createExpense(CreateExpenseCommand command) {

        StringBuilder request = new StringBuilder(
                IdempotencyRequestBuilder.build(
                        command.groupId(),
                        command.paidByParticipantId(),
                        command.amount(),
                        command.category(),
                        command.split().type()
                )
        );

        command.split().participants()
                .forEach(participant -> {
                    request.append(
                            IdempotencyRequestBuilder.build(
                                    participant.participantId(),
                                    participant.amount(),
                                    participant.percentage()
                            )
                    );
                });

        String requestHash = idempotencyHashGenerator.generate(request.toString());

        Optional<Idempotency> existing =
                idempotencyService.find(
                        IdempotencyScope.EXPENSE,
                        command.idempotencyKey()
                );

        if (existing.isPresent()) {

            Idempotency idempotency = existing.get();

            boolean sameHash = MessageDigest.isEqual(
                    idempotency.getRequestHash().getBytes(StandardCharsets.UTF_8),
                    requestHash.getBytes(StandardCharsets.UTF_8)
            );

            if (!sameHash) {
                throw new ConflictException(
                        "idempotency key reused with different request"
                );
            }

            ExpenseResponse response = idempotencyService.getResponse(
                    idempotency,
                    ExpenseResponse.class
            );

            return new ExpenseResult(
                    response,
                    true
            );
        }

        BillGroup billGroup = billGroupRepository.findById(command.groupId())
                .orElseThrow(() -> new ResourceNotFoundException("bill group not found"));

        Participant payer = billGroup.requireParticipant(command.paidByParticipantId());

        command.split().participants()
                .forEach(participant ->
                        billGroup.requireParticipant(
                                participant.participantId()
                        ));

        List<SplitParticipant> participants =
                command.split().participants()
                        .stream()
                        .map(participant -> new SplitParticipant(
                                participant.participantId(),
                                participant.amount(),
                                participant.percentage()
                        ))
                        .toList();

        SplitCalculator calculator =
                splitCalculatorResolver.resolve(
                        command.split().type()
                );

        List<ExpenseSplit> splits =
                calculator.calculate(
                        command.amount(),
                        participants
                );

        Idempotency idempotency =
                idempotencyService.create(
                        IdempotencyScope.EXPENSE,
                        command.idempotencyKey(),
                        requestHash
                );

        Expense expense = Expense.createNew(
                idGenerator.generate(),
                command.groupId(),
                payer.getId(),
                command.amount(),
                command.category(),
                command.split().type(),
                splits,
                Instant.now(clock)
        );

        Expense savedExpense = expenseRepository.save(expense);

        AuditLog auditLog = AuditLog.createNew(
                idGenerator.generate(),
                command.groupId(),
                AuditAction.CREATED,
                AuditEntityType.EXPENSE,
                savedExpense.getId(),
                Instant.now(clock)
        );

        auditLogRepository.save(auditLog);

        ExpenseResponse response = expenseApiMapper.toResponse(
                savedExpense,
                billGroup.getParticipants()
        );

        idempotencyService.complete(
                idempotency,
                HttpStatus.CREATED.value(),
                response
        );

        return new ExpenseResult(
                response,
                false
        );
    }
}
