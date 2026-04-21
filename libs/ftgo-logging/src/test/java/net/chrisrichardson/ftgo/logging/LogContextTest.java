package net.chrisrichardson.ftgo.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class LogContextTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void withUserId_setsMdcKey_andRestoresOnClose() {
        assertThat(MDC.get(MdcKeys.USER_ID)).isNull();

        try (var ignored = LogContext.withUserId("user-123")) {
            assertThat(MDC.get(MdcKeys.USER_ID)).isEqualTo("user-123");
        }

        assertThat(MDC.get(MdcKeys.USER_ID)).isNull();
    }

    @Test
    void withRequestId_acceptsNonStringValues() {
        try (var ignored = LogContext.withRequestId(42L)) {
            assertThat(MDC.get(MdcKeys.REQUEST_ID)).isEqualTo("42");
        }
    }

    @Test
    void nestedScopes_restorePreviousValueOnClose() {
        try (var outer = LogContext.withUserId("outer")) {
            assertThat(MDC.get(MdcKeys.USER_ID)).isEqualTo("outer");

            try (var inner = LogContext.withUserId("inner")) {
                assertThat(MDC.get(MdcKeys.USER_ID)).isEqualTo("inner");
            }

            assertThat(MDC.get(MdcKeys.USER_ID)).isEqualTo("outer");
        }

        assertThat(MDC.get(MdcKeys.USER_ID)).isNull();
    }

    @Test
    void put_withNullValue_removesMdcKey() {
        MDC.put(MdcKeys.USER_ID, "existing");

        try (var ignored = LogContext.put(MdcKeys.USER_ID, null)) {
            assertThat(MDC.get(MdcKeys.USER_ID)).isNull();
        }

        assertThat(MDC.get(MdcKeys.USER_ID)).isEqualTo("existing");
    }

    @Test
    void clear_removesCanonicalFtgoKeys() {
        MDC.put(MdcKeys.USER_ID, "u");
        MDC.put(MdcKeys.REQUEST_ID, "r");
        MDC.put(MdcKeys.CORRELATION_ID, "c");
        MDC.put("other-key", "keep-me");

        LogContext.clear();

        assertThat(MDC.get(MdcKeys.USER_ID)).isNull();
        assertThat(MDC.get(MdcKeys.REQUEST_ID)).isNull();
        assertThat(MDC.get(MdcKeys.CORRELATION_ID)).isNull();
        assertThat(MDC.get("other-key")).isEqualTo("keep-me");
    }

    @Test
    void snapshot_returnsImmutableCopyOfCurrentMdc() {
        MDC.put(MdcKeys.USER_ID, "u");

        var snap = LogContext.snapshot();

        assertThat(snap).containsEntry(MdcKeys.USER_ID, "u");
        MDC.put(MdcKeys.USER_ID, "changed");
        assertThat(snap).containsEntry(MdcKeys.USER_ID, "u");
    }
}
