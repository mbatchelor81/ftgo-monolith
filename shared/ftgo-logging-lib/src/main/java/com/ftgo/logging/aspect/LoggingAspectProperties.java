package com.ftgo.logging.aspect;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for the {@link LoggingAspect}.
 *
 * <p>Configurable via {@code application.yml}:
 * <pre>{@code
 * ftgo:
 *   logging:
 *     aspect:
 *       enabled: true
 *       base-packages:
 *         - com.ftgo.orderservice.domain
 *       log-level: DEBUG
 *       include-args: true
 *       include-result: false
 *       slow-execution-threshold-ms: 500
 * }</pre>
 */
@ConfigurationProperties(prefix = "ftgo.logging.aspect")
public class LoggingAspectProperties {

    /** Whether the logging aspect is enabled. Default: {@code true}. */
    private boolean enabled = true;

    /** Packages to instrument. Default: all {@code com.ftgo} packages. */
    private List<String> basePackages = List.of();

    /** Log level for entry/exit messages. Default: {@code DEBUG}. */
    private String logLevel = "DEBUG";

    /** Whether to include method arguments in entry logs. Default: {@code true}. */
    private boolean includeArgs = true;

    /** Whether to include return values in exit logs. Default: {@code false}. */
    private boolean includeResult = false;

    /** Threshold in milliseconds above which a WARN is logged. Default: {@code 500}. */
    private long slowExecutionThresholdMs = 500;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(List<String> basePackages) {
        this.basePackages = basePackages;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public boolean isIncludeArgs() {
        return includeArgs;
    }

    public void setIncludeArgs(boolean includeArgs) {
        this.includeArgs = includeArgs;
    }

    public boolean isIncludeResult() {
        return includeResult;
    }

    public void setIncludeResult(boolean includeResult) {
        this.includeResult = includeResult;
    }

    public long getSlowExecutionThresholdMs() {
        return slowExecutionThresholdMs;
    }

    public void setSlowExecutionThresholdMs(long slowExecutionThresholdMs) {
        this.slowExecutionThresholdMs = slowExecutionThresholdMs;
    }
}
