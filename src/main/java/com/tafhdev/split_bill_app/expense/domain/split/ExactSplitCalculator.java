package com.tafhdev.split_bill_app.expense.domain.split;

import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.domain.exception.Guard;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExactSplitCalculator {

    private final IdGenerator idGenerator;

    public ExactSplitCalculator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public List<ExpenseSplit> calculate(
            Money expenseAmount,
            List<ExactSplit> exactSplits
    ) {
        validateInput(
                expenseAmount,
                exactSplits
        );

        Money totalSplitAmount = Money.of(
                BigDecimal.ZERO
        );

        for (ExactSplit exactSplit : exactSplits) {
            totalSplitAmount = totalSplitAmount.add(
                    exactSplit.amount()
            );
        }

        validateTotalAmount(
                expenseAmount,
                totalSplitAmount
        );

        List<ExpenseSplit> splits = new ArrayList<>();

        for (ExactSplit exactSplit : exactSplits) {
            splits.add(
                    ExpenseSplit.createNew(
                            idGenerator.generate(),
                            exactSplit.participantId(),
                            exactSplit.amount()
                    )
            );

            totalSplitAmount = totalSplitAmount.add(
                    exactSplit.amount()
            );
        }

        return splits;
    }

    private void validateInput(
            Money expenseAmount,
            List<ExactSplit> exactSplits
    ) {
        Guard.requireNotNull(expenseAmount, "expense amount");
        Guard.requireNotNull(exactSplits, "exact splits");

        if (exactSplits.size() < 2) {
            throw new DomainException(
                    "exact split requires at least two participants"
            );
        }

        for (ExactSplit exactSplit : exactSplits) {
            Guard.requireNotNull(exactSplit, "exact split");
            Guard.requireNotNull(exactSplit.participantId(), "participant id");
            Guard.requireNotNull(exactSplit.amount(), "amount");

            if (exactSplit.amount().isNegative()) {
                throw new DomainException(
                        "split amount must not be negative"
                );
            }
        }

        validateUniqueParticipants(
                exactSplits
        );
    }

    private void validateUniqueParticipants(
            List<ExactSplit> exactSplits
    ) {
        long uniqueParticipantCount = exactSplits.stream()
                .map(ExactSplit::participantId)
                .distinct()
                .count();

        if (uniqueParticipantCount != exactSplits.size()) {
            throw new DomainException(
                    "split participants must be unique"
            );
        }
    }

    private void validateTotalAmount(
            Money expenseAmount,
            Money totalSplitAmount
    ) {
        if (!expenseAmount.equals(totalSplitAmount)) {
            throw new DomainException(
                    "total split amount must equal expense amount"
            );
        }
    }
}