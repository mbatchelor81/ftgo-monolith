package com.ftgo.errorhandling.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ErrorCode}. */
@DisplayName("ErrorCode")
class ErrorCodeTest {

    @Test
    @DisplayName("every error code has a non-null code string")
    void allCodes_haveNonNullCode() {
        for (ErrorCode ec : ErrorCode.values()) {
            assertThat(ec.getCode()).as("ErrorCode.%s code", ec.name()).isNotBlank();
        }
    }

    @Test
    @DisplayName("every error code has a non-null default message")
    void allCodes_haveNonNullDefaultMessage() {
        for (ErrorCode ec : ErrorCode.values()) {
            assertThat(ec.getDefaultMessage())
                    .as("ErrorCode.%s defaultMessage", ec.name())
                    .isNotBlank();
        }
    }

    @Test
    @DisplayName("all code strings are unique")
    void allCodeStrings_areUnique() {
        Set<String> codes =
                Arrays.stream(ErrorCode.values())
                        .map(ErrorCode::getCode)
                        .collect(Collectors.toSet());
        assertThat(codes).hasSameSizeAs(ErrorCode.values());
    }

    @Test
    @DisplayName("code strings follow FTGO-NNN-NNN pattern")
    void codeStrings_followNamingConvention() {
        for (ErrorCode ec : ErrorCode.values()) {
            assertThat(ec.getCode())
                    .as("ErrorCode.%s code format", ec.name())
                    .matches("FTGO-\\d{3}-\\d{3}");
        }
    }
}
