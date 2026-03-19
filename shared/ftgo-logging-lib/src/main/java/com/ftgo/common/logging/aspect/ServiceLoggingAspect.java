package com.ftgo.common.logging.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that logs method entry and exit for @Service classes
 * in the com.ftgo package hierarchy.
 *
 * Logs at DEBUG level:
 * - Method entry with class and method name
 * - Method exit with execution duration
 * - Method failure with exception type
 *
 * To enable, ensure the {@code ftgo-logging-lib} is on the classpath
 * and AOP auto-proxy is enabled (default in Spring Boot).
 */
@Aspect
@Component
public class ServiceLoggingAspect {

    @Around("within(com.ftgo..*)  && @within(org.springframework.stereotype.Service)")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String methodName = joinPoint.getSignature().getName();

        if (!log.isDebugEnabled()) {
            return joinPoint.proceed();
        }

        log.debug("Entering {}", methodName);
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.debug("Exiting {} ({}ms)", methodName, duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.debug("Failed {} ({}ms) — {}: {}",
                    methodName, duration, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }
}
