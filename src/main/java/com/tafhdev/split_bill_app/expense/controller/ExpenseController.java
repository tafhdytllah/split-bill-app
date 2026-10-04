package com.tafhdev.split_bill_app.expense.controller;

import com.tafhdev.split_bill_app.expense.controller.dto.CreateExpenseRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.ExpenseResponse;
import com.tafhdev.split_bill_app.expense.controller.mapper.ExpenseApiMapper;
import com.tafhdev.split_bill_app.expense.service.ExpenseService;
import com.tafhdev.split_bill_app.expense.service.dto.CreateExpenseCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseApiMapper expenseApiMapper;

    public ExpenseController(
            ExpenseService expenseService,
            ExpenseApiMapper expenseApiMapper
    ) {
        this.expenseService = expenseService;
        this.expenseApiMapper = expenseApiMapper;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable UUID groupId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateExpenseRequest request
    ) {

        CreateExpenseCommand command = expenseApiMapper.toCommand(
                idempotencyKey,
                groupId,
                request
        );

        ExpenseResponse response = expenseService.createExpense(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
