package com.tafhdev.split_bill_app.expense.service;

import com.tafhdev.split_bill_app.expense.domain.ExpenseSplitType;
import com.tafhdev.split_bill_app.expense.domain.calculator.SplitCalculator;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SplitCalculatorResolver {

    private final List<SplitCalculator> calculators;

    public SplitCalculatorResolver(
            List<SplitCalculator> calculators
    ) {
        this.calculators = calculators;
    }

    public SplitCalculator resolve(ExpenseSplitType expenseSplitType) {
        return calculators.stream()
                .filter(calculator ->
                        calculator.supports() == expenseSplitType
                )
                .findFirst()
                .orElseThrow(() ->
                        new DomainException(
                                "unsupported split type: " + expenseSplitType
                        )
                );
    }
}
