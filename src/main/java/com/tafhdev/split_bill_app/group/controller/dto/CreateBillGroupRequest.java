package com.tafhdev.split_bill_app.group.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateBillGroupRequest(

        @NotBlank
        String name,

        @NotEmpty
        List<@NotBlank String> participants
) {
}
