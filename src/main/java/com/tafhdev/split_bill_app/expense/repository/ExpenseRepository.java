package com.tafhdev.split_bill_app.expense.repository;

import com.tafhdev.split_bill_app.expense.domain.Expense;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository {

    Expense save(Expense expense);

    Optional<Expense> findById(UUID id);
}
