package net.chrisrichardson.ftgo.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.FieldNamesAware;
import net.logstash.logback.composite.JsonWritingUtils;
import net.logstash.logback.fieldnames.LogstashFieldNames;

import java.io.IOException;

/**
 * JSON provider that writes a masked version of the log message.
 *
 * Replaces the default {@code MessageJsonProvider} in
 * {@code LoggingEventCompositeJsonEncoder} to ensure sensitive data
 * is masked in structured JSON output (docker/k8s profiles).
 *
 * Uses the same masking logic as {@link SensitiveDataMaskingConverter}.
 */
public class SensitiveDataMaskingJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent>
        implements FieldNamesAware<LogstashFieldNames> {

    public static final String FIELD_MESSAGE = "message";

    public SensitiveDataMaskingJsonProvider() {
        setFieldName(FIELD_MESSAGE);
    }

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        String masked = SensitiveDataMaskingConverter.maskSensitiveData(event.getFormattedMessage());
        JsonWritingUtils.writeStringField(generator, getFieldName(), masked);
    }

    @Override
    public void setFieldNames(LogstashFieldNames fieldNames) {
        setFieldName(fieldNames.getMessage());
    }
}
