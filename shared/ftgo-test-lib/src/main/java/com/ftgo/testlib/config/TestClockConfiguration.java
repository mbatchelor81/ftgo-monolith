package com.ftgo.testlib.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Provides a fixed {@link Clock} bean for deterministic time-dependent tests.
 *
 * <pre>{@code
 * @SpringBootTest
 * @Import(TestClockConfiguration.class)
 * class TimeBasedTest {
 *     @Autowired
 *     private Clock clock; // Fixed at 2024-01-15T10:00:00Z
 * }
 * }</pre>
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestClockConfiguration {

    public static final Instant FIXED_INSTANT = Instant.parse("2024-01-15T10:00:00Z");
    public static final ZoneId ZONE = ZoneId.of("UTC");

    @Bean
    public Clock testClock() {
        return Clock.fixed(FIXED_INSTANT, ZONE);
    }
}
