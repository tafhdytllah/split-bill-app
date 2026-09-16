package com.tafhdev.split_bill_app.expense.domain.split;

import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.domain.exception.Guard;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Component
public class EqualSplitCalculator {

    private final IdGenerator idGenerator;

    public EqualSplitCalculator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public List<ExpenseSplit> calculate(
            Money expenseAmount,
            List<UUID> participantIds
    ) {
        validateInput(
                expenseAmount,
                participantIds
        );

        Money baseAmount = expenseAmount.divide(
                participantIds.size()
        );

        List<ExpenseSplit> splits = new ArrayList<>();

        Money allocatedAmount = Money.of(BigDecimal.ZERO);

        for (int i = 0; i < participantIds.size(); i++) {
            UUID participantId = participantIds.get(i);

            Money splitAmount;

            if (i == participantIds.size() - 1) {
                splitAmount = expenseAmount.subtract(
                        allocatedAmount
                );
            } else {
                splitAmount = baseAmount;
            }

            splits.add(
                    ExpenseSplit.createNew(
                            idGenerator.generate(),
                            participantId,
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
            List<UUID> participantIds
    ) {
        Guard.requireNotNull(expenseAmount, "expense amount");
        Guard.requireNotNull(participantIds, "participants");

        if (participantIds.size() < 2) {
            throw new DomainException(
                    "equal split requires at least two participants"
            );
        }

        for (UUID participantId : participantIds) {
            Guard.requireNotNull(participantId, "participant id");
        }

        if (new HashSet<>(participantIds).size() != participantIds.size()) {
            throw new DomainException(
                    "split participants must be unique"
            );
        }
    }

}
