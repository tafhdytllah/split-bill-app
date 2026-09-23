package com.tafhdev.split_bill_app.expense.controller.mapper;

import com.tafhdev.split_bill_app.expense.controller.dto.response.ExpenseResponse;
import com.tafhdev.split_bill_app.expense.controller.dto.response.ExpenseSplitResponse;
import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.group.domain.Participant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ExpenseResponseMapper {

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
}
