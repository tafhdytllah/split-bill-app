package com.tafhdev.split_bill_app.expense.controller;

import com.tafhdev.split_bill_app.expense.controller.dto.request.CreateExpenseRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.response.ExpenseResponse;
import com.tafhdev.split_bill_app.expense.controller.mapper.ExpenseResponseMapper;
import com.tafhdev.split_bill_app.expense.service.ExpenseService;
import com.tafhdev.split_bill_app.expense.service.dto.CreateExpenseResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseResponseMapper expenseResponseMapper;

    public ExpenseController(
            ExpenseService expenseService,
            ExpenseResponseMapper expenseResponseMapper
    ) {
        this.expenseService = expenseService;
        this.expenseResponseMapper = expenseResponseMapper;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateExpenseRequest request
    ) {
        CreateExpenseResult result = expenseService.createExpense(
                groupId,
                request
        );

        ExpenseResponse expenseResponse = expenseResponseMapper.toResponse(
                result.expense(),
                result.participants()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(expenseResponse);
    }
}
