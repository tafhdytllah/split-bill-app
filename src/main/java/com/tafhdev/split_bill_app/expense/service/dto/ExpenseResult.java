package com.tafhdev.split_bill_app.expense.service.dto;

import com.tafhdev.split_bill_app.expense.controller.dto.ExpenseResponse;

public record ExpenseResult(

        ExpenseResponse response,
        boolean reply
) {
}
