package com.ftgo.common;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** Embeddable value object representing a monetary amount backed by {@link BigDecimal}. */
@Embeddable
@Access(AccessType.FIELD)
public class Money {

    /** Zero money constant. */
    public static final Money ZERO = new Money(0);

    private BigDecimal amount;

    private Money() {}

    /**
     * Creates a Money from a {@link BigDecimal}.
     *
     * @param amount the monetary amount
     */
    public Money(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Creates a Money by parsing a decimal string.
     *
     * @param s the string representation of the amount
     */
    public Money(String s) {
        this.amount = new BigDecimal(s);
    }

    /**
     * Creates a Money from an integer value.
     *
     * @param i the integer amount
     */
    public Money(int i) {
        this.amount = new BigDecimal(i);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Money money = (Money) o;
        return new EqualsBuilder().append(amount, money.amount).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(amount).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("amount", amount).toString();
    }

    /**
     * Returns a new Money that is the sum of this and the given delta.
     *
     * @param delta the amount to add
     * @return the resulting Money
     */
    public Money add(Money delta) {
        return new Money(amount.add(delta.amount));
    }

    /**
     * Returns true if this amount is greater than or equal to the other.
     *
     * @param other the amount to compare against
     * @return true if this >= other
     */
    public boolean isGreaterThanOrEqual(Money other) {
        return amount.compareTo(other.amount) >= 0;
    }

    /**
     * Returns the plain string representation of the amount.
     *
     * @return the amount as a string
     */
    public String asString() {
        return amount.toPlainString();
    }

    /**
     * Returns a new Money that is this amount multiplied by the given factor.
     *
     * @param x the multiplier
     * @return the resulting Money
     */
    public Money multiply(int x) {
        return new Money(amount.multiply(new BigDecimal(x)));
    }
}
