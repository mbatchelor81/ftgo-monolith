package com.ftgo.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Money} value object.
 */
class MoneyTest {

    private final int m1Amount = 10;
    private final int m2Amount = 15;

    private Money m1 = new Money(m1Amount);
    private Money m2 = new Money(m2Amount);

    @Test
    void shouldReturnAsString() {
        assertEquals(Integer.toString(m1Amount), new Money(m1Amount).asString());
    }

    @Test
    void shouldCompare() {
        assertTrue(m2.isGreaterThanOrEqual(m2));
        assertTrue(m2.isGreaterThanOrEqual(m1));
        assertFalse(m1.isGreaterThanOrEqual(m2));
    }

    @Test
    void shouldAdd() {
        assertEquals(new Money(m1Amount + m2Amount), m1.add(m2));
    }

    @Test
    void shouldMultiply() {
        int multiplier = 12;
        assertEquals(new Money(m2Amount * multiplier), m2.multiply(multiplier));
    }
}
