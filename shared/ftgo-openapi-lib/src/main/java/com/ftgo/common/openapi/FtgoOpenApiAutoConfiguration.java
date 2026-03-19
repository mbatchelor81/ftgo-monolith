package com.ftgo.common.openapi;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration entry point for FTGO shared OpenAPI configuration.
 * Activates automatically when ftgo-openapi-lib is on the classpath.
 */
@AutoConfiguration
@Import(OpenApiConfiguration.class)
public class FtgoOpenApiAutoConfiguration {
}
