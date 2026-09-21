package com.tafhdev.split_bill_app.group.controller.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateBillGroupRequest(

        @NotNull
        String name,

        List<String> participants
) {
}
