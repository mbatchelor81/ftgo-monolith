package com.ftgo.errorhandling.handler;

import com.ftgo.errorhandling.dto.ErrorResponse;
import com.ftgo.errorhandling.exception.ErrorCodes;
import com.ftgo.errorhandling.exception.ResourceNotFoundException;
import com.ftgo.errorhandling.exception.ServiceCommunicationException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import net.chrisrichardson.ftgo.common.NotYetImplementedException;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.OrderMinimumNotMetException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private static final String TEST_TRACE_ID = "abc123def456";

    private GlobalExceptionHandler handlerWithTracing() {
        Tracer tracer = mock(Tracer.class);
        Span span = mock(Span.class);
        TraceContext traceContext = mock(TraceContext.class);

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn(TEST_TRACE_ID);

        return new GlobalExceptionHandler(tracer);
    }

    private MockHttpServletRequest defaultRequest() {
        return new MockHttpServletRequest("GET", "/api/v1/orders/1");
    }

    @Nested
    class WithTracing {

        @Test
        void handleResourceNotFound_returns404WithErrorCode() {
            var handler = handlerWithTracing();
            var request = defaultRequest();
            var ex = new ResourceNotFoundException("Order", 42L, ErrorCodes.ORDER_NOT_FOUND);

            ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCodes.ORDER_NOT_FOUND);
            assertThat(response.getBody().getMessage()).contains("Order not found with id: 42");
            assertThat(response.getBody().getTraceId()).isEqualTo(TEST_TRACE_ID);
            assertThat(response.getBody().getTimestamp()).isNotNull();
            assertThat(response.getBody().getPath()).isEqualTo("/api/v1/orders/1");
        }

        @Test
        void handleStateConflict_returns409() {
            var handler = handlerWithTracing();
            var request = defaultRequest();
            var ex = new UnsupportedStateTransitionException(TestState.APPROVED);

            ResponseEntity<ErrorResponse> response = handler.handleStateConflict(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCodes.STATE_CONFLICT);
            assertThat(response.getBody().getMessage()).contains("current state: APPROVED");
            assertThat(response.getBody().getTraceId()).isEqualTo(TEST_TRACE_ID);
        }

        @Test
        void handleOrderMinimumNotMet_returns422() {
            var handler = handlerWithTracing();
            var request = defaultRequest();
            var ex = new OrderMinimumNotMetException();

            ResponseEntity<ErrorResponse> response = handler.handleOrderMinimumNotMet(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCodes.ORDER_MINIMUM_NOT_MET);
            assertThat(response.getBody().getMessage()).contains("minimum");
            assertThat(response.getBody().getTraceId()).isEqualTo(TEST_TRACE_ID);
        }

        @Test
        void handleNotYetImplemented_returns501() {
            var handler = handlerWithTracing();
            var request = defaultRequest();
            var ex = new NotYetImplementedException();

            ResponseEntity<ErrorResponse> response = handler.handleNotYetImplemented(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCodes.INTERNAL_ERROR);
            assertThat(response.getBody().getTraceId()).isEqualTo(TEST_TRACE_ID);
        }

        @Test
        void handleServiceCommunication_returns502() {
            var handler = handlerWithTracing();
            var request = defaultRequest();
            var ex = new ServiceCommunicationException("consumer-service", "Connection refused");

            ResponseEntity<ErrorResponse> response = handler.handleServiceCommunication(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCodes.SERVICE_COMMUNICATION_ERROR);
            assertThat(response.getBody().getMessage()).contains("consumer-service");
            assertThat(response.getBody().getTraceId()).isEqualTo(TEST_TRACE_ID);
        }

        @Test
        void handleAllUncaught_returns500_withoutStackTrace() {
            var handler = handlerWithTracing();
            var request = defaultRequest();
            var ex = new RuntimeException("database connection pool exhausted");

            ResponseEntity<ErrorResponse> response = handler.handleAllUncaught(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCodes.INTERNAL_ERROR);
            assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
            assertThat(response.getBody().getMessage()).doesNotContain("database connection pool");
            assertThat(response.getBody().getTraceId()).isEqualTo(TEST_TRACE_ID);
        }
    }

    @Nested
    class WithNoopTracer {

        @Test
        void handleResourceNotFound_withNoopTracer_returnsNullTraceId() {
            var handler = new GlobalExceptionHandler(Tracer.NOOP);
            var request = defaultRequest();
            var ex = new ResourceNotFoundException("Order", 1L);

            ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTraceId()).isNull();
        }
    }

    private enum TestState {
        APPROVED, CANCELLED
    }
}
