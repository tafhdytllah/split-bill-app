package com.tafhdev.split_bill_app.expense.controller;

import com.tafhdev.split_bill_app.expense.controller.dto.request.CreateExpenseRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.response.ExpenseResponse;
import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.split.ExactSplit;
import com.tafhdev.split_bill_app.expense.domain.split.PercentageSplit;
import com.tafhdev.split_bill_app.expense.service.ExpenseService;
import com.tafhdev.split_bill_app.shared.domain.Money;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateExpenseRequest request
    ) {
        List<ExactSplit> exactSplits = null;

        if (request.exactSplits() != null) {
            exactSplits = request.exactSplits().stream()
                    .map(split -> new ExactSplit(
                            split.participantId(),
                            Money.of(split.amount())
                    ))
                    .toList();
        }

        List<PercentageSplit> percentageSplits = null;

        if (request.percentageSplits() != null) {
            percentageSplits = request.percentageSplits().stream()
                    .map(split -> new PercentageSplit(
                            split.participantId(),
                            split.percentage()
                    ))
                    .toList();
        }

        Expense expense = expenseService.createExpense(
                groupId,
                request.paidBy(),
                Money.of(request.amount()),
                request.category(),
                request.splitType(),
                request.participants(),
                exactSplits,
                percentageSplits
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ExpenseResponse.from(expense));
    }
}
