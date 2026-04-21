package net.chrisrichardson.ftgo.resilience.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DependentServiceHealthIndicatorRegistrarTest {

    @Test
    void toBeanName_hyphenatedServiceName_rendersAsCamelCaseIndicator() {
        assertThat(FtgoResilienceAutoConfiguration.DependentServiceHealthIndicatorRegistrar
                .toBeanName("consumer-service"))
                .isEqualTo("consumerServiceHealthIndicator");
    }

    @Test
    void toBeanName_singleWord_appendsIndicatorSuffix() {
        assertThat(FtgoResilienceAutoConfiguration.DependentServiceHealthIndicatorRegistrar
                .toBeanName("order"))
                .isEqualTo("orderHealthIndicator");
    }

    @Test
    void toBeanName_multipleHyphens_camelCasesEverySegment() {
        assertThat(FtgoResilienceAutoConfiguration.DependentServiceHealthIndicatorRegistrar
                .toBeanName("foo-bar-baz"))
                .isEqualTo("fooBarBazHealthIndicator");
    }
}
