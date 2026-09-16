package com.tafhdev.split_bill_app.expense.persistence.mapper;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.expense.domain.SplitType;
import com.tafhdev.split_bill_app.expense.persistence.entity.ExpenseSplitEntity;
import com.tafhdev.split_bill_app.expense.persistence.entity.ExpenseEntity;
import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import com.tafhdev.split_bill_app.group.persistence.entity.ParticipantEntity;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExpenseMapper {

    private final ExpenseSplitMapper expenseSplitMapper;

    public ExpenseMapper(ExpenseSplitMapper expenseSplitMapper) {
        this.expenseSplitMapper = expenseSplitMapper;
    }

    public ExpenseEntity toEntity(
            Expense domain,
            BillGroupEntity groupEntity,
            ParticipantEntity paidByEntity
    ) {
        return new ExpenseEntity(
                domain.getId(),
                groupEntity,
                paidByEntity,
                domain.getAmount().value(),
                domain.getCategory().name(),
                domain.getSplitType().name(),
                domain.getCreatedAt()
        );
    }

    public Expense toDomain(ExpenseEntity entity) {
        List<ExpenseSplit> splits =
                entity.getSplits().stream()
                        .map(expenseSplitMapper::toDomain)
                        .toList();

        return Expense.reconstitute(
                entity.getId(),
                entity.getGroup().getId(),
                entity.getPaidBy().getId(),
                Money.of(entity.getAmount()),
                ExpenseCategory.valueOf(entity.getCategory()),
                SplitType.valueOf(entity.getSplitType()),
                splits,
                entity.getCreatedAt()
        );
    }
}
