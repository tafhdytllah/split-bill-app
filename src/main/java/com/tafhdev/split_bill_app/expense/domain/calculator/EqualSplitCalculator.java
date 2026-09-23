package com.tafhdev.split_bill_app.expense.domain.calculator;

import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.expense.domain.SplitParticipant;
import com.tafhdev.split_bill_app.expense.domain.SplitType;
import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.domain.exception.Guard;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class EqualSplitCalculator implements SplitCalculator {

    private final IdGenerator idGenerator;

    public EqualSplitCalculator(
            IdGenerator idGenerator
    ) {
        this.idGenerator = idGenerator;
    }

    @Override
    public SplitType supports() {
        return SplitType.EQUAL;
    }

    @Override
    public List<ExpenseSplit> calculate(
            Money totalAmount,
            List<SplitParticipant> participants
    ) {
        validateInput(
                totalAmount,
                participants
        );

        Money baseAmount = totalAmount.divide(
                participants.size()
        );

        List<ExpenseSplit> splits = new ArrayList<>();

        Money allocatedAmount = Money.of(BigDecimal.ZERO);

        for (int i = 0; i < participants.size(); i++) {
            SplitParticipant participant = participants.get(i);

            Money splitAmount;

            if (i == participants.size() - 1) {
                splitAmount = totalAmount.subtract(
                        allocatedAmount
                );
            } else {
                splitAmount = baseAmount;
            }

            splits.add(
                    ExpenseSplit.createNew(
                            idGenerator.generate(),
                            participant.participantId(),
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
            Money totalAmount,
            List<SplitParticipant> participants
    ) {
        Guard.requireNotNull(totalAmount, "expense amount");
        Guard.requireNotNull(participants, "participants");

        if (participants.size() < 2) {
            throw new DomainException(
                    "equal split requires at least two participants"
            );
        }

        Set<UUID> participantIds = new HashSet<>();

        for (SplitParticipant participant : participants) {
            Guard.requireNotNull(participant, "split participant");
            Guard.requireNotNull(
                    participant.participantId(),
                    "participant id"
            );

            if (participant.amount() != null
                    || participant.percentage() != null) {
                throw new DomainException(
                        "equal split must not have amount or percentage"
                );
            }

            if (!participantIds.add(participant.participantId())) {
                throw new DomainException(
                        "split participants must be unique"
                );
            }
        }
    }
}
