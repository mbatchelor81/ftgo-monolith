package net.chrisrichardson.ftgo.common.jpa;

import net.chrisrichardson.ftgo.common.CommonConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Shared JPA configuration that enables auto-configuration and imports
 * common value objects. Modules depending on ftgo-common-jpa get JPA
 * support and the common library transitively.
 */
@Configuration
@EnableAutoConfiguration
@Import(CommonConfiguration.class)
public class JpaConfiguration {
}
