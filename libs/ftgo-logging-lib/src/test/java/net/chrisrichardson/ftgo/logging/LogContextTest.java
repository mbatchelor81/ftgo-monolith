package net.chrisrichardson.ftgo.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LogContextTest {

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void putUserId_validValue_setsInMdc() {
        LogContext.putUserId("user-42");
        assertThat(MDC.get(LogContext.USER_ID)).isEqualTo("user-42");
    }

    @Test
    void putRequestId_validValue_setsInMdc() {
        LogContext.putRequestId("req-abc");
        assertThat(MDC.get(LogContext.REQUEST_ID)).isEqualTo("req-abc");
    }

    @Test
    void putOrderId_validValue_setsInMdc() {
        LogContext.putOrderId("order-99");
        assertThat(MDC.get(LogContext.ORDER_ID)).isEqualTo("order-99");
    }

    @Test
    void putRestaurantId_validValue_setsInMdc() {
        LogContext.putRestaurantId("rest-7");
        assertThat(MDC.get(LogContext.RESTAURANT_ID)).isEqualTo("rest-7");
    }

    @Test
    void snapshot_multipleKeys_capturesAll() {
        LogContext.putUserId("u1");
        LogContext.putRequestId("r1");
        Map<String, String> snap = LogContext.snapshot();
        assertThat(snap).containsEntry(LogContext.USER_ID, "u1");
        assertThat(snap).containsEntry(LogContext.REQUEST_ID, "r1");
    }

    @Test
    void restore_withSnapshot_appliesContext() {
        Map<String, String> context = Map.of(LogContext.USER_ID, "u2");
        LogContext.restore(context);
        assertThat(MDC.get(LogContext.USER_ID)).isEqualTo("u2");
    }

    @Test
    void clear_withExistingKeys_removesAll() {
        LogContext.putUserId("u1");
        LogContext.clear();
        assertThat(MDC.get(LogContext.USER_ID)).isNull();
    }

    @Test
    void wrap_withMdcContext_propagatesToRunnable() throws Exception {
        LogContext.putUserId("u-wrap");
        Runnable wrapped = LogContext.wrap(() -> {
            assertThat(MDC.get(LogContext.USER_ID)).isEqualTo("u-wrap");
        });

        MDC.clear();
        assertThat(MDC.get(LogContext.USER_ID)).isNull();

        wrapped.run();
        assertThat(MDC.get(LogContext.USER_ID)).isNull();
    }

    @Test
    void remove_singleKey_deletesOnlyThatKey() {
        LogContext.putUserId("u1");
        LogContext.putRequestId("r1");
        LogContext.remove(LogContext.USER_ID);
        assertThat(MDC.get(LogContext.USER_ID)).isNull();
        assertThat(MDC.get(LogContext.REQUEST_ID)).isEqualTo("r1");
    }
}
