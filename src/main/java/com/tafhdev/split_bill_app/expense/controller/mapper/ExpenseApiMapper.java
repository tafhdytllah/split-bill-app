package com.tafhdev.split_bill_app.expense.controller.mapper;

import com.tafhdev.split_bill_app.expense.controller.dto.CreateExpenseRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.SplitParticipantRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.SplitRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.ExpenseResponse;
import com.tafhdev.split_bill_app.expense.controller.dto.ExpenseSplitResponse;
import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.service.dto.CreateExpenseCommand;
import com.tafhdev.split_bill_app.expense.service.dto.SplitCommand;
import com.tafhdev.split_bill_app.expense.service.dto.SplitParticipantCommand;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ExpenseApiMapper {

    public CreateExpenseCommand toCommand(
            String idempotencyKey,
            UUID groupId,
            CreateExpenseRequest request
    ) {
        return new CreateExpenseCommand(
                idempotencyKey,
                groupId,
                request.paidBy(),
                Money.of(request.amount()),
                request.category(),
                toSplitCommand(request.split())
        );
    }

    public ExpenseResponse toResponse(
            Expense expense,
            List<Participant> participants
    ) {
        Map<UUID, String> participantNames =
                participants.stream()
                        .collect(Collectors.toMap(
                                Participant::getId,
                                Participant::getName
                        ));

        List<ExpenseSplitResponse> splits =
                expense.getSplits().stream()
                        .map(expenseSplit ->
                                ExpenseSplitResponse.from(
                                        expenseSplit,
                                        participantNames.get(
                                                expenseSplit.getParticipantId()
                                        )
                                )
                        )
                        .toList();

        return new ExpenseResponse(
                expense.getId(),
                expense.getGroupId(),
                expense.getPaidBy(),
                expense.getAmount().value(),
                expense.getCategory(),
                expense.getSplitType(),
                splits,
                expense.getCreatedAt()
        );
    }

    private SplitCommand toSplitCommand(
            SplitRequest request
    ) {
        return new SplitCommand(
                request.type(),
                request.participants().stream()
                        .map(this::toSplitParticipantCommand)
                        .toList()
        );
    }

    private SplitParticipantCommand toSplitParticipantCommand(
            SplitParticipantRequest request
    ) {
        return new SplitParticipantCommand(
                request.participantId(),
                request.amount() != null ? Money.of(request.amount()) : null,
                request.percentage()
        );
    }
}
