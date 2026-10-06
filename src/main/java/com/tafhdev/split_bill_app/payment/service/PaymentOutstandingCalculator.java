package com.tafhdev.split_bill_app.payment.service;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.payment.domain.Payment;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PaymentOutstandingCalculator {

    public Money calculate(
            UUID fromParticipantId,
            UUID toParticipantId,
            List<Expense> expenses,
            List<Payment> payments
    ) {
        Money debt = calculateDebt(
                fromParticipantId,
                toParticipantId,
                expenses
        );

        Money paid = calculatePayment(
                fromParticipantId,
                toParticipantId,
                payments
        );

        return debt.subtract(paid);
    }

    private Money calculatePayment(
            UUID fromParticipantId,
            UUID toParticipantId,
            List<Payment> payments
    ) {
        Money total = Money.zero();

        for (Payment payment : payments) {
            if (payment.getFromParticipantId().equals(fromParticipantId)
                    && payment.getToParticipantId().equals(toParticipantId)
            ) {
                total = total.add(payment.getAmount());
            }
        }

        return total;
    }

    private Money calculateDebt(
            UUID fromParticipantId,
            UUID toParticipantId,
            List<Expense> expenses
    ) {
        Money total = Money.zero();

        for (Expense expense : expenses) {
            if (!expense.getPaidBy().equals(toParticipantId)) {
                continue;
            }

            for (ExpenseSplit split : expense.getSplits()) {
                if (split.getParticipantId().equals(fromParticipantId)) {
                    total = total.add(split.getAmount());
                }
            }
        }

        return total;
    }
}
