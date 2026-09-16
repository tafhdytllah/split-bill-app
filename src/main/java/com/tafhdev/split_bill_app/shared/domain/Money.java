package com.tafhdev.split_bill_app.shared.domain;

import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.domain.exception.Guard;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public static Money of(BigDecimal amount) {
        Guard.requireNotNull(amount, "money amount");

        if (amount.scale() > SCALE) {
            throw new DomainException(
                    "money amount must have at most 2 decimal places"
            );
        }

        return new Money(
                amount.setScale(SCALE, ROUNDING_MODE)
        );
    }

    public static Money rounded(BigDecimal amount) {
        Guard.requireNotNull(amount, "money amount");

        return new Money(
                amount.setScale(SCALE, ROUNDING_MODE)
        );
    }

    public BigDecimal value() {
        return amount;
    }

    public Money add(Money other) {
        return rounded(
                amount.add(other.amount)
        );
    }

    public Money subtract(Money other) {
        return rounded(
                amount.subtract(other.amount)
        );
    }

    public Money multiply(BigDecimal multiplier) {
        Guard.requireNotNull(multiplier, "multiplier");

        return rounded(
                amount.multiply(multiplier)
        );
    }

    public Money divide(int divisor) {
        if (divisor == 0) {
            throw new DomainException(
                    "divisor must not be zero"
            );
        }

        return rounded(
                amount.divide(
                        BigDecimal.valueOf(divisor),
                        SCALE,
                        ROUNDING_MODE
                )
        );
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Money money)) {
            return false;
        }

        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
