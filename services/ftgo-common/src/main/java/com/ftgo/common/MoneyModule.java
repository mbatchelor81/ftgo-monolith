package com.ftgo.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import java.io.IOException;

/**
 * Jackson module that registers custom serializer and deserializer for {@link Money}.
 */
public class MoneyModule extends SimpleModule {

    /**
     * Deserializes a JSON string value into a {@link Money} instance.
     */
    static class MoneyDeserializer extends StdScalarDeserializer<Money> {

        protected MoneyDeserializer() {
            super(Money.class);
        }

        @Override
        public Money deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {
            JsonToken token = jp.getCurrentToken();
            if (token == JsonToken.VALUE_STRING) {
                String str = jp.getText().trim();
                if (str.isEmpty()) {
                    return null;
                } else {
                    return new Money(str);
                }
            } else {
                return (Money) ctxt.handleUnexpectedToken(Money.class, jp);
            }
        }
    }

    /**
     * Serializes a {@link Money} instance as a JSON string.
     */
    static class MoneySerializer extends StdScalarSerializer<Money> {

        MoneySerializer() {
            super(Money.class);
        }

        @Override
        public void serialize(Money value, JsonGenerator jgen,
                SerializerProvider provider) throws IOException {
            jgen.writeString(value.asString());
        }
    }

    @Override
    public String getModuleName() {
        return "FtgoCommonModule";
    }

    /**
     * Creates a new MoneyModule and registers the Money serializer and deserializer.
     */
    public MoneyModule() {
        addDeserializer(Money.class, new MoneyDeserializer());
        addSerializer(Money.class, new MoneySerializer());
    }
}
