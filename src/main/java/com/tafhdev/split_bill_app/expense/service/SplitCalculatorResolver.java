package com.tafhdev.split_bill_app.expense.service;

import com.tafhdev.split_bill_app.expense.domain.SplitType;
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

    public SplitCalculator resolve(SplitType splitType) {
        return calculators.stream()
                .filter(calculator ->
                        calculator.supports() == splitType
                )
                .findFirst()
                .orElseThrow(() ->
                        new DomainException(
                                "unsupported split type: " + splitType
                        )
                );
    }
}
