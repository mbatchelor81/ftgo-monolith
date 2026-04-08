package com.ftgo.logging.mask;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom Logback converter that masks sensitive data in log messages.
 *
 * <p>Automatically redacts:
 * <ul>
 *   <li>Credit card numbers (13-19 digit sequences, with or without dashes/spaces)</li>
 *   <li>Password values in key-value patterns (JSON and query string formats)</li>
 *   <li>Bearer tokens</li>
 *   <li>Authorization header values</li>
 * </ul>
 *
 * <p>Register in logback-spring.xml:
 * <pre>{@code
 *   <conversionRule conversionWord="maskedMsg"
 *       converterClass="com.ftgo.logging.mask.MaskingConverter"/>
 * }</pre>
 *
 * <p>Then use {@code %maskedMsg} instead of {@code %msg} in pattern layouts.
 */
public class MaskingConverter extends ClassicConverter {

    // Credit card: 13-19 digits optionally separated by dashes or spaces
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
            "\\b([0-9]{4}[- ]?){2,4}[0-9]{1,4}\\b"
    );

    // Passwords in JSON: "password":"value" or "secret":"value" etc.
    private static final Pattern PASSWORD_JSON_PATTERN = Pattern.compile(
            "(\"(?:password|passwd|pwd|secret|credential|token|apiKey|api_key|accessToken|access_token|refreshToken|refresh_token)\"\\s*:\\s*\")([^\"]*)(\")",
            Pattern.CASE_INSENSITIVE
    );

    // Passwords in key=value format: password=value
    private static final Pattern PASSWORD_KV_PATTERN = Pattern.compile(
            "((?:password|passwd|pwd|secret|credential|token|apiKey|api_key|accessToken|access_token|refreshToken|refresh_token)\\s*=\\s*)([^\\s,;&]+)",
            Pattern.CASE_INSENSITIVE
    );

    // Bearer tokens
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile(
            "(Bearer\\s+)[A-Za-z0-9\\-._~+/]+=*",
            Pattern.CASE_INSENSITIVE
    );

    // Authorization header values (captures scheme + credential, e.g. "Basic dXNlcjpwYXNz")
    private static final Pattern AUTH_HEADER_PATTERN = Pattern.compile(
            "(Authorization\\s*[:=]\\s*)(\\S+(?:\\s+\\S+)?)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String convert(ILoggingEvent event) {
        return maskSensitiveData(event.getFormattedMessage());
    }

    /**
     * Masks sensitive data patterns in the given message.
     *
     * @param message the log message to mask
     * @return the masked message
     */
    static String maskSensitiveData(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String masked = message;

        // Mask passwords in JSON format first (more specific pattern)
        masked = PASSWORD_JSON_PATTERN.matcher(masked).replaceAll("$1******$3");

        // Mask passwords in key=value format
        masked = PASSWORD_KV_PATTERN.matcher(masked).replaceAll("$1******");

        // Mask Bearer tokens
        masked = BEARER_TOKEN_PATTERN.matcher(masked).replaceAll("$1[REDACTED]");

        // Mask Authorization header values
        masked = AUTH_HEADER_PATTERN.matcher(masked).replaceAll("$1[REDACTED]");

        // Mask credit card numbers — preserve last 4 digits
        Matcher ccMatcher = CREDIT_CARD_PATTERN.matcher(masked);
        StringBuilder sb = new StringBuilder();
        while (ccMatcher.find()) {
            String match = ccMatcher.group();
            String digitsOnly = match.replaceAll("[- ]", "");
            if (digitsOnly.length() >= 13) {
                String lastFour = digitsOnly.substring(digitsOnly.length() - 4);
                String replacement = "*".repeat(digitsOnly.length() - 4) + lastFour;
                ccMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
        }
        ccMatcher.appendTail(sb);
        masked = sb.toString();

        return masked;
    }
}
