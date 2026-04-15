package com.ftgo.observability.logging;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AOP aspect that logs method entry, exit, and exceptions for service-layer methods.
 *
 * <p>Activated when {@code ftgo.logging.aspect.enabled=true} (the default). Targets all public
 * methods in Spring {@code @Service} components under the configured base package.
 *
 * <p>Log levels:
 *
 * <ul>
 *   <li>{@code DEBUG} — method entry with arguments and method exit with duration
 *   <li>{@code DEBUG} — exceptions with class and message (error-level logging is left to the
 *       global exception handler)
 * </ul>
 */
@Aspect
public class LoggingAspect {

    /**
     * Intercepts public methods on classes annotated with {@code @Service} within the {@code
     * com.ftgo} package hierarchy.
     */
    @Around(
            "execution(public * com.ftgo..*(..)) "
                    + "&& (@within(org.springframework.stereotype.Service) "
                    + "|| @within(org.springframework.stereotype.Component))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

        String methodName = joinPoint.getSignature().getName();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Entering {}.{}({})",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    methodName,
                    formatArgs(joinPoint.getArgs()));
        }

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            if (logger.isDebugEnabled()) {
                long elapsed = System.currentTimeMillis() - startTime;
                logger.debug(
                        "Exiting {}.{}() — took {} ms",
                        joinPoint.getTarget().getClass().getSimpleName(),
                        methodName,
                        elapsed);
            }
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            logger.debug(
                    "Exception in {}.{}() after {} ms — {}: {}",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    methodName,
                    elapsed,
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
            throw ex;
        }
    }

    private static String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args)
                .map(arg -> arg == null ? "null" : arg.getClass().getSimpleName())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
