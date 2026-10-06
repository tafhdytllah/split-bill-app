package com.tafhdev.split_bill_app.expense.config;

import com.tafhdev.split_bill_app.expense.domain.calculator.EqualSplitCalculator;
import com.tafhdev.split_bill_app.expense.domain.calculator.ExactSplitCalculator;
import com.tafhdev.split_bill_app.expense.domain.calculator.PercentageSplitCalculator;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExpenseCalculatorConfig {

    @Bean
    public EqualSplitCalculator equalSplitCalculator(
            IdGenerator idGenerator
    ) {
        return new EqualSplitCalculator(idGenerator);
    }

    @Bean
    public ExactSplitCalculator exactSplitCalculator(
            IdGenerator idGenerator
    ) {
        return new ExactSplitCalculator(idGenerator);
    }

    @Bean
    public PercentageSplitCalculator percentageSplitCalculator(
            IdGenerator idGenerator
    ) {
        return new PercentageSplitCalculator(idGenerator);
    }
}





























