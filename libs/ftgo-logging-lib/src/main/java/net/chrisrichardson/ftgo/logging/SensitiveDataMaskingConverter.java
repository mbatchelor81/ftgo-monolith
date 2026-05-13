package net.chrisrichardson.ftgo.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

/**
 * Logback converter that masks sensitive data in log messages.
 *
 * Masks credit card numbers, passwords, tokens, authorization headers,
 * and SSN patterns before they reach appenders.
 *
 * Register in logback-spring.xml:
 * {@code <conversionRule conversionWord="maskedMsg"
 *     converterClass="net.chrisrichardson.ftgo.logging.SensitiveDataMaskingConverter"/>}
 */
public class SensitiveDataMaskingConverter extends ClassicConverter {

    private static final String MASK = "****";

    private static final Pattern[] PATTERNS = {
            // Credit card numbers (13-19 digits, with optional separators)
            Pattern.compile("\\b(\\d{4})[- ]?(\\d{4})[- ]?(\\d{4})[- ]?(\\d{1,7})\\b"),
            // Bearer tokens (must run before generic key-value patterns)
            Pattern.compile("(?i)(Bearer)\\s+([A-Za-z0-9_\\-\\.]+)"),
            // Sensitive key-value pairs — handles JSON ("key":"value") and plain (key=value) formats
            Pattern.compile("(?i)\"?(password|passwd|pwd|api[_-]?key|apikey|api[_-]?secret|token|secret|credential)\"?\\s*[=:]\\s*\"?([^\",\\s}]+)\"?"),
            // SSN pattern
            Pattern.compile("\\b(\\d{3})-?(\\d{2})-?(\\d{4})\\b"),
    };

    private static final String[] REPLACEMENTS = {
            "$1-" + MASK + "-" + MASK + "-$4",
            "$1 " + MASK,
            "$1=" + MASK,
            "***-**-$3",
    };

    @Override
    public String convert(ILoggingEvent event) {
        return maskSensitiveData(event.getFormattedMessage());
    }

    static String maskSensitiveData(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        String masked = message;
        for (int i = 0; i < PATTERNS.length; i++) {
            masked = PATTERNS[i].matcher(masked).replaceAll(REPLACEMENTS[i]);
        }
        return masked;
    }
}
