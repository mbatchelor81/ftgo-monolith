package net.chrisrichardson.ftgo.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataMaskingConverterTest {

    @Test
    void masksCreditCardNumbers() {
        String input = "Card number 4111-1111-1111-1111 was charged";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("1111-1111");
        assertThat(result).contains("4111-****-****-1111");
    }

    @Test
    void masksCreditCardNumbersWithoutSeparators() {
        String input = "Card 4111111111111111 charged";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("11111111");
        assertThat(result).contains("4111-****-****-1111");
    }

    @Test
    void masksPasswordInKeyValue() {
        String input = "password=secret123 submitted";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("secret123");
        assertThat(result).contains("password=****");
    }

    @Test
    void masksPasswordInJson() {
        String input = "{\"password\":\"myP@ss\", \"user\":\"bob\"}";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("myP@ss");
        assertThat(result).contains("password=****");
    }

    @Test
    void masksBearerToken() {
        String input = "Authorization: Bearer eyJhbGciOi.payload.signature";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("eyJhbGciOi");
        assertThat(result).contains("Bearer ****");
    }

    @Test
    void masksApiKey() {
        String input = "api_key=abc123secret submitted";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("abc123secret");
        assertThat(result).contains("api_key=****");
    }

    @Test
    void masksTokenInJson() {
        String input = "{\"token\":\"abc123\"}";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("abc123");
        assertThat(result).contains("token=****");
    }

    @Test
    void masksSsn() {
        String input = "SSN: 123-45-6789";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("123-45");
        assertThat(result).contains("***-**-6789");
    }

    @Test
    void returnsNullForNull() {
        assertThat(SensitiveDataMaskingConverter.maskSensitiveData(null)).isNull();
    }

    @Test
    void returnsEmptyForEmpty() {
        assertThat(SensitiveDataMaskingConverter.maskSensitiveData("")).isEmpty();
    }

    @Test
    void doesNotMaskNonSensitiveData() {
        String input = "Order 12345 created for user alice";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).isEqualTo(input);
    }
}
