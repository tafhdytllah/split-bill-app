package com.tafhdev.split_bill_app.expense.controller.dto.response;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.SplitType;
import com.tafhdev.split_bill_app.group.domain.Participant;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record ExpenseResponse(
        UUID id,
        UUID groupId,
        UUID paidBy,

        @JsonSerialize(using = ToStringSerializer.class)
        BigDecimal amount,

        ExpenseCategory category,
        SplitType splitType,
        List<ExpenseSplitResponse> splits,
        Instant createdAt
) {

    public static ExpenseResponse from(
            Expense expense,
            List<Participant> participants
    ) {
        Map<UUID, String> participantNames = participants.stream()
                .collect(Collectors.toMap(
                        Participant::getId,
                        Participant::getName
                ));

        List<ExpenseSplitResponse> splits = expense.getSplits().stream()
                        .map(expenseSplit -> ExpenseSplitResponse.from(
                                expenseSplit,
                                participantNames.get(expenseSplit.getParticipantId())
                        ))
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
}
