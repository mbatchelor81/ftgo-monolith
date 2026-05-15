package net.chrisrichardson.ftgo.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class TracedAspect {

    private final Tracer tracer;

    public TracedAspect(Tracer tracer) {
        this.tracer = tracer;
    }

    @Around("@annotation(net.chrisrichardson.ftgo.tracing.Traced)")
    public Object traceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Traced annotation = method.getAnnotation(Traced.class);

        String spanName = annotation.value().isEmpty()
                ? signature.getDeclaringType().getSimpleName() + "." + method.getName()
                : annotation.value();

        Span span = tracer.nextSpan().name(spanName).start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            span.tag("class", signature.getDeclaringType().getName());
            span.tag("method", method.getName());
            return joinPoint.proceed();
        } catch (Throwable t) {
            span.error(t);
            throw t;
        } finally {
            span.end();
        }
    }
}
