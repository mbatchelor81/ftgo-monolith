package com.ftgo.common.error;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for FTGO error handling.
 * Services that depend on ftgo-error-handling-lib get the
 * {@link GlobalExceptionHandler} automatically.
 */
@AutoConfiguration
@Import(GlobalExceptionHandler.class)
public class FtgoErrorHandlingAutoConfiguration {
}
