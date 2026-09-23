package com.tafhdev.split_bill_app.expense.service.dto;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.group.domain.Participant;

import java.util.List;

public record CreateExpenseResult(

        Expense expense,
        List<Participant> participants
) {
}
