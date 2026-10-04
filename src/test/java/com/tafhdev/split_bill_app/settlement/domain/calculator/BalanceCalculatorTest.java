package com.tafhdev.split_bill_app.settlement.domain.calculator;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplitType;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.payment.domain.Payment;
import com.tafhdev.split_bill_app.settlement.domain.Balance;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceCalculatorTest {

    private static final UUID GROUP_ID = UUID.randomUUID();
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

    private final BalanceCalculator calculator = new BalanceCalculator();

    @Test
    void shouldInitializeAllParticipantsWithZeroBalance() {
        UUID participant1Id = UUID.randomUUID();
        UUID participant2Id = UUID.randomUUID();

        List<Balance> balances = calculator.calculate(
                List.of(
                        participant(participant1Id),
                        participant(participant2Id)
                ),
                List.of(),
                List.of()
        );

        assertThat(balances)
                .hasSize(2);

        assertBalance(balances, participant1Id, "0");
        assertBalance(balances, participant2Id, "0");
    }

    @Test
    void shouldAddExpenseAmountToPayerBalance() {
        UUID payerId = UUID.randomUUID();
        UUID participant2Id = UUID.randomUUID();
        UUID participant3Id = UUID.randomUUID();

        Expense expense = expense(
                payerId,
                "100",
                split(participant2Id, "50"),
                split(participant3Id, "50")
        );

        List<Balance> balances = calculator.calculate(
                List.of(
                        participant(payerId),
                        participant(participant2Id),
                        participant(participant3Id)
                ),
                List.of(expense),
                List.of()
        );

        assertBalance(balances, payerId, "100");
    }

    @Test
    void shouldSubtractExpenseSplitAmountFromParticipants() {
        UUID payerId = UUID.randomUUID();
        UUID participant2Id = UUID.randomUUID();
        UUID participant3Id = UUID.randomUUID();

        Expense expense = expense(
                payerId,
                "100",
                split(participant2Id, "40"),
                split(participant3Id, "60")
        );

        List<Balance> balances = calculator.calculate(
                List.of(
                        participant(payerId),
                        participant(participant2Id),
                        participant(participant3Id)
                ),
                List.of(expense),
                List.of()
        );

        assertBalance(balances, participant2Id, "-40");
        assertBalance(balances, participant3Id, "-60");
    }

    @Test
    void shouldApplyMultipleExpenseSplits() {
        UUID payerId = UUID.randomUUID();
        UUID participant2Id = UUID.randomUUID();
        UUID participant3Id = UUID.randomUUID();
        UUID participant4Id = UUID.randomUUID();

        Expense expense = expense(
                payerId,
                "150",
                split(participant2Id, "30"),
                split(participant3Id, "50"),
                split(participant4Id, "70")
        );

        List<Balance> balances = calculator.calculate(
                List.of(
                        participant(payerId),
                        participant(participant2Id),
                        participant(participant3Id),
                        participant(participant4Id)
                ),
                List.of(expense),
                List.of()
        );

        assertBalance(balances, payerId, "150");
        assertBalance(balances, participant2Id, "-30");
        assertBalance(balances, participant3Id, "-50");
        assertBalance(balances, participant4Id, "-70");
    }

    @Test
    void shouldAccumulateMultipleExpenses() {
        UUID payer1Id = UUID.randomUUID();
        UUID payer2Id = UUID.randomUUID();
        UUID participant3Id = UUID.randomUUID();

        Expense expense1 = expense(
                payer1Id,
                "100",
                split(payer2Id, "50"),
                split(participant3Id, "50")
        );

        Expense expense2 = expense(
                payer2Id,
                "60",
                split(payer1Id, "30"),
                split(participant3Id, "30")
        );

        List<Balance> balances = calculator.calculate(
                List.of(
                        participant(payer1Id),
                        participant(payer2Id),
                        participant(participant3Id)
                ),
                List.of(expense1, expense2),
                List.of()
        );

        assertBalance(balances, payer1Id, "70");
        assertBalance(balances, payer2Id, "10");
        assertBalance(balances, participant3Id, "-80");
    }

    @Test
    void shouldKeepZeroBalanceForParticipantWithoutTransaction() {
        UUID payerId = UUID.randomUUID();
        UUID participant2Id = UUID.randomUUID();
        UUID participant3Id = UUID.randomUUID();

        Expense expense = expense(
                payerId,
                "100",
                split(participant2Id, "50"),
                split(participant3Id, "50")
        );

        List<Balance> balances = calculator.calculate(
                List.of(
                        participant(payerId),
                        participant(participant2Id),
                        participant(participant3Id)
                ),
                List.of(expense),
                List.of()
        );

        assertBalance(balances, payerId, "100");
        assertBalance(balances, participant2Id, "-50");
        assertBalance(balances, participant3Id, "-50");
    }

    @Test
    void shouldAddPaymentAmountToFromParticipantBalance() {
        UUID fromParticipantId = UUID.randomUUID();
        UUID toParticipantId = UUID.randomUUID();

        Payment payment = payment(
                fromParticipantId,
                toParticipantId,
                "50"
        );

        List<Balance> balances = calculator.calculate(
                List.of(
                        participant(fromParticipantId),
                        participant(toParticipantId)
                ),
                List.of(),
                List.of(payment)
        );

        assertBalance(balances, fromParticipantId, "50");
        assertBalance(balances, toParticipantId, "-50");
    }

    @Test
    void shouldApplyMultiplePayments() {
        UUID participant1Id = UUID.randomUUID();
        UUID participant2Id = UUID.randomUUID();
        UUID participant3Id = UUID.randomUUID();

        Payment payment1 = payment(
                participant1Id,
                participant2Id,
                "30"
        );

        Payment payment2 = payment(
                participant1Id,
                participant3Id,
                "20"
        );

        List<Balance> balances = calculator.calculate(
                List.of(
                        participant(participant1Id),
                        participant(participant2Id),
                        participant(participant3Id)
                ),
                List.of(),
                List.of(payment1, payment2)
        );

        assertBalance(balances, participant1Id, "50");
        assertBalance(balances, participant2Id, "-30");
        assertBalance(balances, participant3Id, "-20");
    }

    @Test
    void shouldApplyExpensesAndPaymentsTogether() {
        UUID payerId = UUID.randomUUID();
        UUID participant2Id = UUID.randomUUID();
        UUID participant3Id = UUID.randomUUID();

        Expense expense = expense(
                payerId,
                "100",
                split(participant2Id, "50"),
                split(participant3Id, "50")
        );

        Payment payment = payment(
                participant2Id,
                payerId,
                "40"
        );

        List<Balance> balances = calculator.calculate(
                List.of(
                        participant(payerId),
                        participant(participant2Id),
                        participant(participant3Id)
                ),
                List.of(expense),
                List.of(payment)
        );

        assertBalance(balances, payerId, "60");
        assertBalance(balances, participant2Id, "-10");
        assertBalance(balances, participant3Id, "-50");
    }

    @Test
    void shouldPreserveBalanceForParticipantAfterMultipleTransactions() {
        UUID participant1Id = UUID.randomUUID();
        UUID participant2Id = UUID.randomUUID();
        UUID participant3Id = UUID.randomUUID();

        Expense expense1 = expense(
                participant1Id,
                "120",
                split(participant2Id, "60"),
                split(participant3Id, "60")
        );

        Payment payment = payment(
                participant2Id,
                participant1Id,
                "20"
        );

        Expense expense2 = expense(
                participant3Id,
                "60",
                split(participant1Id, "30"),
                split(participant2Id, "30")
        );

        List<Balance> balances = calculator.calculate(
                List.of(
                        participant(participant1Id),
                        participant(participant2Id),
                        participant(participant3Id)
                ),
                List.of(expense1, expense2),
                List.of(payment)
        );

        assertBalance(balances, participant1Id, "70");
        assertBalance(balances, participant2Id, "-70");
        assertBalance(balances, participant3Id, "0");
    }

    @Test
    void shouldNotModifyInputExpensesOrPayments() {
        UUID payerId = UUID.randomUUID();
        UUID participant2Id = UUID.randomUUID();
        UUID participant3Id = UUID.randomUUID();

        Expense expense = expense(
                payerId,
                "100",
                split(participant2Id, "50"),
                split(participant3Id, "50")
        );

        Payment payment = payment(
                participant2Id,
                payerId,
                "20"
        );

        calculator.calculate(
                List.of(
                        participant(payerId),
                        participant(participant2Id),
                        participant(participant3Id)
                ),
                List.of(expense),
                List.of(payment)
        );

        assertThat(expense.getAmount())
                .isEqualTo(Money.of(new BigDecimal("100")));

        assertThat(payment.getAmount())
                .isEqualTo(Money.of(new BigDecimal("20")));

        assertThat(expense.getPaidBy())
                .isEqualTo(payerId);

        assertThat(payment.getFromParticipantId())
                .isEqualTo(participant2Id);

        assertThat(payment.getToParticipantId())
                .isEqualTo(payerId);
    }

    private Participant participant(UUID id) {
        return Participant.createNew(
                id,
                GROUP_ID,
                "Participant",
                CREATED_AT
        );
    }

    private ExpenseSplit split(
            UUID participantId,
            String amount
    ) {
        return ExpenseSplit.createNew(
                UUID.randomUUID(),
                participantId,
                Money.of(new BigDecimal(amount))
        );
    }

    private Expense expense(
            UUID paidBy,
            String amount,
            ExpenseSplit... splits
    ) {
        return Expense.createNew(
                UUID.randomUUID(),
                GROUP_ID,
                paidBy,
                Money.of(new BigDecimal(amount)),
                ExpenseCategory.FOOD,
                ExpenseSplitType.EXACT,
                List.of(splits),
                CREATED_AT
        );
    }

    private Payment payment(
            UUID fromParticipantId,
            UUID toParticipantId,
            String amount
    ) {
        return Payment.createNew(
                UUID.randomUUID(),
                GROUP_ID,
                fromParticipantId,
                toParticipantId,
                Money.of(new BigDecimal(amount)),
                CREATED_AT
        );
    }

    private void assertBalance(
            List<Balance> balances,
            UUID participantId,
            String expectedAmount
    ) {
        Balance balance = balances.stream()
                .filter(item ->
                        item.getParticipantId().equals(participantId)
                )
                .findFirst()
                .orElseThrow();

        assertThat(balance.getAmount())
                .isEqualTo(
                        Money.of(new BigDecimal(expectedAmount))
                );
    }
}