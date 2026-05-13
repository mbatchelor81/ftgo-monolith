package com.ftgo.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Produces structured JSON error responses for security exceptions
 * instead of Spring Boot's default HTML error page.
 */
public class SecurityExceptionHandlers {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            OBJECT_MAPPER.writeValue(response.getOutputStream(),
                    errorBody(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                            authException.getMessage(), request.getRequestURI()));
        };
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            OBJECT_MAPPER.writeValue(response.getOutputStream(),
                    errorBody(HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                            accessDeniedException.getMessage(), request.getRequestURI()));
        };
    }

    private Map<String, Object> errorBody(int status, String error, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return body;
    }
}
