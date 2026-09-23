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
public class ExactSplitCalculator implements SplitCalculator {

    private final IdGenerator idGenerator;

    public ExactSplitCalculator(
            IdGenerator idGenerator
    ) {
        this.idGenerator = idGenerator;
    }

    @Override
    public SplitType supports() {
        return SplitType.EXACT;
    }

    @Override
    public List<ExpenseSplit> calculate(
            Money totalAmount,
            List<SplitParticipant> participants
    ) {
        validateInput(totalAmount, participants);

        List<ExpenseSplit> splits = new ArrayList<>();

        Money allocatedAmount = Money.of(BigDecimal.ZERO);

        for (SplitParticipant participant : participants) {
            Money splitAmount = participant.amount();

            splits.add(
                    ExpenseSplit.createNew(
                            idGenerator.generate(),
                            participant.participantId(),
                            splitAmount
                    )
            );

            allocatedAmount = allocatedAmount.add(splitAmount);
        }

        if (!allocatedAmount.equals(totalAmount)) {
            throw new DomainException(
                    "exact split amounts must equal expense amount"
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
                    "exact split requires at least two participants"
            );
        }

        Set<UUID> participantIds = new HashSet<>();

        for (SplitParticipant participant : participants) {
            Guard.requireNotNull(participant, "split participant");
            Guard.requireNotNull(
                    participant.participantId(),
                    "participant id"
            );
            Guard.requireNotNull(
                    participant.amount(),
                    "split amount"
            );

            if (participant.percentage() != null) {
                throw new DomainException(
                        "exact split must not have percentage"
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
