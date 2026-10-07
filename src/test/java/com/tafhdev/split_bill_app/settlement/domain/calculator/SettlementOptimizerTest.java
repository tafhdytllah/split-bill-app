package com.tafhdev.split_bill_app.settlement.domain.calculator;

import com.tafhdev.split_bill_app.settlement.domain.Balance;
import com.tafhdev.split_bill_app.settlement.domain.Settlement;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class SettlementOptimizerTest {

    private SettlementOptimizer optimizer;

    @BeforeEach
    void setUp() {
        optimizer = new SettlementOptimizer();
    }

    // =========================================================
    // BASIC SETTLEMENT
    // =========================================================

    @Test
    void shouldCreateOneSettlementForOneDebtorAndOneCreditor() {
        UUID debtorId = UUID.randomUUID();
        UUID creditorId = UUID.randomUUID();

        List<Balance> balances = List.of(
                balance(debtorId, "-100.00"),
                balance(creditorId, "100.00")
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        assertThat(result)
                .hasSize(1);

        assertThat(result.getFirst())
                .satisfies(settlement -> {
                    assertThat(settlement.getFromParticipantId())
                            .isEqualTo(debtorId);

                    assertThat(settlement.getToParticipantId())
                            .isEqualTo(creditorId);

                    assertThat(settlement.getAmount().value())
                            .isEqualByComparingTo("100.00");
                });
    }

    @Test
    void shouldUseMinimumOfDebtorAndCreditorAmount() {
        UUID debtor1Id = UUID.randomUUID();
        UUID debtor2Id = UUID.randomUUID();

        UUID creditor1Id = UUID.randomUUID();
        UUID creditor2Id = UUID.randomUUID();

        List<Balance> balances = List.of(
                balance(debtor1Id, "-80.00"),
                balance(debtor2Id, "-20.00"),
                balance(creditor1Id, "50.00"),
                balance(creditor2Id, "50.00")
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        /*
         * Salah satu solusi optimal:
         *
         * debtor1 -> creditor1 = 50
         * debtor1 -> creditor2 = 30
         * debtor2 -> creditor2 = 20
         *
         * Total = 3 transaksi.
         * Solusi optimal lain dengan pasangan creditor berbeda juga valid.
         */

        assertThat(result)
                .hasSize(3);

        assertThat(result)
                .allSatisfy(settlement -> {
                    assertThat(settlement.getAmount().value())
                            .isPositive();
                });

        assertThat(totalSettlementAmount(result))
                .isEqualByComparingTo("100.00");
    }


    // =========================================================
    // MULTIPLE DEBTORS / ONE CREDITOR
    // =========================================================

    @Test
    void shouldHandleMultipleDebtorsAndOneCreditor() {
        UUID debtor1Id = UUID.randomUUID();
        UUID debtor2Id = UUID.randomUUID();
        UUID debtor3Id = UUID.randomUUID();

        UUID creditorId = UUID.randomUUID();

        List<Balance> balances = List.of(
                balance(debtor1Id, "-30.00"),
                balance(debtor2Id, "-20.00"),
                balance(debtor3Id, "-50.00"),
                balance(creditorId, "100.00")
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        assertThat(result)
                .hasSize(3);

        assertThat(result)
                .allSatisfy(settlement -> {
                    assertThat(settlement.getToParticipantId())
                            .isEqualTo(creditorId);
                });

        assertThat(result)
                .extracting(
                        Settlement::getFromParticipantId,
                        settlement -> settlement.getAmount().value()
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                debtor1Id,
                                new BigDecimal("30.00")
                        ),
                        tuple(
                                debtor2Id,
                                new BigDecimal("20.00")
                        ),
                        tuple(
                                debtor3Id,
                                new BigDecimal("50.00")
                        )
                );
    }


    // =========================================================
    // ONE DEBTOR / MULTIPLE CREDITORS
    // =========================================================

    @Test
    void shouldHandleOneDebtorAndMultipleCreditors() {
        UUID debtorId = UUID.randomUUID();

        UUID creditor1Id = UUID.randomUUID();
        UUID creditor2Id = UUID.randomUUID();
        UUID creditor3Id = UUID.randomUUID();

        List<Balance> balances = List.of(
                balance(debtorId, "-100.00"),
                balance(creditor1Id, "20.00"),
                balance(creditor2Id, "30.00"),
                balance(creditor3Id, "50.00")
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        assertThat(result)
                .hasSize(3);

        assertThat(result)
                .allSatisfy(settlement -> {
                    assertThat(settlement.getFromParticipantId())
                            .isEqualTo(debtorId);
                });

        assertThat(result)
                .extracting(
                        Settlement::getToParticipantId,
                        settlement -> settlement.getAmount().value()
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                creditor1Id,
                                new BigDecimal("20.00")
                        ),
                        tuple(
                                creditor2Id,
                                new BigDecimal("30.00")
                        ),
                        tuple(
                                creditor3Id,
                                new BigDecimal("50.00")
                        )
                );
    }


    // =========================================================
    // EXACT MATCH
    // =========================================================

    @Test
    void shouldCreateMinimumSettlementsWhenAmountsMatchExactly() {
        UUID debtor1Id = UUID.randomUUID();
        UUID debtor2Id = UUID.randomUUID();

        UUID creditor1Id = UUID.randomUUID();
        UUID creditor2Id = UUID.randomUUID();

        List<Balance> balances = List.of(
                balance(debtor1Id, "-50.00"),
                balance(debtor2Id, "-50.00"),
                balance(creditor1Id, "50.00"),
                balance(creditor2Id, "50.00")
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        /*
         * Minimum = 2
         *
         * debtor1 -> creditorX = 50
         * debtor2 -> creditorY = 50
         */

        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .extracting(
                        settlement -> settlement.getAmount().value()
                )
                .containsOnly(
                        new BigDecimal("50.00")
                );
    }


    // =========================================================
    // IMPORTANT:
    // BACKTRACKING TEST
    // =========================================================

    @Test
    void shouldBacktrackWhenFirstSolutionIsNotOptimal() {
        UUID debtor1Id = UUID.randomUUID();
        UUID debtor2Id = UUID.randomUUID();

        UUID creditor1Id = UUID.randomUUID();
        UUID creditor2Id = UUID.randomUUID();

        /*
         * Debtors:
         *
         * debtor1 = -6
         * debtor2 = -4
         *
         * Creditors:
         *
         * creditor1 = +4
         * creditor2 = +6
         */

        List<Balance> balances = List.of(
                balance(debtor1Id, "-6.00"),
                balance(debtor2Id, "-4.00"),
                balance(creditor1Id, "4.00"),
                balance(creditor2Id, "6.00")
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        /*
         * DFS akan mencoba creditor pertama dahulu.
         *
         * FIRST BRANCH:
         *
         * debtor1 -> creditor1 = 4
         *
         * remaining:
         *
         * debtor1 = 2
         * debtor2 = 4
         * creditor2 = 6
         *
         * lalu:
         *
         * debtor1 -> creditor2 = 2
         * debtor2 -> creditor2 = 4
         *
         * total = 3 transactions
         *
         *
         * Tetapi ada solusi lebih baik:
         *
         * debtor1 -> creditor2 = 6
         * debtor2 -> creditor1 = 4
         *
         * total = 2 transactions
         *
         * Jadi optimizer HARUS backtracking
         * dari branch pertama untuk menemukan solusi 2 transaksi.
         */

        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .extracting(
                        Settlement::getFromParticipantId,
                        Settlement::getToParticipantId,
                        settlement -> settlement.getAmount().value()
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                debtor1Id,
                                creditor2Id,
                                new BigDecimal("6.00")
                        ),
                        tuple(
                                debtor2Id,
                                creditor1Id,
                                new BigDecimal("4.00")
                        )
                );
    }


    // =========================================================
    // BACKTRACKING WITH MORE PARTICIPANTS
    // =========================================================

    @Test
    void shouldFindOptimalSolutionAmongMultiplePossibleCombinations() {
        UUID debtor1Id = UUID.randomUUID();
        UUID debtor2Id = UUID.randomUUID();
        UUID debtor3Id = UUID.randomUUID();

        UUID creditor1Id = UUID.randomUUID();
        UUID creditor2Id = UUID.randomUUID();
        UUID creditor3Id = UUID.randomUUID();

        /*
         * Debtors:
         *
         * D1 = -8
         * D2 = -7
         * D3 = -5
         *
         * Creditors:
         *
         * C1 = +5
         * C2 = +8
         * C3 = +7
         *
         * Optimal:
         *
         * D1 -> C2 = 8
         * D2 -> C3 = 7
         * D3 -> C1 = 5
         *
         * = 3 transactions
         *
         * Tetapi DFS dengan urutan creditor:
         *
         * C1, C2, C3
         *
         * akan mencoba kombinasi yang tidak optimal terlebih dahulu.
         *
         * Optimizer harus tetap menemukan 3 transaksi.
         */

        List<Balance> balances = List.of(
                balance(debtor1Id, "-8.00"),
                balance(debtor2Id, "-7.00"),
                balance(debtor3Id, "-5.00"),

                balance(creditor1Id, "5.00"),
                balance(creditor2Id, "8.00"),
                balance(creditor3Id, "7.00")
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        assertThat(result)
                .hasSize(3);

        assertThat(result)
                .extracting(
                        Settlement::getFromParticipantId,
                        Settlement::getToParticipantId,
                        settlement -> settlement.getAmount().value()
                )
                .containsExactlyInAnyOrder(
                        tuple(
                                debtor1Id,
                                creditor2Id,
                                new BigDecimal("8.00")
                        ),
                        tuple(
                                debtor2Id,
                                creditor3Id,
                                new BigDecimal("7.00")
                        ),
                        tuple(
                                debtor3Id,
                                creditor1Id,
                                new BigDecimal("5.00")
                        )
                );
    }


    // =========================================================
    // ZERO BALANCE
    // =========================================================

    @Test
    void shouldIgnoreZeroBalance() {
        UUID debtorId = UUID.randomUUID();
        UUID balancedId = UUID.randomUUID();
        UUID creditorId = UUID.randomUUID();

        List<Balance> balances = List.of(
                balance(debtorId, "-100.00"),
                balance(balancedId, "0.00"),
                balance(creditorId, "100.00")
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        assertThat(result)
                .hasSize(1);

        Settlement settlement =
                result.getFirst();

        assertThat(settlement.getFromParticipantId())
                .isEqualTo(debtorId);

        assertThat(settlement.getToParticipantId())
                .isEqualTo(creditorId);

        assertThat(settlement.getAmount().value())
                .isEqualByComparingTo("100.00");

        assertThat(
                result.stream()
                        .flatMap(s ->
                                Stream.of(
                                        s.getFromParticipantId(),
                                        s.getToParticipantId()
                                )
                        )
        ).doesNotContain(balancedId);
    }


    // =========================================================
    // ALL ZERO
    // =========================================================

    @Test
    void shouldReturnEmptyWhenAllBalancesAreZero() {
        List<Balance> balances = List.of(
                balance(
                        UUID.randomUUID(),
                        "0.00"
                ),
                balance(
                        UUID.randomUUID(),
                        "0.00"
                ),
                balance(
                        UUID.randomUUID(),
                        "0.00"
                )
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        assertThat(result)
                .isEmpty();
    }


    // =========================================================
    // EMPTY INPUT
    // =========================================================

    @Test
    void shouldReturnEmptyWhenBalancesAreEmpty() {
        List<Settlement> result =
                optimizer.optimize(List.of());

        assertThat(result)
                .isEmpty();
    }


    // =========================================================
    // ONLY DEBTORS
    // =========================================================

    @Test
    void shouldReturnEmptyWhenThereAreOnlyDebtors() {
        List<Balance> balances = List.of(
                balance(
                        UUID.randomUUID(),
                        "-100.00"
                ),
                balance(
                        UUID.randomUUID(),
                        "-50.00"
                )
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        assertThat(result)
                .isEmpty();
    }


    // =========================================================
    // ONLY CREDITORS
    // =========================================================

    @Test
    void shouldReturnEmptyWhenThereAreOnlyCreditors() {
        List<Balance> balances = List.of(
                balance(
                        UUID.randomUUID(),
                        "100.00"
                ),
                balance(
                        UUID.randomUUID(),
                        "50.00"
                )
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        assertThat(result)
                .isEmpty();
    }


    // =========================================================
    // INPUT IMMUTABILITY
    // =========================================================

    @Test
    void shouldNotModifyOriginalBalances() {
        UUID debtorId = UUID.randomUUID();
        UUID creditorId = UUID.randomUUID();

        Balance debtor =
                balance(
                        debtorId,
                        "-100.00"
                );

        Balance creditor =
                balance(
                        creditorId,
                        "100.00"
                );

        List<Balance> balances =
                List.of(
                        debtor,
                        creditor
                );

        optimizer.optimize(balances);

        assertThat(debtor.getParticipantId())
                .isEqualTo(debtorId);

        assertThat(debtor.getAmount().value())
                .isEqualByComparingTo("-100.00");

        assertThat(creditor.getParticipantId())
                .isEqualTo(creditorId);

        assertThat(creditor.getAmount().value())
                .isEqualByComparingTo("100.00");
    }


    // =========================================================
    // SETTLEMENT INVARIANTS
    // =========================================================

    @Test
    void shouldNeverCreateZeroOrNegativeSettlement() {
        UUID debtor1Id = UUID.randomUUID();
        UUID debtor2Id = UUID.randomUUID();

        UUID creditor1Id = UUID.randomUUID();
        UUID creditor2Id = UUID.randomUUID();

        List<Balance> balances = List.of(
                balance(debtor1Id, "-70.00"),
                balance(debtor2Id, "-30.00"),
                balance(creditor1Id, "60.00"),
                balance(creditor2Id, "40.00")
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        assertThat(result)
                .isNotEmpty()
                .allSatisfy(settlement -> {
                    assertThat(
                            settlement.getAmount().value()
                    ).isGreaterThan(BigDecimal.ZERO);
                });
    }

    @Test
    void shouldNeverCreateSettlementBetweenSameParticipant() {
        UUID debtor1Id = UUID.randomUUID();
        UUID debtor2Id = UUID.randomUUID();

        UUID creditor1Id = UUID.randomUUID();
        UUID creditor2Id = UUID.randomUUID();

        List<Balance> balances = List.of(
                balance(debtor1Id, "-70.00"),
                balance(debtor2Id, "-30.00"),
                balance(creditor1Id, "60.00"),
                balance(creditor2Id, "40.00")
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        assertThat(result)
                .allSatisfy(settlement -> {
                    assertThat(
                            settlement.getFromParticipantId()
                    ).isNotEqualTo(
                            settlement.getToParticipantId()
                    );
                });
    }


    // =========================================================
    // TOTAL SETTLEMENT MUST EQUAL TOTAL DEBT
    // =========================================================

    @Test
    void shouldSettleEntireDebt() {
        UUID debtor1Id = UUID.randomUUID();
        UUID debtor2Id = UUID.randomUUID();

        UUID creditor1Id = UUID.randomUUID();
        UUID creditor2Id = UUID.randomUUID();

        List<Balance> balances = List.of(
                balance(debtor1Id, "-70.00"),
                balance(debtor2Id, "-30.00"),
                balance(creditor1Id, "60.00"),
                balance(creditor2Id, "40.00")
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        BigDecimal totalDebt =
                balances.stream()
                        .filter(Balance::isDebtor)
                        .map(Balance::getAmount)
                        .map(Money::value)
                        .map(BigDecimal::abs)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalSettlement =
                totalSettlementAmount(result);

        assertThat(totalSettlement)
                .isEqualByComparingTo(totalDebt);
    }


    // =========================================================
    // TOTAL SETTLEMENT MUST EQUAL TOTAL RECEIVABLE
    // =========================================================

    @Test
    void shouldSettleEntireReceivable() {
        UUID debtor1Id = UUID.randomUUID();
        UUID debtor2Id = UUID.randomUUID();

        UUID creditor1Id = UUID.randomUUID();
        UUID creditor2Id = UUID.randomUUID();

        List<Balance> balances = List.of(
                balance(debtor1Id, "-70.00"),
                balance(debtor2Id, "-30.00"),
                balance(creditor1Id, "60.00"),
                balance(creditor2Id, "40.00")
        );

        List<Settlement> result =
                optimizer.optimize(balances);

        BigDecimal totalReceivable =
                balances.stream()
                        .filter(Balance::isCreditor)
                        .map(Balance::getAmount)
                        .map(Money::value)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalSettlement =
                totalSettlementAmount(result);

        assertThat(totalSettlement)
                .isEqualByComparingTo(totalReceivable);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Balance balance(
            UUID participantId,
            String amount
    ) {
        return Balance.of(
                participantId,
                Money.of(
                        new BigDecimal(amount)
                )
        );
    }

    private BigDecimal totalSettlementAmount(
            List<Settlement> settlements
    ) {
        return settlements.stream()
                .map(Settlement::getAmount)
                .map(Money::value)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}