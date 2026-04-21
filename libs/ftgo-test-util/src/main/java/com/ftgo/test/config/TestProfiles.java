package com.ftgo.test.config;

/**
 * Canonical Spring profile names used by FTGO test suites.
 *
 * <p>Kept as constants so tests reference the string once, per the
 * testing strategy (see docs/testing-strategy.md). Services that need a
 * bespoke profile should still put it here so the conventions stay
 * consistent across bounded contexts.
 */
public final class TestProfiles {

    /**
     * Default profile for unit tests. No database, no Spring context,
     * no network. Activates in-memory stubs where a bean is unavoidable.
     */
    public static final String UNIT = "test-unit";

    /**
     * Profile for integration tests that spin up a Spring context and a
     * real MySQL via Testcontainers (see {@link com.ftgo.test.containers.FtgoMySqlContainer}).
     */
    public static final String INTEGRATION = "test-integration";

    /**
     * Profile for contract-producer tests. Activates the stub data the
     * contract verifier uses to generate responses.
     */
    public static final String CONTRACT = "test-contract";

    /**
     * Profile for API tests — full Spring Boot context, random port,
     * Rest-Assured driving HTTP traffic end-to-end against the service.
     */
    public static final String API = "test-api";

    private TestProfiles() {
    }
}
