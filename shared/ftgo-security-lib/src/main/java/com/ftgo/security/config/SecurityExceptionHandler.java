package com.ftgo.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides security exception handlers that return structured JSON error
 * responses instead of default HTML error pages.
 *
 * <p>Two beans are exposed:
 * <ul>
 *   <li>{@link AuthenticationEntryPoint} — handles 401 Unauthorized</li>
 *   <li>{@link AccessDeniedHandler} — handles 403 Forbidden</li>
 * </ul>
 */
@Configuration
public class SecurityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityExceptionHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public AuthenticationEntryPoint ftgoAuthenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response,
                org.springframework.security.core.AuthenticationException authException) -> {
            log.warn("Unauthorized access attempt: uri={}, message={}",
                request.getRequestURI(), authException.getMessage());
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED,
                "Authentication required", request.getRequestURI());
        };
    }

    @Bean
    public AccessDeniedHandler ftgoAccessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response,
                org.springframework.security.access.AccessDeniedException accessDeniedException) -> {
            log.warn("Access denied: uri={}, message={}",
                request.getRequestURI(), accessDeniedException.getMessage());
            writeErrorResponse(response, HttpStatus.FORBIDDEN,
                "Access denied", request.getRequestURI());
        };
    }

    private static void writeErrorResponse(HttpServletResponse response, HttpStatus status,
                                            String message, String path) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
