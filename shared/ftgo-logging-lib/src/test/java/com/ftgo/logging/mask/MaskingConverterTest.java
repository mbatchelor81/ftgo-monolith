package com.ftgo.logging.mask;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingConverterTest {

    @Test
    void maskSensitiveData_creditCardWithDashes_masksAllButLastFour() {
        String input = "Card number: 4111-1111-1111-1234";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("************1234");
        assertThat(result).doesNotContain("4111");
    }

    @Test
    void maskSensitiveData_creditCardWithSpaces_masksAllButLastFour() {
        String input = "Card: 4111 1111 1111 5678";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("************5678");
    }

    @Test
    void maskSensitiveData_creditCardContiguous_masksAllButLastFour() {
        String input = "cc=4111111111111111";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("************1111");
        assertThat(result).doesNotContain("41111111");
    }

    @Test
    void maskSensitiveData_creditCard13Digits_masksAllButLastFour() {
        String input = "Card: 4222222222225";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("*********2225");
        assertThat(result).doesNotContain("4222");
    }

    @Test
    void maskSensitiveData_creditCard15Digits_masksAllButLastFour() {
        String input = "Amex: 378282246310005";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("***********0005");
        assertThat(result).doesNotContain("3782");
    }

    @Test
    void maskSensitiveData_creditCard17Digits_masksAllButLastFour() {
        String input = "Maestro: 41111111111111113";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("*************1113");
        assertThat(result).doesNotContain("4111");
    }

    @Test
    void maskSensitiveData_creditCard19Digits_masksAllButLastFour() {
        String input = "UnionPay: 4111111111111111309";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("***************1309");
        assertThat(result).doesNotContain("4111");
    }

    @Test
    void maskSensitiveData_passwordInJson_masksValue() {
        String input = "{\"password\":\"s3cr3t!\"}";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("\"password\":\"******\"");
        assertThat(result).doesNotContain("s3cr3t!");
    }

    @Test
    void maskSensitiveData_secretInJson_masksValue() {
        String input = "{\"secret\":\"myTopSecret\"}";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("\"secret\":\"******\"");
        assertThat(result).doesNotContain("myTopSecret");
    }

    @Test
    void maskSensitiveData_apiKeyInJson_masksValue() {
        String input = "{\"apiKey\":\"abc-123-xyz\"}";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("\"apiKey\":\"******\"");
        assertThat(result).doesNotContain("abc-123-xyz");
    }

    @Test
    void maskSensitiveData_tokenInJson_masksValue() {
        String input = "{\"token\":\"eyJhbGciOi\"}";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("\"token\":\"******\"");
    }

    @Test
    void maskSensitiveData_passwordInKeyValue_masksValue() {
        String input = "password=myPassword123";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("password=******");
        assertThat(result).doesNotContain("myPassword123");
    }

    @Test
    void maskSensitiveData_bearerToken_redacts() {
        String input = "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.abc.def";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("Bearer [REDACTED]");
        assertThat(result).doesNotContain("eyJhbG");
    }

    @Test
    void maskSensitiveData_authorizationHeader_redacts() {
        String input = "Authorization: Basic dXNlcjpwYXNz";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).contains("[REDACTED]");
        assertThat(result).doesNotContain("dXNlcjpwYXNz");
    }

    @Test
    void maskSensitiveData_normalMessage_unchanged() {
        String input = "Order 12345 created for consumer 42";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).isEqualTo(input);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void maskSensitiveData_nullOrEmpty_returnsAsIs(String input) {
        assertThat(MaskingConverter.maskSensitiveData(input)).isEqualTo(input);
    }

    @Test
    void maskSensitiveData_multiplePatterns_masksAll() {
        String input = "User password=secret123 used card 4111-1111-1111-9999 with Bearer eyJtoken";
        String result = MaskingConverter.maskSensitiveData(input);
        assertThat(result).doesNotContain("secret123");
        assertThat(result).doesNotContain("4111");
        assertThat(result).doesNotContain("eyJtoken");
        assertThat(result).contains("************9999");
        assertThat(result).contains("password=******");
        assertThat(result).contains("Bearer [REDACTED]");
    }
}
