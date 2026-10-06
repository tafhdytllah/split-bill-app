package com.tafhdev.split_bill_app.payment.service;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplitType;
import com.tafhdev.split_bill_app.payment.domain.Payment;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentOutstandingCalculatorTest {

    private PaymentOutstandingCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PaymentOutstandingCalculator();
    }

    @Test
    void shouldReturnZeroWhenThereIsNoExpenseAndNoPayment() {

        UUID fromParticipantId = UUID.randomUUID();
        UUID toParticipantId = UUID.randomUUID();

        Money result = calculator.calculate(
                fromParticipantId,
                toParticipantId,
                List.of(),
                List.of()
        );

        assertThat(result.value())
                .isEqualByComparingTo("0.00");
    }

    @Test
    void shouldCalculateOutstandingDebt() {

        UUID groupId = UUID.randomUUID();
        UUID andi = UUID.randomUUID();
        UUID budi = UUID.randomUUID();

        Expense expense = createExpense(
                groupId,
                andi,
                budi,
                "50.00"
        );

        Money result = calculator.calculate(
                budi,
                andi,
                List.of(expense),
                List.of()
        );

        assertThat(result.value())
                .isEqualByComparingTo("50.00");
    }

    @Test
    void shouldSubtractPreviousPaymentFromOutstandingDebt() {

        UUID groupId = UUID.randomUUID();
        UUID andi = UUID.randomUUID();
        UUID budi = UUID.randomUUID();

        Expense expense = createExpense(
                groupId,
                andi,
                budi,
                "50.00"
        );

        Payment payment = createPayment(
                groupId,
                budi,
                andi,
                "20.00"
        );

        Money result = calculator.calculate(
                budi,
                andi,
                List.of(expense),
                List.of(payment)
        );

        assertThat(result.value())
                .isEqualByComparingTo("30.00");
    }

    @Test
    void shouldReturnZeroWhenDebtIsFullyPaid() {

        UUID groupId = UUID.randomUUID();
        UUID andi = UUID.randomUUID();
        UUID budi = UUID.randomUUID();

        Expense expense = createExpense(
                groupId,
                andi,
                budi,
                "50.00"
        );

        Payment payment = createPayment(
                groupId,
                budi,
                andi,
                "50.00"
        );

        Money result = calculator.calculate(
                budi,
                andi,
                List.of(expense),
                List.of(payment)
        );

        assertThat(result.value())
                .isEqualByComparingTo("0.00");
    }

    @Test
    void shouldNotSubtractPaymentInOppositeDirection() {

        UUID groupId = UUID.randomUUID();
        UUID andi = UUID.randomUUID();
        UUID budi = UUID.randomUUID();

        Expense expense = createExpense(
                groupId,
                andi,
                budi,
                "50.00"
        );

        Payment payment = createPayment(
                groupId,
                andi,
                budi,
                "20.00"
        );

        Money result = calculator.calculate(
                budi,
                andi,
                List.of(expense),
                List.of(payment)
        );

        assertThat(result.value())
                .isEqualByComparingTo("50.00");
    }

    @Test
    void shouldCalculateMultipleExpenses() {

        UUID groupId = UUID.randomUUID();
        UUID andi = UUID.randomUUID();
        UUID budi = UUID.randomUUID();

        Expense expense1 = createExpense(
                groupId,
                andi,
                budi,
                "50.00"
        );

        Expense expense2 = createExpense(
                groupId,
                andi,
                budi,
                "30.00"
        );

        Money result = calculator.calculate(
                budi,
                andi,
                List.of(expense1, expense2),
                List.of()
        );

        assertThat(result.value())
                .isEqualByComparingTo("80.00");
    }

    @Test
    void shouldCalculateMultiplePayments() {

        UUID groupId = UUID.randomUUID();
        UUID andi = UUID.randomUUID();
        UUID budi = UUID.randomUUID();

        Expense expense = createExpense(
                groupId,
                andi,
                budi,
                "100.00"
        );

        Payment payment1 = createPayment(
                groupId,
                budi,
                andi,
                "30.00"
        );

        Payment payment2 = createPayment(
                groupId,
                budi,
                andi,
                "20.00"
        );

        Money result = calculator.calculate(
                budi,
                andi,
                List.of(expense),
                List.of(payment1, payment2)
        );

        assertThat(result.value())
                .isEqualByComparingTo("50.00");
    }

    @Test
    void shouldOnlyCalculateDebtBetweenSpecifiedParticipants() {

        UUID groupId = UUID.randomUUID();

        UUID andi = UUID.randomUUID();
        UUID budi = UUID.randomUUID();
        UUID cika = UUID.randomUUID();

        Expense budiExpense = createExpense(
                groupId,
                andi,
                budi,
                "50.00"
        );

        Expense cikaExpense = createExpense(
                groupId,
                andi,
                cika,
                "100.00"
        );

        Money result = calculator.calculate(
                budi,
                andi,
                List.of(budiExpense, cikaExpense),
                List.of()
        );

        assertThat(result.value())
                .isEqualByComparingTo("50.00");
    }

    @Test
    void shouldReturnZeroWhenThereIsNoDebtBetweenParticipants() {

        UUID groupId = UUID.randomUUID();

        UUID andi = UUID.randomUUID();
        UUID budi = UUID.randomUUID();
        UUID cika = UUID.randomUUID();

        Expense expense = createExpense(
                groupId,
                andi,
                cika,
                "50.00"
        );

        Money result = calculator.calculate(
                budi,
                andi,
                List.of(expense),
                List.of()
        );

        assertThat(result.value())
                .isEqualByComparingTo("0.00");
    }

    private Expense createExpense(
            UUID groupId,
            UUID payerId,
            UUID debtorId,
            String debtorAmount
    ) {
        Money totalAmount = Money.of(
                new BigDecimal("100.00")
        );

        Money debtorMoney = Money.of(
                new BigDecimal(debtorAmount)
        );

        Money payerAmount = totalAmount.subtract(debtorMoney);

        return Expense.createNew(
                UUID.randomUUID(),
                groupId,
                payerId,
                totalAmount,
                ExpenseCategory.FOOD,
                ExpenseSplitType.EXACT,
                List.of(
                        ExpenseSplit.createNew(
                                UUID.randomUUID(),
                                payerId,
                                payerAmount
                        ),
                        ExpenseSplit.createNew(
                                UUID.randomUUID(),
                                debtorId,
                                debtorMoney
                        )
                ),
                Instant.now()
        );
    }

    private Payment createPayment(
            UUID groupId,
            UUID fromParticipantId,
            UUID toParticipantId,
            String amount
    ) {
        return Payment.createNew(
                UUID.randomUUID(),
                groupId,
                fromParticipantId,
                toParticipantId,
                Money.of(new BigDecimal(amount)),
                Instant.now()
        );
    }
}