package com.ftgo.security.handler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for security-related exceptions.
 *
 * <p>Intercepts {@link AuthenticationException} and {@link AccessDeniedException} thrown during
 * request processing and returns structured JSON error responses without leaking internal details.
 */
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied");
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
