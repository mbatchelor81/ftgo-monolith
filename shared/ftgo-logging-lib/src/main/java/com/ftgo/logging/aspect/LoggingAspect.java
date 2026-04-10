package com.ftgo.logging.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * AOP aspect that provides automatic method entry/exit logging for service
 * layer methods in FTGO microservices.
 *
 * <p>Logs method invocations at configurable log levels and detects slow
 * executions. Controlled via {@link LoggingAspectProperties}.
 *
 * <p>The aspect targets Spring-managed beans annotated with
 * {@code @Service} within configured base packages.
 *
 * <p>Example output:
 * <pre>
 * DEBUG c.f.o.d.OrderService - --&gt; createOrder(consumerId=42, restaurantId=7)
 * DEBUG c.f.o.d.OrderService - &lt;-- createOrder completed in 45ms
 * WARN  c.f.o.d.OrderService - &lt;-- processPayment completed in 1250ms [SLOW]
 * </pre>
 */
@Aspect
public class LoggingAspect {

    private final LoggingAspectProperties properties;

    public LoggingAspect(LoggingAspectProperties properties) {
        this.properties = properties;
    }

    /**
     * Around advice for all public methods in classes annotated with
     * {@code @org.springframework.stereotype.Service} within packages
     * matching {@code com.ftgo..*}.
     */
    @Around("execution(public * com.ftgo..*.*(..)) && @within(org.springframework.stereotype.Service)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getName();

        // Check if the class is in one of the configured base packages
        if (!isInBasePackages(className)) {
            return joinPoint.proceed();
        }

        Logger logger = LoggerFactory.getLogger(signature.getDeclaringType());
        String methodName = signature.getName();

        // Log entry
        if (properties.isIncludeArgs() && isLogEnabled(logger)) {
            String args = formatArguments(signature.getParameterNames(), joinPoint.getArgs());
            log(logger, "--> {}({})", methodName, args);
        } else if (isLogEnabled(logger)) {
            log(logger, "--> {}()", methodName);
        }

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // Log exit
            if (duration >= properties.getSlowExecutionThresholdMs()) {
                logger.warn("<-- {} completed in {}ms [SLOW]", methodName, duration);
            } else if (properties.isIncludeResult() && result != null && isLogEnabled(logger)) {
                log(logger, "<-- {} returned {} in {}ms", methodName, result, duration);
            } else if (isLogEnabled(logger)) {
                log(logger, "<-- {} completed in {}ms", methodName, duration);
            }

            return result;
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("<-- {} threw {} in {}ms", methodName,
                    throwable.getClass().getSimpleName(), duration);
            throw throwable;
        }
    }

    private boolean isInBasePackages(String className) {
        if (properties.getBasePackages() == null || properties.getBasePackages().isEmpty()) {
            // Default: instrument all com.ftgo packages
            return className.startsWith("com.ftgo");
        }
        return properties.getBasePackages().stream()
                .anyMatch(className::startsWith);
    }

    private boolean isLogEnabled(Logger logger) {
        return switch (properties.getLogLevel().toUpperCase()) {
            case "TRACE" -> logger.isTraceEnabled();
            case "INFO" -> logger.isInfoEnabled();
            case "WARN" -> logger.isWarnEnabled();
            default -> logger.isDebugEnabled();
        };
    }

    private void log(Logger logger, String format, Object... args) {
        switch (properties.getLogLevel().toUpperCase()) {
            case "TRACE" -> logger.trace(format, args);
            case "INFO" -> logger.info(format, args);
            case "WARN" -> logger.warn(format, args);
            default -> logger.debug(format, args);
        }
    }

    private static String formatArguments(String[] paramNames, Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        if (paramNames != null && paramNames.length == args.length) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < paramNames.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(paramNames[i]).append('=').append(summarize(args[i]));
            }
            return sb.toString();
        }
        return Arrays.stream(args)
                .map(LoggingAspect::summarize)
                .collect(Collectors.joining(", "));
    }

    private static String summarize(Object arg) {
        if (arg == null) {
            return "null";
        }
        String str = arg.toString();
        if (str.length() > 100) {
            return str.substring(0, 97) + "...";
        }
        return str;
    }
}
