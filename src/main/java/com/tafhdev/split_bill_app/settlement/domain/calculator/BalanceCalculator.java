package com.tafhdev.split_bill_app.settlement.domain.calculator;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.payment.domain.Payment;
import com.tafhdev.split_bill_app.settlement.domain.Balance;
import com.tafhdev.split_bill_app.shared.domain.Money;
import com.tafhdev.split_bill_app.shared.domain.exception.Guard;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BalanceCalculator {

    public List<Balance> calculate(
            List<Participant> participants,
            List<Expense> expenses,
            List<Payment> payments
    ) {
        Map<UUID, Balance> balances = initializeBalances(participants);

        applyExpenses(balances, expenses);
        applyPayments(balances, payments);

        return balances.values().stream().toList();
    }

    private Map<UUID, Balance> initializeBalances(
            List<Participant> participants
    ) {
        return participants.stream()
                .collect(Collectors.toMap(
                        Participant::getId,
                        participant -> Balance.of(
                                participant.getId(),
                                Money.zero()
                        )
                ));
    }

    private void applyExpenses(
            Map<UUID, Balance> balances,
            List<Expense> expenses
    ) {
        for (Expense expense : expenses) {
            UUID payer = expense.getPaidBy();

            Balance payerBalance = balances.get(payer);
            Guard.requireNotNull(payerBalance, "payer balance");

            balances.put(
                    payer,
                    payerBalance.add(expense.getAmount())
            );

            for (ExpenseSplit split : expense.getSplits()) {
                UUID participantId = split.getParticipantId();

                Balance participantBalance = balances.get(participantId);
                Guard.requireNotNull(participantBalance, "participant balance");

                balances.put(
                        participantId,
                        participantBalance.subtract(split.getAmount())
                );
            }
        }
    }

    private void applyPayments(
            Map<UUID, Balance> balances,
            List<Payment> payments
    ) {
        for (Payment payment : payments) {
            UUID fromParticipantId = payment.getFromParticipantId();
            UUID toParticipantId = payment.getToParticipantId();
            Balance fromBalance = balances.get(fromParticipantId);
            Balance toBalance = balances.get(toParticipantId);

            Guard.requireNotNull(fromBalance, "from participant balance");
            Guard.requireNotNull(toBalance, "to participant balance");

            balances.put(
                    fromParticipantId,
                    fromBalance.add(payment.getAmount())
            );

            balances.put(
                    toParticipantId,
                    toBalance.subtract(payment.getAmount())
            );
        }
    }
}
