package com.tafhdev.split_bill_app.expense.controller;

import com.tafhdev.split_bill_app.expense.controller.dto.CreateExpenseRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.ExpenseResponse;
import com.tafhdev.split_bill_app.expense.controller.mapper.ExpenseApiMapper;
import com.tafhdev.split_bill_app.expense.service.ExpenseService;
import com.tafhdev.split_bill_app.expense.service.dto.CreateExpenseCommand;
import com.tafhdev.split_bill_app.expense.service.dto.ExpenseResult;
import com.tafhdev.split_bill_app.shared.infrastructure.web.response.ApiResponse;
import com.tafhdev.split_bill_app.shared.infrastructure.web.response.ResponseFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
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
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @PathVariable
            UUID groupId,

            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency-Key must not be blank")
            String idempotencyKey,

            @Valid
            @RequestBody
            CreateExpenseRequest request
    ) {

        CreateExpenseCommand command = expenseApiMapper.toCommand(
                idempotencyKey,
                groupId,
                request
        );

        ExpenseResult result = expenseService.createExpense(command);

        return result.replay()
                ? ResponseFactory.ok(result.response())
                : ResponseFactory.created(result.response());
    }
}
