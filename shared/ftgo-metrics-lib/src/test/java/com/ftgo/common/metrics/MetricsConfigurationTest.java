package com.ftgo.common.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.application.name=test-service",
        "ftgo.metrics.environment=test"
})
class MetricsConfigurationTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void commonTagsAreApplied() {
        // When a counter is registered, it should have the common tags
        meterRegistry.counter("test.counter").increment();

        assertThat(meterRegistry.find("test.counter").counter()).isNotNull();
        assertThat(meterRegistry.find("test.counter").counter().getId().getTag("application"))
                .isEqualTo("test-service");
        assertThat(meterRegistry.find("test.counter").counter().getId().getTag("environment"))
                .isEqualTo("test");
    }

    @SpringBootApplication
    static class TestApp {
    }
}
