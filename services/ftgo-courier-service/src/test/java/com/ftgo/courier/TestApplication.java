package com.ftgo.courier;

import com.ftgo.security.config.FtgoSecurityAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Minimal Spring Boot configuration for @WebMvcTest slicing in tests.
 * Required because the courier service does not yet have a main application class.
 *
 * <p>Uses @SpringBootConfiguration + @EnableAutoConfiguration instead of
 * @SpringBootApplication to avoid component scanning non-web beans.
 *
 * <p>Excludes {@link FtgoSecurityAutoConfiguration} because that imports
 * the full SecurityFilterChainConfig which requires beans not available in
 * the @WebMvcTest slice. The authorization tests import RoleHierarchyConfig
 * directly for method-level security.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = FtgoSecurityAutoConfiguration.class)
public class TestApplication {
}
