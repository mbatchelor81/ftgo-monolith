package com.ftgo.observability.logging;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ServiceMdcFilterTest {

    private final ServiceMdcFilter filter = new ServiceMdcFilter("ftgo-test-service");

    @Test
    void doFilterInternal_withGetRequest_populatesMdcWithServiceAndRequestContext() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/consumers/1");
        var response = new MockHttpServletResponse();

        // Capture MDC values during filter chain execution
        final String[] capturedService = new String[1];
        final String[] capturedMethod = new String[1];
        final String[] capturedUri = new String[1];

        FilterChain chain =
                (req, res) -> {
                    capturedService[0] = MDC.get(ServiceMdcFilter.SERVICE_MDC_KEY);
                    capturedMethod[0] = MDC.get(ServiceMdcFilter.REQUEST_METHOD_MDC_KEY);
                    capturedUri[0] = MDC.get(ServiceMdcFilter.REQUEST_URI_MDC_KEY);
                };

        filter.doFilterInternal(request, response, chain);

        assertThat(capturedService[0]).isEqualTo("ftgo-test-service");
        assertThat(capturedMethod[0]).isEqualTo("GET");
        assertThat(capturedUri[0]).isEqualTo("/api/consumers/1");
    }

    @Test
    void doFilterInternal_afterExecution_cleansMdcContext() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/orders");
        var response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {};

        filter.doFilterInternal(request, response, chain);

        assertThat(MDC.get(ServiceMdcFilter.SERVICE_MDC_KEY)).isNull();
        assertThat(MDC.get(ServiceMdcFilter.REQUEST_METHOD_MDC_KEY)).isNull();
        assertThat(MDC.get(ServiceMdcFilter.REQUEST_URI_MDC_KEY)).isNull();
    }
}
