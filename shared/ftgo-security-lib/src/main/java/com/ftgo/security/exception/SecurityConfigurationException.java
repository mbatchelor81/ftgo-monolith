package com.ftgo.security.exception;

/**
 * Exception thrown when the security configuration is invalid or incomplete.
 *
 * <p>For example, this may be thrown if required security properties are
 * missing or if an unsupported authentication scheme is configured.
 */
public class SecurityConfigurationException extends RuntimeException {

    public SecurityConfigurationException(String message) {
        super(message);
    }

    public SecurityConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
