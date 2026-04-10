package com.ftgo.testlib.builder;

import net.chrisrichardson.ftgo.common.Money;

import java.math.BigDecimal;

/**
 * Builder for {@link Money} test instances.
 *
 * <pre>{@code
 * Money price = MoneyBuilder.money().withAmount("12.99").build();
 * Money zero  = MoneyBuilder.zero();
 * }</pre>
 */
public final class MoneyBuilder {

    private BigDecimal amount = new BigDecimal("10.00");

    private MoneyBuilder() {
    }

    public static MoneyBuilder money() {
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

    public Money build() {
        return new Money(amount);
    }
}
