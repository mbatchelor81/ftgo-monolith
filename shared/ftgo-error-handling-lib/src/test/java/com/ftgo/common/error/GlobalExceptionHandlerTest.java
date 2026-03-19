package com.ftgo.common.error;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        MDC.clear();
    }

    @Test
    void handleFtgoServiceException_returnsCorrectErrorResponse() {
        FtgoServiceException ex = new FtgoServiceException(ErrorCode.RESOURCE_NOT_FOUND, "Order 123 not found");

        ResponseEntity<ErrorResponse> response = handler.handleFtgoServiceException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("FTGO-003");
        assertThat(response.getBody().getMessage()).isEqualTo("Order 123 not found");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void handleFtgoServiceException_includesTraceIdFromMDC() {
        MDC.put("traceId", "abc-123-trace");
        FtgoServiceException ex = new FtgoServiceException(ErrorCode.INTERNAL_ERROR);

        ResponseEntity<ErrorResponse> response = handler.handleFtgoServiceException(ex, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTraceId()).isEqualTo("abc-123-trace");
    }

    @Test
    void handleFtgoServiceException_noTraceIdWhenMDCEmpty() {
        FtgoServiceException ex = new FtgoServiceException(ErrorCode.BAD_REQUEST);

        ResponseEntity<ErrorResponse> response = handler.handleFtgoServiceException(ex, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTraceId()).isNull();
    }

    @Test
    void handleIllegalArgument_returnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid quantity");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("FTGO-007");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid quantity");
    }

    @Test
    void handleIllegalState_returnsUnprocessableEntity() {
        IllegalStateException ex = new IllegalStateException("Cannot cancel delivered order");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalState(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("FTGO-300");
        assertThat(response.getBody().getMessage()).isEqualTo("Cannot cancel delivered order");
    }

    @Test
    void handleAllUncaughtExceptions_returnsInternalServerError() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorResponse> response = handler.handleAllUncaughtExceptions(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("FTGO-001");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }
}
