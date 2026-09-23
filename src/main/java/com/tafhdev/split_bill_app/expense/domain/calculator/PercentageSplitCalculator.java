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
import java.math.RoundingMode;
import java.util.*;

@Component
public class PercentageSplitCalculator implements SplitCalculator {

    private final IdGenerator idGenerator;

    public PercentageSplitCalculator(
            IdGenerator idGenerator
    ) {
        this.idGenerator = idGenerator;
    }

    @Override
    public SplitType supports() {
        return SplitType.PERCENTAGE;
    }

    @Override
    public List<ExpenseSplit> calculate(
            Money totalAmount,
            List<SplitParticipant> participants
    ) {
        validateInput(totalAmount, participants);

        List<ExpenseSplit> splits = new ArrayList<>();

        Money allocatedAmount = Money.of(BigDecimal.ZERO);

        for (int i = 0; i < participants.size(); i++) {
            SplitParticipant participant = participants.get(i);

            Money splitAmount;

            if (i == participants.size() - 1) {
                splitAmount = totalAmount.subtract(allocatedAmount);
            } else {
                splitAmount = totalAmount.multiply(
                        participant.percentage()
                                .divide(
                                        BigDecimal.valueOf(100),
                                        10,
                                        RoundingMode.HALF_UP
                                )
                );
            }

            splits.add(
                    ExpenseSplit.createNew(
                            idGenerator.generate(),
                            participant.participantId(),
                            splitAmount
                    )
            );

            allocatedAmount = allocatedAmount.add(splitAmount);
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
                    "percentage split requires at least two participants"
            );
        }

        Set<UUID> participantIds = new HashSet<>();
        BigDecimal totalPercentage = BigDecimal.ZERO;

        for (SplitParticipant participant : participants) {
            Guard.requireNotNull(participant, "split participant");
            Guard.requireNotNull(participant.participantId(), "participant id");
            Guard.requireNotNull(participant.percentage(), "split percentage");

            if (participant.percentage().scale() > 2) {
                throw new DomainException(
                        "percentage must have at most 2 decimal places"
                );
            }

            if (participant.percentage().compareTo(BigDecimal.ZERO) < 0
                    || participant.percentage().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new DomainException(
                        "percentage must be between 0 and 100"
                );
            }

            if (participant.amount() != null) {
                throw new DomainException(
                        "percentage split must not have amount"
                );
            }

            if (!participantIds.add(participant.participantId())) {
                throw new DomainException(
                        "split participants must be unique"
                );
            }

            totalPercentage = totalPercentage.add(
                    participant.percentage()
            );
        }

        if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new DomainException(
                    "percentage split must equal 100%"
            );
        }
    }
}
