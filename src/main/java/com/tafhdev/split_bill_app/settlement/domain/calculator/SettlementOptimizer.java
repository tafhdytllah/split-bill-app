package com.tafhdev.split_bill_app.settlement.domain.calculator;

import com.tafhdev.split_bill_app.settlement.domain.Balance;
import com.tafhdev.split_bill_app.settlement.domain.Settlement;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SettlementOptimizer {

    public List<Settlement> optimize(List<Balance> balances) {
        List<Balance> debtors = new ArrayList<>();
        List<Balance> creditors = new ArrayList<>();

        separateBalances(
                balances,
                debtors,
                creditors
        );

        List<Settlement> bestSettlement = new ArrayList<>();

        findBestSettlement(
                debtors,
                creditors,
                0,
                new ArrayList<>(),
                bestSettlement
        );

        return bestSettlement;
    }

    private void separateBalances(
            List<Balance> balances,
            List<Balance> debtors,
            List<Balance> creditors
    ) {
        for (Balance balance : balances) {
            if (balance.isDebtor()) {
                debtors.add(balance.negate());
            }

            if (balance.isCreditor()) {
                creditors.add(balance);
            }
        }
    }

    private void findBestSettlement(
            List<Balance> debtors,
            List<Balance> creditors,
            int debtorIndex,
            List<Settlement> currentSettlement,
            List<Settlement> bestSettlement
    ) {
        if (debtorIndex == debtors.size()) {
            updateBestSettlement(currentSettlement, bestSettlement);
            return;
        }

        if (isPruned(currentSettlement, bestSettlement)) {
            return;
        }

        Balance debtor = debtors.get(debtorIndex);

        if (debtor.isSettled()) {
            findBestSettlement(
                    debtors,
                    creditors,
                    debtorIndex + 1,
                    currentSettlement,
                    bestSettlement
            );
            return;
        }

        for (int creditorIndex = 0; creditorIndex < creditors.size(); creditorIndex++) {

            Balance creditor = creditors.get(creditorIndex);

            if (creditor.isSettled()) {
                continue;
            }

            createSettlementBranch(
                    debtors,
                    creditors,
                    debtorIndex,
                    creditorIndex,
                    currentSettlement,
                    bestSettlement
            );
        }
    }

    private void createSettlementBranch(
            List<Balance> debtors,
            List<Balance> creditors,
            int debtorIndex,
            int creditorIndex,
            List<Settlement> currentSettlement,
            List<Settlement> bestSettlement
    ) {
        Balance debtor = debtors.get(debtorIndex);
        Balance creditor = creditors.get(creditorIndex);

        Money settlementAmount = debtor.getAmount().min(
                creditor.getAmount()
        );

        Settlement settlement = Settlement.createNew(
                debtor.getParticipantId(),
                creditor.getParticipantId(),
                settlementAmount
        );

        currentSettlement.add(settlement);

        List<Balance> nextDebtors = new ArrayList<>(debtors);
        List<Balance> nextCreditors = new ArrayList<>(creditors);

        nextDebtors.set(
                debtorIndex,
                debtor.subtract(settlementAmount)
        );

        nextCreditors.set(
                creditorIndex,
                creditor.subtract(settlementAmount)
        );

        findBestSettlement(
                nextDebtors,
                nextCreditors,
                debtorIndex,
                currentSettlement,
                bestSettlement
        );

        currentSettlement.removeLast();
    }

    private boolean isPruned(
            List<Settlement> currentSettlement,
            List<Settlement> bestSettlement
    ) {
        return !bestSettlement.isEmpty()
                && currentSettlement.size() >= bestSettlement.size();
    }

    private void updateBestSettlement(
            List<Settlement> currentSettlement,
            List<Settlement> bestSettlement
    ) {
        if (bestSettlement.isEmpty()
                || currentSettlement.size() < bestSettlement.size()) {

            bestSettlement.clear();
            bestSettlement.addAll(currentSettlement);
        }
    }
}
