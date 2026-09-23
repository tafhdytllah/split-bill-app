package com.tafhdev.split_bill_app.expense.controller.dto.request;

import com.tafhdev.split_bill_app.expense.domain.SplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SplitRequest(

        @NotNull
        SplitType type,

        @NotEmpty
        List<@Valid SplitParticipantRequest> participants
) {
}
