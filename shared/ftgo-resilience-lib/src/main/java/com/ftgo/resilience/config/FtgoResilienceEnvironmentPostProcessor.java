package com.ftgo.resilience.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

/**
 * Loads {@code ftgo-resilience-defaults.yml} as a low-priority property source.
 *
 * <p>Spring Boot does not auto-load custom-named YAML files from the classpath.
 * This post-processor loads the library's defaults so that they apply to every
 * service that depends on {@code ftgo-resilience-lib}, while still allowing
 * services to override any property in their own {@code application.yml}.
 *
 * <p>Registered via {@code META-INF/spring.factories}.
 */
public class FtgoResilienceEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String RESOURCE_PATH = "ftgo-resilience-defaults.yml";
    private static final String PROPERTY_SOURCE_NAME = "ftgo-resilience-defaults";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        var resource = new ClassPathResource(RESOURCE_PATH);
        if (!resource.exists()) {
            return;
        }

        try {
            var loader = new YamlPropertySourceLoader();
            List<PropertySource<?>> sources = loader.load(PROPERTY_SOURCE_NAME, resource);
            for (PropertySource<?> source : sources) {
                // Add last so application.yml properties take precedence
                environment.getPropertySources().addLast(source);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + RESOURCE_PATH, e);
        }
    }
}
