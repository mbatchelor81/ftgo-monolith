package com.ftgo.test.builders;

import net.chrisrichardson.ftgo.common.Money;

import java.math.BigDecimal;

/**
 * Fluent builder for {@link Money} test fixtures.
 *
 * <p>Keeps test setup readable by avoiding the {@code new Money(new BigDecimal("12.50"))}
 * incantation sprinkled through legacy monolith tests. Prefer a builder
 * even for trivial amounts — it makes refactors (e.g. changing the
 * underlying scale) a single-file change.
 *
 * <pre>{@code
 * Money total = MoneyBuilder.aMoney().withAmount("42.00").build();
 * Money zero  = MoneyBuilder.zero();
 * }</pre>
 */
public final class MoneyBuilder {

    private BigDecimal amount = new BigDecimal("0.00");

    private MoneyBuilder() {
    }

    public static MoneyBuilder aMoney() {
        return new MoneyBuilder();
    }

    public static Money zero() {
        return Money.ZERO;
    }

    public MoneyBuilder withAmount(String amount) {
        this.amount = new BigDecimal(amount);
        return this;
    }

    public MoneyBuilder withAmount(int amount) {
        this.amount = new BigDecimal(amount);
        return this;
    }

    public MoneyBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public Money build() {
        return new Money(amount);
    }
}
