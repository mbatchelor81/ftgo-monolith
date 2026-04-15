package com.ftgo.observability.logging;

import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import net.logstash.logback.composite.AbstractJsonProvider;

/**
 * Logstash-logback-encoder JSON provider that writes the log message with sensitive data masked.
 *
 * <p>Replaces the default {@code message} field with a masked version that redacts credit card
 * numbers, passwords, bearer tokens, and other sensitive patterns.
 *
 * <p>Register in {@code logback-spring.xml} inside a {@code LogstashEncoder} providers block:
 *
 * <pre>{@code
 * <provider class="com.ftgo.observability.logging.SensitiveDataMaskingJsonProvider"/>
 * }</pre>
 */
public class SensitiveDataMaskingJsonProvider
        extends AbstractJsonProvider<ch.qos.logback.classic.spi.ILoggingEvent> {

    public static final String FIELD_NAME = "message";

    @Override
    public void writeTo(JsonGenerator generator, ch.qos.logback.classic.spi.ILoggingEvent event)
            throws IOException {
        String message = event.getFormattedMessage();
        if (message != null) {
            generator.writeStringField(
                    FIELD_NAME, SensitiveDataMaskingConverter.maskSensitiveData(message));
        }
    }
}
