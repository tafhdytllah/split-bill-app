package com.tafhdev.split_bill_app.expense.persistence.mapper;

import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.expense.persistence.entity.ExpenseEntity;
import com.tafhdev.split_bill_app.expense.persistence.entity.ExpenseSplitEntity;
import com.tafhdev.split_bill_app.group.persistence.entity.ParticipantEntity;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class ExpenseSplitMapper {

    public ExpenseSplitEntity toEntity(
            ExpenseSplit domain,
            ExpenseEntity expenseEntity,
            ParticipantEntity participantEntity
    ) {
        return new ExpenseSplitEntity(
                domain.getId(),
                expenseEntity,
                participantEntity,
                domain.getAmount().value()
        );
    }

    public ExpenseSplit toDomain(ExpenseSplitEntity entity) {
        return ExpenseSplit.reconstitute(
                entity.getId(),
                entity.getParticipant().getId(),
                Money.of(entity.getAmount())
        );
    }
}
