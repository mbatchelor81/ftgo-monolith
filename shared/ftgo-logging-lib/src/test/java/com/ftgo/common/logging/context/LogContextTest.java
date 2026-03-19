package com.ftgo.common.logging.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class LogContextTest {

    @AfterEach
    void cleanUp() {
        MDC.clear();
    }

    @Test
    void setAndGetUserId() {
        LogContext.setUserId("user-42");
        assertThat(LogContext.getUserId()).isEqualTo("user-42");
        assertThat(MDC.get("userId")).isEqualTo("user-42");
    }

    @Test
    void setAndGetServiceName() {
        LogContext.setServiceName("ftgo-order-service");
        assertThat(MDC.get("serviceName")).isEqualTo("ftgo-order-service");
    }

    @Test
    void putAndGet() {
        LogContext.put("orderId", "123");
        assertThat(LogContext.get("orderId")).isEqualTo("123");
    }

    @Test
    void remove() {
        LogContext.put("orderId", "123");
        LogContext.remove("orderId");
        assertThat(LogContext.get("orderId")).isNull();
    }

    @Test
    void clear() {
        LogContext.setUserId("user-42");
        LogContext.put("orderId", "123");
        LogContext.clear();
        assertThat(LogContext.getUserId()).isNull();
        assertThat(LogContext.get("orderId")).isNull();
    }

    @Test
    void nullSafety() {
        // Should not throw
        LogContext.setUserId(null);
        LogContext.setServiceName(null);
        LogContext.put(null, "value");
        LogContext.put("key", null);
        LogContext.remove(null);
    }
}
