package com.tafhdev.split_bill_app.settlement.domain.calculator;

import com.tafhdev.split_bill_app.settlement.domain.Balance;
import com.tafhdev.split_bill_app.settlement.domain.Settlement;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class SettlementOptimizer {

    private static final int EXACT_OPTIMIZATION_LIMIT = 20;

    public List<Settlement> optimize(List<Balance> balances) {
        List<Balance> debtors = new ArrayList<>();
        List<Balance> creditors = new ArrayList<>();

        separateBalances(
                balances,
                debtors,
                creditors
        );

        debtors.sort(
                Comparator.comparing(
                        Balance::getAmount,
                        Comparator.reverseOrder()
                )
        );

        creditors.sort(
                Comparator.comparing(
                        Balance::getAmount,
                        Comparator.reverseOrder()
                )
        );

        int participantCount =
                debtors.size() + creditors.size();

        if (participantCount > EXACT_OPTIMIZATION_LIMIT) {
            return optimizeGreedy(
                    debtors,
                    creditors
            );
        }

        List<Settlement> exactSettlements =
                extractExactSettlements(
                        debtors,
                        creditors
                );

        if (debtors.stream().allMatch(Balance::isSettled)
                && creditors.stream().allMatch(Balance::isSettled)) {
            return exactSettlements;
        }

        List<Settlement> bestSettlement =
                new ArrayList<>();

        findBestSettlement(
                debtors,
                creditors,
                0,
                exactSettlements,
                bestSettlement
        );

        return bestSettlement;
    }

    private List<Settlement> optimizeGreedy(
            List<Balance> debtors,
            List<Balance> creditors
    ) {
        List<Settlement> settlements =
                new ArrayList<>();

        int debtorIndex = 0;
        int creditorIndex = 0;

        while (debtorIndex < debtors.size()
                && creditorIndex < creditors.size()) {

            Balance debtor =
                    debtors.get(debtorIndex);

            Balance creditor =
                    creditors.get(creditorIndex);

            Money settlementAmount =
                    debtor.getAmount().min(
                            creditor.getAmount()
                    );

            settlements.add(
                    Settlement.createNew(
                            debtor.getParticipantId(),
                            creditor.getParticipantId(),
                            settlementAmount
                    )
            );

            debtor =
                    debtor.subtract(settlementAmount);

            creditor =
                    creditor.subtract(settlementAmount);

            debtors.set(
                    debtorIndex,
                    debtor
            );

            creditors.set(
                    creditorIndex,
                    creditor
            );

            if (debtor.isSettled()) {
                debtorIndex++;
            }

            if (creditor.isSettled()) {
                creditorIndex++;
            }
        }

        return settlements;
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

    private List<Settlement> extractExactSettlements(
            List<Balance> debtors,
            List<Balance> creditors
    ) {
        List<Settlement> settlements = new ArrayList<>();

        for (int debtorIndex = 0;
             debtorIndex < debtors.size();
             debtorIndex++) {

            Balance debtor = debtors.get(debtorIndex);

            if (debtor.isSettled()) {
                continue;
            }

            for (int creditorIndex = 0;
                 creditorIndex < creditors.size();
                 creditorIndex++) {

                Balance creditor = creditors.get(creditorIndex);

                if (creditor.isSettled()) {
                    continue;
                }

                if (!debtor.getAmount().equals(
                        creditor.getAmount()
                )) {
                    continue;
                }

                settlements.add(
                        Settlement.createNew(
                                debtor.getParticipantId(),
                                creditor.getParticipantId(),
                                debtor.getAmount()
                        )
                );

                debtors.set(
                        debtorIndex,
                        debtor.subtract(debtor.getAmount())
                );

                creditors.set(
                        creditorIndex,
                        creditor.subtract(creditor.getAmount())
                );

                break;
            }
        }

        return settlements;
    }

    private void findBestSettlement(
            List<Balance> debtors,
            List<Balance> creditors,
            int debtorIndex,
            List<Settlement> currentSettlement,
            List<Settlement> bestSettlement
    ) {
        if (debtorIndex == debtors.size()) {
            updateBestSettlement(
                    currentSettlement,
                    bestSettlement
            );
            return;
        }

        if (isPruned(
                debtors,
                creditors,
                debtorIndex,
                currentSettlement,
                bestSettlement
        )) {
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

        Money debtorAmount = debtor.getAmount();

        int exactCreditorIndex =
                findExactCreditor(
                        creditors,
                        debtorAmount
                );

        if (exactCreditorIndex >= 0) {
            createSettlementBranch(
                    debtors,
                    creditors,
                    debtorIndex,
                    exactCreditorIndex,
                    currentSettlement,
                    bestSettlement
            );

            return;
        }

        Money previousCreditorAmount = null;

        for (
                int creditorIndex = 0;
                creditorIndex < creditors.size();
                creditorIndex++
        ) {
            Balance creditor =
                    creditors.get(creditorIndex);

            if (creditor.isSettled()) {
                continue;
            }

            if (Objects.equals(
                    previousCreditorAmount,
                    creditor.getAmount()
            )) {
                continue;
            }

            previousCreditorAmount =
                    creditor.getAmount();

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

        List<Settlement> exactSettlements =
                extractExactSettlements(
                        nextDebtors,
                        nextCreditors
                );

        currentSettlement.addAll(exactSettlements);

        findBestSettlement(
                nextDebtors,
                nextCreditors,
                debtorIndex,
                currentSettlement,
                bestSettlement
        );

        currentSettlement.subList(
                currentSettlement.size() - exactSettlements.size(),
                currentSettlement.size()
        ).clear();

        currentSettlement.removeLast();
    }

    private boolean isPruned(
            List<Balance> debtors,
            List<Balance> creditors,
            int debtorIndex,
            List<Settlement> currentSettlement,
            List<Settlement> bestSettlement
    ) {
        if (bestSettlement.isEmpty()) {
            return false;
        }

        int remainingDebtors = 0;

        for (int i = debtorIndex; i < debtors.size(); i++) {
            if (!debtors.get(i).isSettled()) {
                remainingDebtors++;
            }
        }

        int remainingCreditors = 0;

        for (Balance creditor : creditors) {
            if (!creditor.isSettled()) {
                remainingCreditors++;
            }
        }

        int lowerBound = Math.max(
                remainingDebtors,
                remainingCreditors
        );

        return currentSettlement.size() + lowerBound
                >= bestSettlement.size();
    }

    private int findExactCreditor(
            List<Balance> creditors,
            Money debtorAmount
    ) {
        for (int i = 0; i < creditors.size(); i++) {
            Balance creditor = creditors.get(i);

            if (!creditor.isSettled()
                    && creditor.getAmount().equals(debtorAmount)) {
                return i;
            }
        }

        return -1;
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
