package com.ftgo.common;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link Money} JSON serialization and deserialization.
 */
class MoneySerializationTest {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void initialize() {
        objectMapper.registerModule(new MoneyModule());
    }

    /**
     * Container class for testing Money serialization within a wrapper object.
     */
    public static class MoneyContainer {
        private Money price;

        @Override
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

        public Money getPrice() {
            return price;
        }

        public void setPrice(Money price) {
            this.price = price;
        }

        public MoneyContainer() {
        }

        public MoneyContainer(Money price) {
            this.price = price;
        }
    }

    @Test
    void shouldSer() throws IOException {
        Money price = new Money("12.34");
        MoneyContainer mc = new MoneyContainer(price);
        assertEquals("{\"price\":\"12.34\"}", objectMapper.writeValueAsString(mc));
    }

    @Test
    void shouldDe() throws IOException {
        Money price = new Money("12.34");
        MoneyContainer mc = new MoneyContainer(price);
        assertEquals(mc, objectMapper.readValue("{\"price\":\"12.34\"}", MoneyContainer.class));
    }

    @Test
    void shouldFailToDe() {
        JsonMappingException jsonMappingException = assertThrows(
                JsonMappingException.class,
                () -> objectMapper.readValue(
                        "{\"price\": { \"amount\" : \"12.34\"} }", MoneyContainer.class)
        );
        assertNotNull(jsonMappingException);
    }
}
