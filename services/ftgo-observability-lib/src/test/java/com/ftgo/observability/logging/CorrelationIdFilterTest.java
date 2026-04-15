package com.ftgo.observability.logging;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void doFilterInternal_withMissingHeader_generatesCorrelationId() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        String responseHeader = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(responseHeader).isNotNull().isNotBlank();
        // UUID format: 8-4-4-4-12 hex chars
        assertThat(responseHeader)
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void doFilterInternal_withExistingHeader_usesProvidedCorrelationId() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "test-correlation-123");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER))
                .isEqualTo("test-correlation-123");
    }

    @Test
    void doFilterInternal_afterExecution_cleansMdcContext() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
        assertThat(MDC.get(CorrelationIdFilter.REQUEST_ID_MDC_KEY)).isNull();
        assertThat(MDC.get(CorrelationIdFilter.USER_ID_MDC_KEY)).isNull();
    }

    @Test
    void doFilterInternal_withBlankHeader_generatesNewId() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "   ");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        String responseHeader = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(responseHeader).isNotBlank();
        assertThat(responseHeader.trim()).isNotEqualTo("");
    }

    @Test
    void doFilterInternal_withMissingRequestIdHeader_generatesRequestId() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        String requestIdHeader = response.getHeader(CorrelationIdFilter.REQUEST_ID_HEADER);
        assertThat(requestIdHeader).isNotNull().isNotBlank();
        assertThat(requestIdHeader)
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void doFilterInternal_withExistingRequestIdHeader_usesProvidedRequestId() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.REQUEST_ID_HEADER, "req-abc-123");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.REQUEST_ID_HEADER))
                .isEqualTo("req-abc-123");
    }

    @Test
    void doFilterInternal_withUserIdHeader_populatesMdcWithUserId() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.USER_ID_HEADER, "user-42");
        var response = new MockHttpServletResponse();

        final String[] capturedUserId = new String[1];
        FilterChain chain =
                (req, res) -> {
                    capturedUserId[0] = MDC.get(CorrelationIdFilter.USER_ID_MDC_KEY);
                };

        filter.doFilterInternal(request, response, chain);

        assertThat(capturedUserId[0]).isEqualTo("user-42");
    }

    @Test
    void doFilterInternal_withoutUserIdHeader_doesNotSetUserIdMdc() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        final String[] capturedUserId = new String[1];
        FilterChain chain =
                (req, res) -> {
                    capturedUserId[0] = MDC.get(CorrelationIdFilter.USER_ID_MDC_KEY);
                };

        filter.doFilterInternal(request, response, chain);

        assertThat(capturedUserId[0]).isNull();
    }
}
