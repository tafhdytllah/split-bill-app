package com.tafhdev.split_bill_app.expense.controller.dto;

import com.tafhdev.split_bill_app.expense.domain.ExpenseSplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SplitRequest(

        @NotNull
        ExpenseSplitType type,

        @NotEmpty
        List<@Valid SplitParticipantRequest> participants
) {
}
