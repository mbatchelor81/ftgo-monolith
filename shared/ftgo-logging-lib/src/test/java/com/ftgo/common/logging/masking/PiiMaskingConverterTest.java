package com.ftgo.common.logging.masking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PiiMaskingConverterTest {

    @Test
    void masksCreditCardNumbers() {
        String input = "Payment with card 4111-1111-1111-1234 processed";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("****-****-****-1234");
        assertThat(result).doesNotContain("4111");
    }

    @Test
    void masksCreditCardWithoutDashes() {
        String input = "Card number 4111111111111234";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("****-****-****-1234");
    }

    @Test
    void masksEmailAddresses() {
        String input = "User email: john.doe@example.com";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("j***@example.com");
        assertThat(result).doesNotContain("john.doe");
    }

    @Test
    void masksBearerTokens() {
        String input = "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.abc123";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("Bearer [MASKED]");
        assertThat(result).doesNotContain("eyJhbGci");
    }

    @Test
    void masksPasswordInJson() {
        String input = "{\"username\":\"admin\",\"password\":\"secret123\"}";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("\"password\":\"[MASKED]\"");
        assertThat(result).doesNotContain("secret123");
    }

    @Test
    void masksApiKeyInJson() {
        String input = "{\"apiKey\":\"sk-12345abcdef\"}";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("\"apiKey\":\"[MASKED]\"");
        assertThat(result).doesNotContain("sk-12345");
    }

    @Test
    void doesNotModifyNonSensitiveMessages() {
        String input = "Order 12345 created for consumer 42";
        String result = PiiMaskingConverter.maskSensitiveData(input);
        assertThat(result).isEqualTo(input);
    }

    @Test
    void handlesNullAndEmptyStrings() {
        assertThat(PiiMaskingConverter.maskSensitiveData(null)).isNull();
        assertThat(PiiMaskingConverter.maskSensitiveData("")).isEmpty();
    }
}
