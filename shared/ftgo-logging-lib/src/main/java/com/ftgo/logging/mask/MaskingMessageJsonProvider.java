package com.ftgo.logging.mask;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.JsonWritingUtils;

import java.io.IOException;

/**
 * Custom JSON provider for Logstash encoder that masks sensitive data in the
 * log message before writing it to the JSON output.
 *
 * <p>This provider replaces the default message field in JSON-structured logs,
 * ensuring that credit card numbers, passwords, tokens, and other PII are
 * redacted before the log entry is serialized.
 *
 * <p>Register in logstash encoder configuration:
 * <pre>{@code
 * <encoder class="net.logstash.logback.encoder.LogstashEncoder">
 *     <provider class="com.ftgo.logging.mask.MaskingMessageJsonProvider"/>
 * </encoder>
 * }</pre>
 */
public class MaskingMessageJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent> {

    public static final String FIELD_MESSAGE = "message";

    public MaskingMessageJsonProvider() {
        setFieldName(FIELD_MESSAGE);
    }

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        String maskedMessage = MaskingConverter.maskSensitiveData(event.getFormattedMessage());
        JsonWritingUtils.writeStringField(generator, getFieldName(), maskedMessage);
    }
}
