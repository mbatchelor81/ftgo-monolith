package net.chrisrichardson.ftgo.errorhandling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeTest {

    @ParameterizedTest
    @EnumSource(ErrorCode.class)
    void getHttpStatus_allErrorCodes_returnsBetween400And599(ErrorCode code) {
        assertThat(code.getHttpStatus()).isBetween(400, 599);
    }

    @ParameterizedTest
    @EnumSource(ErrorCode.class)
    void getDefaultMessage_allErrorCodes_returnsNonBlank(ErrorCode code) {
        assertThat(code.getDefaultMessage()).isNotBlank();
    }

    @Test
    void getHttpStatus_validationError_returns400() {
        assertThat(ErrorCode.VALIDATION_ERROR.getHttpStatus()).isEqualTo(400);
    }

    @Test
    void getHttpStatus_resourceNotFound_returns404() {
        assertThat(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus()).isEqualTo(404);
    }

    @Test
    void getHttpStatus_stateConflict_returns409() {
        assertThat(ErrorCode.STATE_CONFLICT.getHttpStatus()).isEqualTo(409);
    }

    @Test
    void getHttpStatus_orderMinimumNotMet_returns422() {
        assertThat(ErrorCode.ORDER_MINIMUM_NOT_MET.getHttpStatus()).isEqualTo(422);
    }

    @Test
    void getHttpStatus_internalError_returns500() {
        assertThat(ErrorCode.INTERNAL_ERROR.getHttpStatus()).isEqualTo(500);
    }

    @Test
    void getHttpStatus_notImplemented_returns501() {
        assertThat(ErrorCode.NOT_IMPLEMENTED.getHttpStatus()).isEqualTo(501);
    }

    @Test
    void getHttpStatus_serviceUnavailable_returns503() {
        assertThat(ErrorCode.SERVICE_UNAVAILABLE.getHttpStatus()).isEqualTo(503);
    }
}
