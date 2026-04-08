package com.ftgo.logging.mdc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class LogContextTest {

    @AfterEach
    void tearDown() {
        LogContext.clear();
    }

    @Test
    void setUserId_setsValueInMDC() {
        LogContext.setUserId("consumer-42");
        assertThat(MDC.get(LogContext.KEY_USER_ID)).isEqualTo("consumer-42");
    }

    @Test
    void setUserId_null_removesFromMDC() {
        LogContext.setUserId("consumer-42");
        LogContext.setUserId(null);
        assertThat(MDC.get(LogContext.KEY_USER_ID)).isNull();
    }

    @Test
    void setRequestId_setsValueInMDC() {
        LogContext.setRequestId("req-123");
        assertThat(MDC.get(LogContext.KEY_REQUEST_ID)).isEqualTo("req-123");
    }

    @Test
    void setCorrelationId_setsValueInMDC() {
        LogContext.setCorrelationId("corr-456");
        assertThat(MDC.get(LogContext.KEY_CORRELATION_ID)).isEqualTo("corr-456");
    }

    @Test
    void setServiceName_setsValueInMDC() {
        LogContext.setServiceName("ftgo-order-service");
        assertThat(MDC.get(LogContext.KEY_SERVICE_NAME)).isEqualTo("ftgo-order-service");
    }

    @Test
    void setRequestMethod_setsValueInMDC() {
        LogContext.setRequestMethod("POST");
        assertThat(MDC.get(LogContext.KEY_REQUEST_METHOD)).isEqualTo("POST");
    }

    @Test
    void setRequestUri_setsValueInMDC() {
        LogContext.setRequestUri("/api/orders");
        assertThat(MDC.get(LogContext.KEY_REQUEST_URI)).isEqualTo("/api/orders");
    }

    @Test
    void getUserId_returnsSetValue() {
        LogContext.setUserId("user-99");
        assertThat(LogContext.getUserId()).isEqualTo("user-99");
    }

    @Test
    void getRequestId_returnsSetValue() {
        LogContext.setRequestId("req-abc");
        assertThat(LogContext.getRequestId()).isEqualTo("req-abc");
    }

    @Test
    void getCorrelationId_returnsSetValue() {
        LogContext.setCorrelationId("corr-xyz");
        assertThat(LogContext.getCorrelationId()).isEqualTo("corr-xyz");
    }

    @Test
    void getServiceName_returnsSetValue() {
        LogContext.setServiceName("order-svc");
        assertThat(LogContext.getServiceName()).isEqualTo("order-svc");
    }

    @Test
    void clear_removesAllFtgoFields() {
        LogContext.setUserId("user-1");
        LogContext.setRequestId("req-1");
        LogContext.setCorrelationId("corr-1");
        LogContext.setServiceName("svc-1");
        LogContext.setRequestMethod("GET");
        LogContext.setRequestUri("/test");

        LogContext.clear();

        assertThat(MDC.get(LogContext.KEY_USER_ID)).isNull();
        assertThat(MDC.get(LogContext.KEY_REQUEST_ID)).isNull();
        assertThat(MDC.get(LogContext.KEY_CORRELATION_ID)).isNull();
        assertThat(MDC.get(LogContext.KEY_SERVICE_NAME)).isNull();
        assertThat(MDC.get(LogContext.KEY_REQUEST_METHOD)).isNull();
        assertThat(MDC.get(LogContext.KEY_REQUEST_URI)).isNull();
    }

    @Test
    void clear_doesNotAffectNonFtgoMdcFields() {
        MDC.put("customField", "customValue");
        LogContext.setUserId("user-1");

        LogContext.clear();

        assertThat(MDC.get(LogContext.KEY_USER_ID)).isNull();
        assertThat(MDC.get("customField")).isEqualTo("customValue");

        MDC.remove("customField");
    }
}
