package net.chrisrichardson.ftgo.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataMaskingConverterTest {

    @Test
    void maskSensitiveData_creditCardWithHyphens_masksMiddleDigits() {
        String input = "Card number 4111-1111-1111-1111 was charged";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("1111-1111");
        assertThat(result).contains("4111-****-****-1111");
    }

    @Test
    void maskSensitiveData_creditCardWithoutSeparators_masksMiddleDigits() {
        String input = "Card 4111111111111111 charged";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("11111111");
        assertThat(result).contains("4111-****-****-1111");
    }

    @Test
    void maskSensitiveData_passwordKeyValue_masksValue() {
        String input = "password=secret123 submitted";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("secret123");
        assertThat(result).contains("password=****");
    }

    @Test
    void maskSensitiveData_passwordInJson_masksValue() {
        String input = "{\"password\":\"myP@ss\", \"user\":\"bob\"}";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("myP@ss");
        assertThat(result).contains("password=****");
    }

    @Test
    void maskSensitiveData_bearerToken_masksTokenValue() {
        String input = "Authorization: Bearer eyJhbGciOi.payload.signature";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("eyJhbGciOi");
        assertThat(result).contains("Bearer ****");
    }

    @Test
    void maskSensitiveData_basicAuth_masksCredentials() {
        String input = "Authorization: Basic dXNlcjpwYXNz";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("dXNlcjpwYXNz");
        assertThat(result).contains("Basic ****");
    }

    @Test
    void maskSensitiveData_apiKey_masksValue() {
        String input = "api_key=abc123secret submitted";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("abc123secret");
        assertThat(result).contains("api_key=****");
    }

    @Test
    void maskSensitiveData_tokenInJson_masksValue() {
        String input = "{\"token\":\"abc123\"}";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("abc123");
        assertThat(result).contains("token=****");
    }

    @Test
    void maskSensitiveData_ssnWithHyphens_masksFirstFiveDigits() {
        String input = "SSN: 123-45-6789";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("123-45");
        assertThat(result).contains("***-**-6789");
    }

    @Test
    void maskSensitiveData_nineDigitNumber_doesNotMask() {
        String input = "Order 123456789 created";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).isEqualTo(input);
    }

    @Test
    void maskSensitiveData_nullInput_returnsNull() {
        assertThat(SensitiveDataMaskingConverter.maskSensitiveData(null)).isNull();
    }

    @Test
    void maskSensitiveData_emptyInput_returnsEmpty() {
        assertThat(SensitiveDataMaskingConverter.maskSensitiveData("")).isEmpty();
    }

    @Test
    void maskSensitiveData_nonSensitiveText_returnsUnchanged() {
        String input = "Order 12345 created for user alice";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);
        assertThat(result).isEqualTo(input);
    }
}
