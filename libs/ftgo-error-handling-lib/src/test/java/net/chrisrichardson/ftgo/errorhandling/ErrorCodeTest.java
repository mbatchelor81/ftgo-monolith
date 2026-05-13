package net.chrisrichardson.ftgo.errorhandling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeTest {

    @ParameterizedTest
    @EnumSource(ErrorCode.class)
    void allCodes_haveValidHttpStatus(ErrorCode code) {
        assertThat(code.getHttpStatus()).isBetween(400, 599);
    }

    @ParameterizedTest
    @EnumSource(ErrorCode.class)
    void allCodes_haveNonBlankDefaultMessage(ErrorCode code) {
        assertThat(code.getDefaultMessage()).isNotBlank();
    }

    @Test
    void validationError_mapsTo400() {
        assertThat(ErrorCode.VALIDATION_ERROR.getHttpStatus()).isEqualTo(400);
    }

    @Test
    void resourceNotFound_mapsTo404() {
        assertThat(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus()).isEqualTo(404);
    }

    @Test
    void stateConflict_mapsTo409() {
        assertThat(ErrorCode.STATE_CONFLICT.getHttpStatus()).isEqualTo(409);
    }

    @Test
    void orderMinimumNotMet_mapsTo422() {
        assertThat(ErrorCode.ORDER_MINIMUM_NOT_MET.getHttpStatus()).isEqualTo(422);
    }

    @Test
    void internalError_mapsTo500() {
        assertThat(ErrorCode.INTERNAL_ERROR.getHttpStatus()).isEqualTo(500);
    }

    @Test
    void notImplemented_mapsTo501() {
        assertThat(ErrorCode.NOT_IMPLEMENTED.getHttpStatus()).isEqualTo(501);
    }

    @Test
    void serviceUnavailable_mapsTo503() {
        assertThat(ErrorCode.SERVICE_UNAVAILABLE.getHttpStatus()).isEqualTo(503);
    }
}
