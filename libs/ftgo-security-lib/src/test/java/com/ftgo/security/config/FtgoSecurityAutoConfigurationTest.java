package com.ftgo.security.config;

import com.ftgo.security.exception.SecurityExceptionHandlers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoSecurityAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    WebMvcAutoConfiguration.class,
                    FtgoSecurityAutoConfiguration.class));

    @Test
    void securityFilterChainIsRegistered() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SecurityFilterChain.class);
        });
    }

    @Test
    void securityExceptionHandlersAreRegistered() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SecurityExceptionHandlers.class);
        });
    }

    @Test
    void corsConfigurationSourceIsRegistered() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(CorsConfigurationSource.class);
        });
    }

    @Test
    void disabledSecurityProvidesPermitAllFilterChain() {
        contextRunner
                .withPropertyValues("ftgo.security.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(SecurityFilterChain.class);
                });
    }

    @Test
    void customPublicPathsAreConfigurable() {
        contextRunner
                .withPropertyValues(
                        "ftgo.security.public-paths[0]=/api/public/**",
                        "ftgo.security.public-paths[1]=/health")
                .run(context -> {
                    assertThat(context).hasSingleBean(SecurityFilterChain.class);
                    FtgoSecurityProperties props = context.getBean(FtgoSecurityProperties.class);
                    assertThat(props.getPublicPaths()).containsExactly("/api/public/**", "/health");
                });
    }

    @Test
    void corsPropertiesAreConfigurable() {
        contextRunner
                .withPropertyValues(
                        "ftgo.security.cors.allowed-origins[0]=https://example.com",
                        "ftgo.security.cors.allow-credentials=true",
                        "ftgo.security.cors.max-age=7200")
                .run(context -> {
                    FtgoSecurityProperties props = context.getBean(FtgoSecurityProperties.class);
                    assertThat(props.getCors().getAllowedOrigins()).containsExactly("https://example.com");
                    assertThat(props.getCors().isAllowCredentials()).isTrue();
                    assertThat(props.getCors().getMaxAge()).isEqualTo(7200);
                });
    }
}
