package net.chrisrichardson.ftgo.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Logback pattern-layout converter that masks sensitive values (credit
 * cards, passwords, tokens, API keys) inside log messages before they
 * reach any appender.
 *
 * <p>Wired into {@code logback-ftgo.xml} via a {@code <conversionRule>}
 * declaration and referenced as {@code %maskedMsg}. Works for both the
 * human-readable and the JSON encoders.
 *
 * <p>The masking is deliberately conservative: we only strip the
 * <em>value</em> of well-known keys; any other application data flows
 * through unchanged. This keeps the cost low enough to leave the
 * converter on in production without hurting throughput.
 *
 * <p>Example:
 * <pre>
 *   log.info("Charging card 4111-1111-1111-1111 for user");
 *   // Renders as: Charging card **** **** **** 1111 for user
 *
 *   log.info("Received token=eyJhbGciOiJIUzI1NiJ9.payload.sig");
 *   // Renders as: Received token=***REDACTED***
 * </pre>
 */
public class SensitiveDataMaskingConverter extends MessageConverter {

    static final String REDACTED = "***REDACTED***";

    /**
     * Credit-card-like digit sequences (13–19 digits, optionally
     * separated by spaces or hyphens). Keeps the last four digits
     * visible for operational debugging (PCI-DSS allows this).
     */
    private static final Pattern CREDIT_CARD = Pattern.compile(
            "(?<![0-9])(?:\\d[ -]?){12,18}\\d(?![0-9])");

    /** {@code key=value} or {@code "key":"value"} matches for secret-bearing keys. */
    private static final List<Pattern> SECRET_KEY_VALUE_PATTERNS = List.of(
            // key=value, key: value, key="value"
            Pattern.compile("(?i)(password|passwd|pwd|secret|token|api[-_]?key|access[-_]?key|authorization|auth[-_]?token|bearer)"
                    + "\\s*[:=]\\s*\"?([^\\s\",}\\]]+)\"?"),
            // JSON: "key": "value"
            Pattern.compile("(?i)\"(password|passwd|pwd|secret|token|api[-_]?key|access[-_]?key|authorization|auth[-_]?token|bearer)\""
                    + "\\s*:\\s*\"([^\"]*)\"")
    );

    /**
     * Bare JWT tokens (three base64url segments separated by dots).
     * Matched outside of {@code key=value} so log lines that emit a raw
     * token without a label are still scrubbed.
     */
    private static final Pattern JWT = Pattern.compile(
            "\\beyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\b");

    /**
     * {@code Bearer <token>} sequences as they appear in an HTTP
     * Authorization header. We keep the {@code Bearer } prefix so the
     * log line still reads as an auth header, but strip the token.
     */
    private static final Pattern BEARER_TOKEN = Pattern.compile(
            "(?i)\\bBearer\\s+([A-Za-z0-9._~+/=-]+)");

    @Override
    public String convert(ILoggingEvent event) {
        String formatted = event.getFormattedMessage();
        return mask(formatted);
    }

    /**
     * Visible for testing. Applies every masking rule to {@code message}
     * and returns the redacted result.
     */
    public static String mask(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String masked = message;

        // Run BEARER_TOKEN before the key=value patterns so
        // "Authorization: Bearer <token>" is scrubbed fully — the
        // `authorization` key=value rule would otherwise consume only
        // the first whitespace-delimited word and leave the real
        // token on the tail end of the line.
        masked = BEARER_TOKEN.matcher(masked).replaceAll("Bearer " + REDACTED);

        for (Pattern pattern : SECRET_KEY_VALUE_PATTERNS) {
            masked = pattern.matcher(masked).replaceAll(matchResult -> {
                // Preserve the original key=... prefix and substitute the value.
                String match = matchResult.group();
                String value = matchResult.group(2);
                if (value == null || value.isEmpty()) {
                    return match;
                }
                return match.substring(0, match.length() - value.length() - trailingQuoteLength(match))
                        + REDACTED
                        + match.substring(match.length() - trailingQuoteLength(match));
            });
        }

        masked = JWT.matcher(masked).replaceAll(REDACTED);

        masked = CREDIT_CARD.matcher(masked).replaceAll(matchResult -> {
            String digitsOnly = matchResult.group().replaceAll("[^0-9]", "");
            if (digitsOnly.length() < 13 || digitsOnly.length() > 19) {
                return matchResult.group();
            }
            String last4 = digitsOnly.substring(digitsOnly.length() - 4);
            return "**** **** **** " + last4;
        });

        return masked;
    }

    /** Returns 1 if {@code match} ends in a closing quote, else 0. */
    private static int trailingQuoteLength(String match) {
        return match.endsWith("\"") ? 1 : 0;
    }
}
