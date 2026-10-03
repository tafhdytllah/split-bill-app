package com.tafhdev.split_bill_app.settlement.service;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.repository.ExpenseRepository;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.payment.domain.Payment;
import com.tafhdev.split_bill_app.payment.repository.PaymentRepository;
import com.tafhdev.split_bill_app.settlement.domain.Balance;
import com.tafhdev.split_bill_app.settlement.domain.Settlement;
import com.tafhdev.split_bill_app.settlement.domain.calculator.BalanceCalculator;
import com.tafhdev.split_bill_app.settlement.domain.calculator.SettlementOptimizer;
import com.tafhdev.split_bill_app.settlement.service.dto.command.GetSettlementCommand;
import com.tafhdev.split_bill_app.settlement.service.dto.result.SettlementItemResult;
import com.tafhdev.split_bill_app.settlement.service.dto.result.SettlementResult;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SettlementService {

    private final BillGroupRepository billGroupRepository;
    private final ExpenseRepository expenseRepository;
    private final PaymentRepository paymentRepository;
    private final BalanceCalculator balanceCalculator;
    private final SettlementOptimizer settlementOptimizer;

    public SettlementService(
            BillGroupRepository billGroupRepository,
            ExpenseRepository expenseRepository,
            PaymentRepository paymentRepository,
            BalanceCalculator balanceCalculator,
            SettlementOptimizer settlementOptimizer
    ) {
        this.billGroupRepository = billGroupRepository;
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
        this.balanceCalculator = balanceCalculator;
        this.settlementOptimizer = settlementOptimizer;
    }

    @Transactional(readOnly = true)
    public SettlementResult getSettlement(GetSettlementCommand command) {

        BillGroup group = billGroupRepository.findById(command.groupId())
                .orElseThrow(() -> new DomainException("group not found"));

        List<Expense> expenses = expenseRepository.findByGroupId(command.groupId());

        List<Payment> payments = paymentRepository.findByGroupId(command.groupId());

        List<Balance> balances = balanceCalculator.calculate(
                group.getParticipants(),
                expenses,
                payments
        );

        List<Settlement> settlements = settlementOptimizer.optimize(balances);

        return toResult(
                group,
                settlements
        );
    }

    private SettlementResult toResult(
            BillGroup group,
            List<Settlement> settlements
    ) {
        List<SettlementItemResult> items = settlements.stream()
                .map(settlement -> new SettlementItemResult(
                        settlement.getFromParticipantId(),
                        settlement.getToParticipantId(),
                        settlement.getAmount().value()
                ))
                .toList();

        return new SettlementResult(
                group.getId(),
                items
        );
    }
}
