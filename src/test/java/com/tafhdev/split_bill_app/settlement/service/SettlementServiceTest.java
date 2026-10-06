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
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private BillGroupRepository billGroupRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BalanceCalculator balanceCalculator;

    @Mock
    private SettlementOptimizer settlementOptimizer;

    @Mock
    private SettlementApiMapper settlementApiMapper;

    @InjectMocks
    private SettlementService settlementService;

    @Test
    void shouldGetSettlement() {
        UUID groupId = UUID.randomUUID();

        GetSettlementCommand command =
                new GetSettlementCommand(groupId);

        BillGroup group = mock(BillGroup.class);

        Expense expense = mock(Expense.class);
        Payment payment = mock(Payment.class);

        Balance balance = Balance.of(
                UUID.randomUUID(),
                Money.of(new BigDecimal("100000.00"))
        );

        Settlement settlement = Settlement.createNew(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Money.of(new BigDecimal("100000.00"))
        );

        SettlementResponse response = mock(SettlementResponse.class);

        List<Expense> expenses = List.of(expense);
        List<Payment> payments = List.of(payment);
        List<Balance> balances = List.of(balance);
        List<Settlement> settlements = List.of(settlement);

        when(billGroupRepository.findById(groupId))
                .thenReturn(java.util.Optional.of(group));

        when(expenseRepository.findByGroupId(groupId))
                .thenReturn(expenses);

        when(paymentRepository.findByGroupId(groupId))
                .thenReturn(payments);

        when(balanceCalculator.calculate(
                group.getParticipants(),
                expenses,
                payments
        )).thenReturn(balances);

        when(settlementOptimizer.optimize(balances))
                .thenReturn(settlements);

        when(settlementApiMapper.toResponse(
                group,
                balances,
                settlements
        )).thenReturn(response);

        SettlementResponse result =
                settlementService.getSettlement(command);

        assertThat(result)
                .isSameAs(response);

        verify(billGroupRepository)
                .findById(groupId);

        verify(expenseRepository)
                .findByGroupId(groupId);

        verify(paymentRepository)
                .findByGroupId(groupId);

        verify(balanceCalculator)
                .calculate(
                        group.getParticipants(),
                        expenses,
                        payments
                );

        verify(settlementOptimizer)
                .optimize(balances);

        verify(settlementApiMapper)
                .toResponse(
                        group,
                        balances,
                        settlements
                );
    }

    @Test
    void shouldReturnEmptySettlementWhenThereAreNoExpensesAndPayments() {
        UUID groupId = UUID.randomUUID();

        GetSettlementCommand command =
                new GetSettlementCommand(groupId);

        BillGroup group = mock(BillGroup.class);

        List<Expense> expenses = List.of();
        List<Payment> payments = List.of();
        List<Balance> balances = List.of();
        List<Settlement> settlements = List.of();

        SettlementResponse response = mock(SettlementResponse.class);

        when(billGroupRepository.findById(groupId))
                .thenReturn(java.util.Optional.of(group));

        when(expenseRepository.findByGroupId(groupId))
                .thenReturn(expenses);

        when(paymentRepository.findByGroupId(groupId))
                .thenReturn(payments);

        when(balanceCalculator.calculate(
                group.getParticipants(),
                expenses,
                payments
        )).thenReturn(balances);

        when(settlementOptimizer.optimize(balances))
                .thenReturn(settlements);

        when(settlementApiMapper.toResponse(
                group,
                balances,
                settlements
        )).thenReturn(response);

        SettlementResponse result =
                settlementService.getSettlement(command);

        assertThat(result)
                .isSameAs(response);

        verify(balanceCalculator)
                .calculate(
                        group.getParticipants(),
                        expenses,
                        payments
                );

        verify(settlementOptimizer)
                .optimize(balances);

        verify(settlementApiMapper)
                .toResponse(
                        group,
                        balances,
                        settlements
                );
    }

    @Test
    void shouldThrowWhenGroupDoesNotExist() {
        UUID groupId = UUID.randomUUID();

        GetSettlementCommand command =
                new GetSettlementCommand(groupId);

        when(billGroupRepository.findById(groupId))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() ->
                settlementService.getSettlement(command)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("group not found");

        verify(billGroupRepository)
                .findById(groupId);

        verifyNoInteractions(
                expenseRepository,
                paymentRepository,
                balanceCalculator,
                settlementOptimizer,
                settlementApiMapper
        );
    }
}