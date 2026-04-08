package com.ftgo.logging.aspect;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingAspectPropertiesTest {

    @Test
    void defaults_areCorrect() {
        var props = new LoggingAspectProperties();

        assertThat(props.isEnabled()).isTrue();
        assertThat(props.getBasePackages()).isEmpty();
        assertThat(props.getLogLevel()).isEqualTo("DEBUG");
        assertThat(props.isIncludeArgs()).isTrue();
        assertThat(props.isIncludeResult()).isFalse();
        assertThat(props.getSlowExecutionThresholdMs()).isEqualTo(500);
    }

    @Test
    void setters_overrideDefaults() {
        var props = new LoggingAspectProperties();
        props.setEnabled(false);
        props.setBasePackages(List.of("com.ftgo.orderservice"));
        props.setLogLevel("INFO");
        props.setIncludeArgs(false);
        props.setIncludeResult(true);
        props.setSlowExecutionThresholdMs(1000);

        assertThat(props.isEnabled()).isFalse();
        assertThat(props.getBasePackages()).containsExactly("com.ftgo.orderservice");
        assertThat(props.getLogLevel()).isEqualTo("INFO");
        assertThat(props.isIncludeArgs()).isFalse();
        assertThat(props.isIncludeResult()).isTrue();
        assertThat(props.getSlowExecutionThresholdMs()).isEqualTo(1000);
    }
}
