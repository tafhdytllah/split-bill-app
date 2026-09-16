package com.tafhdev.split_bill_app.shared.domain;

import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithTwoDecimalPlaces() {
        Money money = Money.of(
                new BigDecimal("100")
        );

        assertThat(money.value())
                .isEqualByComparingTo("100.00");
    }

    @Test
    void shouldNormalizeOneDecimalPlace() {
        Money money = Money.of(
                new BigDecimal("100.5")
        );

        assertThat(money.value())
                .isEqualByComparingTo("100.50");
    }

    @Test
    void shouldRejectMoreThanTwoDecimalPlaces() {
        assertThatThrownBy(() ->
                Money.of(new BigDecimal("100.125"))
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("money amount must have at most 2 decimal places");
    }

    @Test
    void shouldRejectNullAmount() {
        assertThatThrownBy(() ->
                Money.of(null)
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("money amount must not be null");
    }

    @Test
    void shouldRoundCalculationUsingHalfUp() {
        Money money = Money.rounded(
                new BigDecimal("100.125")
        );

        assertThat(money.value())
                .isEqualByComparingTo("100.13");
    }

    @Test
    void shouldAddMoney() {
        Money first = Money.of(new BigDecimal("100.00"));
        Money second = Money.of(new BigDecimal("50.50"));

        Money result = first.add(second);

        assertThat(result.value())
                .isEqualByComparingTo("150.50");
    }

    @Test
    void shouldSubtractMoney() {
        Money first = Money.of(new BigDecimal("100.00"));
        Money second = Money.of(new BigDecimal("30.25"));

        Money result = first.subtract(second);

        assertThat(result.value())
                .isEqualByComparingTo("69.75");
    }

    @Test
    void shouldMultiplyMoney() {
        Money money = Money.of(
                new BigDecimal("100.00")
        );

        Money result = money.multiply(
                new BigDecimal("0.03")
        );

        assertThat(result.value())
                .isEqualByComparingTo("3.00");
    }

    @Test
    void shouldDivideMoneyUsingHalfUp() {
        Money money = Money.of(
                new BigDecimal("100.00")
        );

        Money result = money.divide(3);

        assertThat(result.value())
                .isEqualByComparingTo("33.33");
    }

    @Test
    void shouldRejectZeroDivisor() {
        Money money = Money.of(
                new BigDecimal("100.00")
        );

        assertThatThrownBy(() ->
                money.divide(0)
        )
                .isInstanceOf(DomainException.class)
                .hasMessage("divisor must not be zero");
    }

    @Test
    void shouldIdentifyPositiveMoney() {
        Money money = Money.of(
                new BigDecimal("100.00")
        );

        assertThat(money.isPositive())
                .isTrue();
        assertThat(money.isNegative())
                .isFalse();
        assertThat(money.isZero())
                .isFalse();
    }

    @Test
    void shouldIdentifyNegativeMoney() {
        Money money = Money.rounded(
                new BigDecimal("-100.00")
        );

        assertThat(money.isPositive())
                .isFalse();
        assertThat(money.isNegative())
                .isTrue();
        assertThat(money.isZero())
                .isFalse();
    }

    @Test
    void shouldIdentifyZeroMoney() {
        Money money = Money.of(
                new BigDecimal("0")
        );

        assertThat(money.isPositive())
                .isFalse();
        assertThat(money.isNegative())
                .isFalse();
        assertThat(money.isZero())
                .isTrue();
    }

    @Test
    void shouldCompareMoneyByValue() {
        Money first = Money.of(new BigDecimal("100"));
        Money second = Money.of(new BigDecimal("100.00"));

        assertThat(first)
                .isEqualTo(second);
    }

    @Test
    void shouldHaveSameHashCodeForEqualMoney() {
        Money first = Money.of(new BigDecimal("100"));
        Money second = Money.of(new BigDecimal("100.00"));

        assertThat(first.hashCode())
                .isEqualTo(second.hashCode());
    }

    @Test
    void shouldReturnAmountAsString() {
        Money money = Money.of(
                new BigDecimal("100")
        );

        assertThat(money.toString())
                .isEqualTo("100.00");
    }
}