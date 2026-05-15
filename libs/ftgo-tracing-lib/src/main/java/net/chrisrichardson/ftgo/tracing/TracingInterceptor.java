package net.chrisrichardson.ftgo.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Adds service-level span tags to every incoming HTTP request so that
 * each trace is annotated with the HTTP method, URI, and response status.
 */
public class TracingInterceptor implements HandlerInterceptor {

    private final Tracer tracer;

    public TracingInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("http.method", request.getMethod());
            currentSpan.tag("http.url", request.getRequestURI());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("http.status_code", String.valueOf(response.getStatus()));
            if (ex != null) {
                currentSpan.error(ex);
            }
        }
    }
}
