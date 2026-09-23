package com.tafhdev.split_bill_app.expense.domain.calculator;

import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.expense.domain.SplitParticipant;
import com.tafhdev.split_bill_app.expense.domain.SplitType;
import com.tafhdev.split_bill_app.shared.domain.Money;

import java.util.List;

public interface SplitCalculator {

    SplitType supports();

    List<ExpenseSplit> calculate(
            Money totalAmount,
            List<SplitParticipant> participants
    );
}
