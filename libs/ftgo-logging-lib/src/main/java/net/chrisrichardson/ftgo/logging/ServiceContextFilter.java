package net.chrisrichardson.ftgo.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ServiceContextFilter extends OncePerRequestFilter {

    private final String serviceName;

    public ServiceContextFilter(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        MDC.put("serviceName", serviceName);
        MDC.put("httpMethod", request.getMethod());
        MDC.put("requestUri", request.getRequestURI());

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("serviceName");
            MDC.remove("httpMethod");
            MDC.remove("requestUri");
        }
    }
}
