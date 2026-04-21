package net.chrisrichardson.ftgo.logging;

import org.junit.jupiter.api.Test;

import static net.chrisrichardson.ftgo.logging.SensitiveDataMaskingConverter.mask;
import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataMaskingConverterTest {

    @Test
    void mask_nullAndEmpty_returnsInputUnchanged() {
        assertThat(mask(null)).isNull();
        assertThat(mask("")).isEmpty();
    }

    @Test
    void mask_messageWithoutSensitiveData_returnsInputUnchanged() {
        String input = "Order 4711 accepted by restaurant 42";
        assertThat(mask(input)).isEqualTo(input);
    }

    @Test
    void mask_creditCardNumber_leavesLastFourDigitsVisible() {
        assertThat(mask("Charging card 4111111111111111 for user"))
                .isEqualTo("Charging card **** **** **** 1111 for user");
    }

    @Test
    void mask_creditCardNumberWithHyphens_isMasked() {
        assertThat(mask("card 4111-1111-1111-1111 paid"))
                .contains("**** **** **** 1111")
                .doesNotContain("4111-1111-1111");
    }

    @Test
    void mask_creditCardNumberWithSpaces_isMasked() {
        assertThat(mask("card 4111 1111 1111 1111 paid"))
                .contains("**** **** **** 1111")
                .doesNotContain("4111 1111 1111");
    }

    @Test
    void mask_shortDigitSequence_isNotTreatedAsCreditCard() {
        String input = "Order 12345678 placed";
        assertThat(mask(input)).isEqualTo(input);
    }

    @Test
    void mask_passwordKeyValue_redactsValue() {
        assertThat(mask("Auth failed: password=hunter2 for user"))
                .contains("password=" + SensitiveDataMaskingConverter.REDACTED)
                .doesNotContain("hunter2");
    }

    @Test
    void mask_tokenKeyValue_redactsValue() {
        assertThat(mask("Received token=abc.def.ghi"))
                .contains("token=" + SensitiveDataMaskingConverter.REDACTED)
                .doesNotContain("abc.def.ghi");
    }

    @Test
    void mask_apiKeyKeyValue_redactsValue() {
        assertThat(mask("api-key=super-secret"))
                .contains("api-key=" + SensitiveDataMaskingConverter.REDACTED)
                .doesNotContain("super-secret");
    }

    @Test
    void mask_jsonPassword_redactsValue() {
        assertThat(mask("{\"username\":\"alice\",\"password\":\"hunter2\"}"))
                .contains(SensitiveDataMaskingConverter.REDACTED)
                .doesNotContain("hunter2")
                .contains("alice");
    }

    @Test
    void mask_bareJwt_isRedacted() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyLTEyMyJ9.abcdef";
        assertThat(mask("Received " + jwt + " from client"))
                .contains(SensitiveDataMaskingConverter.REDACTED)
                .doesNotContain(jwt);
    }

    @Test
    void mask_authorizationHeader_redactsValue() {
        assertThat(mask("Authorization: Bearer abc123xyz"))
                .contains(SensitiveDataMaskingConverter.REDACTED)
                .doesNotContain("abc123xyz");
    }
}
