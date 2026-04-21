package com.ftgo.test.config;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Deterministic {@link Clock} factories for tests that depend on "now".
 *
 * <p>Domain logic that embeds {@code Instant.now()} / {@code LocalDateTime.now()}
 * is brittle in tests. Services should inject a {@link Clock} bean and
 * tests should build one through this class so assertions are
 * reproducible.
 *
 * <pre>{@code
 * Clock clock = TestClock.fixedAt("2025-01-02T03:04:05Z");
 * OrderService service = new OrderService(orderRepository, clock);
 * }</pre>
 */
public final class TestClock {

    /** The canonical "frozen" instant used when no specific time matters. */
    public static final Instant FROZEN_INSTANT = Instant.parse("2025-01-01T00:00:00Z");

    private TestClock() {
    }

    /** A clock fixed at {@link #FROZEN_INSTANT} in UTC. */
    public static Clock frozen() {
        return Clock.fixed(FROZEN_INSTANT, ZoneOffset.UTC);
    }

    /** A clock fixed at the given ISO-8601 instant in UTC. */
    public static Clock fixedAt(String isoInstant) {
        return Clock.fixed(Instant.parse(isoInstant), ZoneOffset.UTC);
    }
}
