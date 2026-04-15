package com.ftgo.observability.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.regex.Pattern;

/**
 * Logback converter that masks sensitive data in log messages.
 *
 * <p>Automatically redacts credit card numbers, passwords, bearer tokens, and basic-auth headers
 * that may accidentally appear in log output. This is a safety net — developers should still avoid
 * logging sensitive data in the first place.
 *
 * <p>Register in {@code logback-spring.xml} with:
 *
 * <pre>{@code
 * <conversionRule conversionWord="maskedMsg"
 *     converterClass="com.ftgo.observability.logging.SensitiveDataMaskingConverter"/>
 * }</pre>
 */
public class SensitiveDataMaskingConverter extends ClassicConverter {

    /** Matches 13-19 digit sequences that look like credit card numbers. */
    private static final Pattern CREDIT_CARD_PATTERN =
            Pattern.compile("\\b([0-9]{4})[- ]?[0-9]{4,}[- ]?[0-9]{4,}[- ]?([0-9]{4})\\b");

    /** Matches password fields in key=value or JSON formats. */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile(
                    "(?i)(password|passwd|pwd|secret|credential)"
                            + "[\"']?\\s*[=:]\\s*[\"']?([^\"',\\s}{]+)[\"']?");

    /** Matches Bearer tokens. */
    private static final Pattern BEARER_TOKEN_PATTERN =
            Pattern.compile("(?i)(Bearer)\\s+([A-Za-z0-9\\-._~+/]+=*)");

    /** Matches Basic auth headers. */
    private static final Pattern BASIC_AUTH_PATTERN =
            Pattern.compile("(?i)(Basic)\\s+([A-Za-z0-9+/]+=*)");

    /** Matches SSN-like patterns (XXX-XX-XXXX). */
    private static final Pattern SSN_PATTERN =
            Pattern.compile("\\b([0-9]{3})-([0-9]{2})-([0-9]{4})\\b");

    private static final String MASKED = "****";

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null) {
            return "";
        }
        return maskSensitiveData(message);
    }

    /**
     * Applies all masking patterns to the given message.
     *
     * @param message the raw log message
     * @return the message with sensitive data redacted
     */
    static String maskSensitiveData(String message) {
        String masked = message;
        masked = CREDIT_CARD_PATTERN.matcher(masked).replaceAll("$1-" + MASKED + "-$2");
        masked = PASSWORD_PATTERN.matcher(masked).replaceAll("$1" + "=" + MASKED);
        masked = BEARER_TOKEN_PATTERN.matcher(masked).replaceAll("$1 " + MASKED);
        masked = BASIC_AUTH_PATTERN.matcher(masked).replaceAll("$1 " + MASKED);
        masked = SSN_PATTERN.matcher(masked).replaceAll("$1-" + MASKED + "-$3");
        return masked;
    }
}
