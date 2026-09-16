package com.tafhdev.split_bill_app.expense.domain.split;

import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.domain.exception.Guard;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class PercentageSplitCalculator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final IdGenerator idGenerator;

    public PercentageSplitCalculator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public List<ExpenseSplit> calculate(
            Money expenseAmount,
            List<PercentageSplit> percentageSplits
    ) {
        validateInput(
                expenseAmount,
                percentageSplits
        );

        List<ExpenseSplit> splits = new ArrayList<>();

        Money allocatedAmount = Money.of(ZERO);

        for (int i = 0; i < percentageSplits.size(); i++) {
            PercentageSplit percentageSplit = percentageSplits.get(i);

            Money splitAmount;

            if (i == percentageSplits.size() - 1) {
                splitAmount = expenseAmount.subtract(
                        allocatedAmount
                );
            } else {
                BigDecimal multiplier = percentageSplit.percentage()
                        .divide(
                                ONE_HUNDRED,
                                4,
                                RoundingMode.HALF_UP
                        );

                splitAmount = expenseAmount.multiply(
                        multiplier
                );
            }

            splits.add(
                    ExpenseSplit.createNew(
                            idGenerator.generate(),
                            percentageSplit.participantId(),
                            splitAmount
                    )
            );

            allocatedAmount = allocatedAmount.add(
                    splitAmount
            );
        }

        return splits;
    }

    private void validateInput(
            Money expenseAmount,
            List<PercentageSplit> percentageSplits
    ) {
        Guard.requireNotNull(expenseAmount, "expense amount");
        Guard.requireNotNull(percentageSplits, "percentage splits");

        if (percentageSplits.size() < 2) {
            throw new DomainException(
                    "percentage split requires at least two participants"
            );
        }

        for (PercentageSplit percentageSplit : percentageSplits) {
            Guard.requireNotNull(percentageSplit, "percentage split");
            Guard.requireNotNull(percentageSplit.participantId(), "participant id");
            Guard.requireNotNull(percentageSplit.percentage(), "percentage");

            validatePercentage(
                    percentageSplit.percentage()
            );
        }

        validateUniqueParticipants(
                percentageSplits
        );

        validateTotalPercentage(
                percentageSplits
        );
    }

    private void validatePercentage(
            BigDecimal percentage
    ) {
        if (percentage.compareTo(ZERO) < 0 || percentage.compareTo(ONE_HUNDRED) > 0) {
            throw new DomainException(
                    "percentage must be between 0 and 100"
            );
        }

        if (percentage.scale() > 2) {
            throw new DomainException(
                    "percentage must have at most 2 decimal places"
            );
        }
    }

    private void validateUniqueParticipants(
            List<PercentageSplit> percentageSplits
    ) {
        long uniqueCount = percentageSplits.stream()
                .map(PercentageSplit::participantId)
                .distinct()
                .count();

        if (uniqueCount != percentageSplits.size()) {
            throw new DomainException(
                    "split participants must be unique"
            );
        }
    }

    private void validateTotalPercentage(
            List<PercentageSplit> percentageSplits
    ) {
        BigDecimal totalPercentage = percentageSplits.stream()
                .map(PercentageSplit::percentage)
                .reduce(
                        ZERO,
                        BigDecimal::add
                );

        if (totalPercentage.compareTo(ONE_HUNDRED) != 0) {
            throw new DomainException(
                    "total percentage must equal 100"
            );
        }
    }
}
