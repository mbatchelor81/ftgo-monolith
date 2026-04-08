package com.ftgo.logging.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        request = new MockHttpServletRequest("GET", "/api/orders");
        response = new MockHttpServletResponse();
    }

    @Test
    void doFilterInternal_withCorrelationIdHeader_usesProvidedValue() throws Exception {
        request.addHeader("X-Correlation-ID", "provided-corr-id");

        filter.doFilterInternal(request, response, captureAndAssert((req, res) -> {
            assertThat(MDC.get("correlationId")).isEqualTo("provided-corr-id");
        }));

        assertThat(response.getHeader("X-Correlation-ID")).isEqualTo("provided-corr-id");
    }

    @Test
    void doFilterInternal_withoutCorrelationIdHeader_generatesUuid() throws Exception {
        filter.doFilterInternal(request, response, captureAndAssert((req, res) -> {
            String correlationId = MDC.get("correlationId");
            assertThat(correlationId).isNotNull().isNotBlank();
            // Validate UUID format
            assertThat(correlationId).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }));
    }

    @Test
    void doFilterInternal_generatesRequestId() throws Exception {
        filter.doFilterInternal(request, response, captureAndAssert((req, res) -> {
            String requestId = MDC.get("requestId");
            assertThat(requestId).isNotNull().isNotBlank();
            assertThat(requestId).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }));
    }

    @Test
    void doFilterInternal_setsRequestMethodAndUri() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/orders/42");

        filter.doFilterInternal(request, response, captureAndAssert((req, res) -> {
            assertThat(MDC.get("requestMethod")).isEqualTo("POST");
            assertThat(MDC.get("requestUri")).isEqualTo("/api/orders/42");
        }));
    }

    @Test
    void doFilterInternal_withUserIdHeader_setsUserIdInMDC() throws Exception {
        request.addHeader("X-User-ID", "consumer-42");

        filter.doFilterInternal(request, response, captureAndAssert((req, res) -> {
            assertThat(MDC.get("userId")).isEqualTo("consumer-42");
        }));
    }

    @Test
    void doFilterInternal_withoutUserIdHeader_doesNotSetUserId() throws Exception {
        filter.doFilterInternal(request, response, captureAndAssert((req, res) -> {
            assertThat(MDC.get("userId")).isNull();
        }));
    }

    @Test
    void doFilterInternal_clearsMdcAfterRequest() throws Exception {
        request.addHeader("X-Correlation-ID", "test-corr-id");
        request.addHeader("X-User-ID", "user-1");

        filter.doFilterInternal(request, response, (req, res) -> {
            // MDC should be populated during filter chain
        });

        // MDC should be cleared after filter completes
        assertThat(MDC.get("correlationId")).isNull();
        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("userId")).isNull();
        assertThat(MDC.get("requestMethod")).isNull();
        assertThat(MDC.get("requestUri")).isNull();
    }

    @Test
    void doFilterInternal_clearsMdcEvenOnException() throws Exception {
        request.addHeader("X-User-ID", "user-1");

        try {
            filter.doFilterInternal(request, response, (req, res) -> {
                throw new ServletException("Test exception");
            });
        } catch (ServletException e) {
            // Expected
        }

        assertThat(MDC.get("correlationId")).isNull();
        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("userId")).isNull();
    }

    @FunctionalInterface
    interface FilterAction {
        void execute(MockHttpServletRequest request, MockHttpServletResponse response) throws Exception;
    }

    private FilterChain captureAndAssert(FilterAction action) {
        return (req, res) -> {
            try {
                action.execute((MockHttpServletRequest) req, (MockHttpServletResponse) res);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        };
    }
}
