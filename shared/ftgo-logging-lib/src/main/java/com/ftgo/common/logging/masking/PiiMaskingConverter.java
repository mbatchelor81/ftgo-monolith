package com.ftgo.common.logging.masking;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

/**
 * Logback converter that masks sensitive data (PII, credentials, tokens)
 * in log messages before they are written to any appender.
 *
 * Usage in logback.xml:
 * <pre>
 * {@code
 * <conversionRule conversionWord="maskedMessage"
 *     converterClass="com.ftgo.common.logging.masking.PiiMaskingConverter" />
 * <pattern>%d{ISO8601} %level %logger - %maskedMessage%n</pattern>
 * }
 * </pre>
 */
public class PiiMaskingConverter extends ClassicConverter {

    // Credit card: 13-19 digits with optional dashes/spaces, mask all but last 4
    private static final Pattern CREDIT_CARD_PATTERN =
            Pattern.compile("\\b(\\d{4})[- ]?(\\d{4})[- ]?(\\d{4})[- ]?(\\d{1,7})\\b");

    // Email: mask local part except first character
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\b([a-zA-Z0-9])[a-zA-Z0-9._%+-]*@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})\\b");

    // Bearer token: mask the token value
    private static final Pattern BEARER_TOKEN_PATTERN =
            Pattern.compile("Bearer\\s+[A-Za-z0-9\\-._~+/]+=*");

    // Password in JSON: "password":"value" or "password": "value"
    private static final Pattern PASSWORD_JSON_PATTERN =
            Pattern.compile("(\"(?:password|passwd|pwd|secret|token|apiKey|api_key)\"\\s*:\\s*\")([^\"]+)(\")",
                    Pattern.CASE_INSENSITIVE);

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null || message.isEmpty()) {
            return message;
        }
        return maskSensitiveData(message);
    }

    static String maskSensitiveData(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        String result = message;

        // Mask credit card numbers — preserve last 4 digits
        result = CREDIT_CARD_PATTERN.matcher(result)
                .replaceAll("****-****-****-$4");

        // Mask email addresses — preserve first char and domain
        result = EMAIL_PATTERN.matcher(result)
                .replaceAll("$1***@$2");

        // Mask bearer tokens
        result = BEARER_TOKEN_PATTERN.matcher(result)
                .replaceAll("Bearer [MASKED]");

        // Mask password/secret JSON fields
        result = PASSWORD_JSON_PATTERN.matcher(result)
                .replaceAll("$1[MASKED]$3");

        return result;
    }
}
