package com.ftgo.observability.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

class LoggingAspectTest {

    private final LoggingAspect aspect = new LoggingAspect();

    @Test
    void logMethodExecution_withSuccessfulCall_returnsResult() throws Throwable {
        ProceedingJoinPoint joinPoint = createMockJoinPoint("createOrder", "arg1");
        when(joinPoint.proceed()).thenReturn("order-42");

        Object result = aspect.logMethodExecution(joinPoint);

        assertThat(result).isEqualTo("order-42");
    }

    @Test
    void logMethodExecution_withException_rethrowsException() throws Throwable {
        ProceedingJoinPoint joinPoint = createMockJoinPoint("createOrder");
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Order failed"));

        assertThatThrownBy(() -> aspect.logMethodExecution(joinPoint))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Order failed");
    }

    @Test
    void logMethodExecution_withNullReturn_returnsNull() throws Throwable {
        ProceedingJoinPoint joinPoint = createMockJoinPoint("deleteOrder");
        when(joinPoint.proceed()).thenReturn(null);

        Object result = aspect.logMethodExecution(joinPoint);

        assertThat(result).isNull();
    }

    private ProceedingJoinPoint createMockJoinPoint(String methodName, Object... args)
            throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.getTarget()).thenReturn(new SampleService());
        return joinPoint;
    }

    /** Dummy target class for the aspect to log against. */
    @Service
    static class SampleService {}
}
