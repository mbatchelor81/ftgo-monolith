package com.ftgo.observability.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SensitiveDataMaskingConverterTest {

    @Test
    void maskSensitiveData_withCreditCardNumber_masksMiddleDigits() {
        String input = "Payment with card 4111111111111111 processed";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);

        assertThat(result).contains("4111-****-1111");
        assertThat(result).doesNotContain("4111111111111111");
    }

    @Test
    void maskSensitiveData_withDashedCreditCard_masksMiddleDigits() {
        String input = "Card: 4111-1111-1111-1111";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);

        assertThat(result).contains("4111-****-1111");
        assertThat(result).doesNotContain("4111-1111-1111-1111");
    }

    @Test
    void maskSensitiveData_withPasswordKeyValue_masksValue() {
        String input = "User login with password=secretPass123";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);

        assertThat(result).doesNotContain("secretPass123");
        assertThat(result).contains("password=****");
    }

    @Test
    void maskSensitiveData_withPasswordJsonStyle_masksValue() {
        String input = "Config: {\"password\": \"mySecret\"}";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);

        assertThat(result).doesNotContain("mySecret");
    }

    @Test
    void maskSensitiveData_withBearerToken_masksToken() {
        String input = "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.payload.signature";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);

        assertThat(result).doesNotContain("eyJhbGciOiJIUzI1NiJ9");
        assertThat(result).contains("Bearer ****");
    }

    @Test
    void maskSensitiveData_withBasicAuth_masksCredentials() {
        String input = "Authorization: Basic dXNlcjpwYXNz";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);

        assertThat(result).doesNotContain("dXNlcjpwYXNz");
        assertThat(result).contains("Basic ****");
    }

    @Test
    void maskSensitiveData_withSsn_masksMiddleDigits() {
        String input = "SSN: 123-45-6789";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);

        assertThat(result).doesNotContain("123-45-6789");
        assertThat(result).contains("****-****-6789");
    }

    @Test
    void maskSensitiveData_withNonSensitiveData_returnsUnchanged() {
        String input = "Order 42 created for consumer 7";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    void maskSensitiveData_withSecretKeyValue_masksValue() {
        String input = "secret=topSecret123";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);

        assertThat(result).doesNotContain("topSecret123");
        assertThat(result).contains("secret=****");
    }

    @Test
    void maskSensitiveData_withCredentialKeyValue_masksValue() {
        String input = "credential: myApiKey";
        String result = SensitiveDataMaskingConverter.maskSensitiveData(input);

        assertThat(result).doesNotContain("myApiKey");
    }
}
