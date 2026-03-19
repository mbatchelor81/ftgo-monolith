package com.ftgo.common.logging;

import com.ftgo.common.logging.filter.CorrelationIdMdcFilter;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdMdcFilterTest {

    private final CorrelationIdMdcFilter filter = new CorrelationIdMdcFilter();

    @Test
    void propagatesCorrelationIdFromHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "test-correlation-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader("X-Correlation-Id")).isEqualTo("test-correlation-123");
    }

    @Test
    void generatesCorrelationIdWhenHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader("X-Correlation-Id")).isNotNull().isNotBlank();
    }

    @Test
    void cleansMdcAfterRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "test-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(MDC.get(CorrelationIdMdcFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }
}
