package com.tafhdev.split_bill_app.expense.service.dto;

import com.tafhdev.split_bill_app.expense.domain.ExpenseSplitType;

import java.util.List;

public record SplitCommand(

        ExpenseSplitType type,

        List<SplitParticipantCommand> participants
) {
}
