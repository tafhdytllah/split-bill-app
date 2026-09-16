package com.tafhdev.split_bill_app.expense.service;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.expense.domain.SplitType;
import com.tafhdev.split_bill_app.expense.domain.split.*;
import com.tafhdev.split_bill_app.expense.repository.ExpenseRepository;
import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExactSplitCalculator exactSplitCalculator;
    private final PercentageSplitCalculator percentageSplitCalculator;
    private final EqualSplitCalculator equalSplitCalculator;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            ExactSplitCalculator exactSplitCalculator,
            PercentageSplitCalculator percentageSplitCalculator,
            EqualSplitCalculator equalSplitCalculator,
            IdGenerator idGenerator,
            Clock clock
    ) {
        this.expenseRepository = expenseRepository;
        this.exactSplitCalculator = exactSplitCalculator;
        this.percentageSplitCalculator = percentageSplitCalculator;
        this.equalSplitCalculator = equalSplitCalculator;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Transactional
    public Expense createExpense(
            UUID groupId,
            UUID paidBy,
            Money amount,
            ExpenseCategory category,
            SplitType splitType,
            List<UUID> participantIds,
            List<ExactSplit> exactSplits,
            List<PercentageSplit> percentageSplits
    ) {
        UUID expenseId = idGenerator.generate();

        Instant createdAt = Instant.now(clock);

        List<ExpenseSplit> splits = calculateSplits(
                amount,
                splitType,
                participantIds,
                exactSplits,
                percentageSplits
        );

        Expense expense = Expense.createNew(
                expenseId,
                groupId,
                paidBy,
                amount,
                category,
                splitType,
                splits,
                createdAt
        );

        return expenseRepository.save(expense);
    }

    private List<ExpenseSplit> calculateSplits(
            Money amount,
            SplitType splitType,
            List<UUID> participantIds,
            List<ExactSplit> exactSplits,
            List<PercentageSplit> percentageSplits
    ) {
        return switch (splitType) {
            case EQUAL ->
                    equalSplitCalculator.calculate(
                            amount,
                            participantIds
                    );

            case EXACT ->
                    exactSplitCalculator.calculate(
                            amount,
                            exactSplits
                    );

            case PERCENTAGE ->
                    percentageSplitCalculator.calculate(
                            amount,
                            percentageSplits
                    );
        };
    }
}
