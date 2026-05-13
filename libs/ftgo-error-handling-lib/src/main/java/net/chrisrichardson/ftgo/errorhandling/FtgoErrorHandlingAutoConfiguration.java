package net.chrisrichardson.ftgo.errorhandling;

import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that registers the {@link GlobalExceptionHandler}
 * when a servlet web application is detected.
 *
 * <p>Disabled by setting {@code ftgo.error-handling.enabled=false}.</p>
 */
@AutoConfiguration
@ConditionalOnClass(Tracer.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "ftgo.error-handling.enabled", havingValue = "true", matchIfMissing = true)
public class FtgoErrorHandlingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(Tracer tracer) {
        return new GlobalExceptionHandler(tracer);
    }
}
