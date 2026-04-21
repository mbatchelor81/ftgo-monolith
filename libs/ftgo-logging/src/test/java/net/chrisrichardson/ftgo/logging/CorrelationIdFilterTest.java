package net.chrisrichardson.ftgo.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void doFilter_withCorrelationIdHeader_propagatesToMdcAndResponse() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn("abc-123");

        AtomicReference<String> mdcDuringChain = new AtomicReference<>();
        doAnswer(invocation -> {
            mdcDuringChain.set(MDC.get(MdcKeys.CORRELATION_ID));
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        assertThat(mdcDuringChain.get()).isEqualTo("abc-123");
        verify(response).setHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "abc-123");
        assertThat(MDC.get(MdcKeys.CORRELATION_ID)).isNull();
    }

    @Test
    void doFilter_withoutHeaders_generatesUuidCorrelationId() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).setHeader(eq(CorrelationIdFilter.CORRELATION_ID_HEADER), anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_fallsBackToRequestIdHeader() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).thenReturn(null);
        when(request.getHeader(CorrelationIdFilter.REQUEST_ID_HEADER)).thenReturn("req-42");

        AtomicReference<String> mdcDuringChain = new AtomicReference<>();
        doAnswer(invocation -> {
            mdcDuringChain.set(MDC.get(MdcKeys.CORRELATION_ID));
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        assertThat(mdcDuringChain.get()).isEqualTo("req-42");
    }
}
