package com.tafhdev.split_bill_app.settlement.service;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.repository.ExpenseRepository;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.payment.domain.Payment;
import com.tafhdev.split_bill_app.payment.repository.PaymentRepository;
import com.tafhdev.split_bill_app.settlement.controller.dto.SettlementResponse;
import com.tafhdev.split_bill_app.settlement.controller.mapper.SettlementApiMapper;
import com.tafhdev.split_bill_app.settlement.domain.Balance;
import com.tafhdev.split_bill_app.settlement.domain.Settlement;
import com.tafhdev.split_bill_app.settlement.domain.calculator.BalanceCalculator;
import com.tafhdev.split_bill_app.settlement.domain.calculator.SettlementOptimizer;
import com.tafhdev.split_bill_app.settlement.service.dto.GetSettlementCommand;
import com.tafhdev.split_bill_app.shared.application.exception.ResourceNotFoundException;
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
    private final SettlementApiMapper settlementApiMapper;

    public SettlementService(
            BillGroupRepository billGroupRepository,
            ExpenseRepository expenseRepository,
            PaymentRepository paymentRepository,
            BalanceCalculator balanceCalculator,
            SettlementOptimizer settlementOptimizer,
            SettlementApiMapper settlementApiMapper
    ) {
        this.billGroupRepository = billGroupRepository;
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
        this.balanceCalculator = balanceCalculator;
        this.settlementOptimizer = settlementOptimizer;
        this.settlementApiMapper = settlementApiMapper;
    }

    @Transactional(readOnly = true)
    public SettlementResponse getSettlement(GetSettlementCommand command) {

//        long start = System.nanoTime();

        BillGroup group = billGroupRepository.findById(command.groupId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("group not found")
                );

//        long afterGroup = System.nanoTime();

        List<Expense> expenses = expenseRepository.findByGroupId(command.groupId());

//        long afterExpense = System.nanoTime();

        List<Payment> payments = paymentRepository.findByGroupId(command.groupId());

//        long afterPayment = System.nanoTime();

        List<Balance> balances = balanceCalculator.calculate(
                group.getParticipants(),
                expenses,
                payments
        );

//        long afterBalance = System.nanoTime();

        List<Settlement> settlements = settlementOptimizer.optimize(balances);

//        long afterOptimizer = System.nanoTime();

        SettlementResponse response = settlementApiMapper.toResponse(
                group,
                balances,
                settlements
        );

//        long afterMapper = System.nanoTime();

//        System.out.println(
//                "SETTLEMENT TIMING | "
//                        + "group=" + nanosToMillis(afterGroup - start)
//                        + "ms, expense=" + nanosToMillis(afterExpense - afterGroup)
//                        + "ms, payment=" + nanosToMillis(afterPayment - afterExpense)
//                        + "ms, balance=" + nanosToMillis(afterBalance - afterPayment)
//                        + "ms, optimizer=" + nanosToMillis(afterOptimizer - afterBalance)
//                        + "ms, mapper=" + nanosToMillis(afterMapper - afterOptimizer)
//                        + "ms, total=" + nanosToMillis(afterMapper - start)
//                        + "ms"
//        );

        return response;
    }

    private long nanosToMillis(long nanos) {
        return nanos / 1_000_000;
    }
}
