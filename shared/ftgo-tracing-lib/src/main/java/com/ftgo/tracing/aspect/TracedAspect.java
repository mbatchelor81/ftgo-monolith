package com.ftgo.tracing.aspect;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * AOP aspect that creates custom tracing spans for methods annotated with {@link Traced}.
 *
 * <p>Each invocation of a {@code @Traced} method produces a new child span
 * under the current trace context. The span includes:
 * <ul>
 *   <li>Span name — from {@link Traced#value()} or the method name</li>
 *   <li>Tags — {@code class} and {@code method}, plus any custom tags</li>
 *   <li>Error status — automatically recorded on exception</li>
 * </ul>
 *
 * <p>This class is registered as a bean via {@code TracingConfiguration},
 * not via {@code @Component} scanning, to ensure proper ordering with
 * {@code @ConditionalOnBean(Tracer.class)}.
 */
@Aspect
public class TracedAspect {

    private static final Logger log = LoggerFactory.getLogger(TracedAspect.class);

    private final Tracer tracer;

    public TracedAspect(Tracer tracer) {
        this.tracer = tracer;
    }

    @Around("@annotation(traced)")
    public Object traceMethod(ProceedingJoinPoint joinPoint, Traced traced) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String spanName = traced.value().isEmpty()
                ? method.getDeclaringClass().getSimpleName() + "." + method.getName()
                : traced.value();

        Span span = tracer.nextSpan().name(spanName);
        span.tag("class", method.getDeclaringClass().getSimpleName());
        span.tag("method", method.getName());

        for (String tagEntry : traced.tags()) {
            String[] parts = tagEntry.split("=", 2);
            if (parts.length == 2) {
                span.tag(parts[0].trim(), parts[1].trim());
            }
        }

        try (Tracer.SpanInScope ignored = tracer.withSpan(span.start())) {
            log.debug("Started span: {} [traceId={}]", spanName, span.context().traceId());
            Object result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            span.error(ex);
            throw ex;
        } finally {
            span.end();
        }
    }
}
