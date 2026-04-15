package com.ftgo.observability.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class LogContextTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void setUserId_withValidValue_populatesMdc() {
        LogContext.setUserId("user-42");

        assertThat(MDC.get(LogContext.USER_ID_KEY)).isEqualTo("user-42");
    }

    @Test
    void setUserId_withNull_doesNotPopulateMdc() {
        LogContext.setUserId(null);

        assertThat(MDC.get(LogContext.USER_ID_KEY)).isNull();
    }

    @Test
    void setUserId_withBlank_doesNotPopulateMdc() {
        LogContext.setUserId("   ");

        assertThat(MDC.get(LogContext.USER_ID_KEY)).isNull();
    }

    @Test
    void setRequestId_withValidValue_populatesMdc() {
        LogContext.setRequestId("req-123");

        assertThat(MDC.get(LogContext.REQUEST_ID_KEY)).isEqualTo("req-123");
    }

    @Test
    void setCorrelationId_withValidValue_populatesMdc() {
        LogContext.setCorrelationId("corr-456");

        assertThat(MDC.get(LogContext.CORRELATION_ID_KEY)).isEqualTo("corr-456");
    }

    @Test
    void setServiceName_withValidValue_populatesMdc() {
        LogContext.setServiceName("ftgo-order-service");

        assertThat(MDC.get(LogContext.SERVICE_KEY)).isEqualTo("ftgo-order-service");
    }

    @Test
    void clear_removesAllFtgoMdcFields() {
        LogContext.setUserId("user-42");
        LogContext.setRequestId("req-123");
        LogContext.setCorrelationId("corr-456");
        LogContext.setServiceName("ftgo-order-service");

        LogContext.clear();

        assertThat(LogContext.getUserId()).isNull();
        assertThat(LogContext.getRequestId()).isNull();
        assertThat(LogContext.getCorrelationId()).isNull();
        assertThat(LogContext.getServiceName()).isNull();
    }

    @Test
    void getUserId_afterSetting_returnsValue() {
        LogContext.setUserId("user-99");

        assertThat(LogContext.getUserId()).isEqualTo("user-99");
    }

    @Test
    void getRequestId_afterSetting_returnsValue() {
        LogContext.setRequestId("req-abc");

        assertThat(LogContext.getRequestId()).isEqualTo("req-abc");
    }
}
