package net.chrisrichardson.ftgo.resilience.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import net.chrisrichardson.ftgo.resilience.client.ResilientWebClientBuilder;
import net.chrisrichardson.ftgo.resilience.client.ServiceEndpoints;
import net.chrisrichardson.ftgo.resilience.health.DependentServiceHealthIndicator;

/**
 * Auto-configuration for the FTGO resilience library.
 *
 * <p>Wires up:
 * <ul>
 *     <li>{@link ServiceEndpoints} — resolves downstream service URLs
 *         from Kubernetes-native DNS config.</li>
 *     <li>{@link ResilientWebClientBuilder} — factory for
 *         {@link WebClient}s pre-wired with Resilience4j circuit breaker,
 *         retry, and bulkhead operators.</li>
 *     <li>One {@link DependentServiceHealthIndicator} per entry in
 *         {@code ftgo.resilience.dependent-services}, registered as a
 *         bean so Spring Boot Actuator rolls it into the
 *         {@code /actuator/health/readiness} endpoint.</li>
 * </ul>
 *
 * <p>Registered via
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * so services only need to declare a dependency on this library.
 */
@AutoConfiguration
@ConditionalOnClass({WebClient.class, CircuitBreakerRegistry.class})
@EnableConfigurationProperties(FtgoResilienceProperties.class)
public class FtgoResilienceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceEndpoints ftgoServiceEndpoints(FtgoResilienceProperties properties) {
        return new ServiceEndpoints(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResilientWebClientBuilder ftgoResilientWebClientBuilder(
            WebClient.Builder webClientBuilder,
            ServiceEndpoints endpoints,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            BulkheadRegistry bulkheadRegistry,
            FtgoResilienceProperties properties) {
        return new ResilientWebClientBuilder(
                webClientBuilder,
                endpoints,
                circuitBreakerRegistry,
                retryRegistry,
                bulkheadRegistry,
                properties.getResilience().getDefaultInstanceName());
    }

    /**
     * Registers one {@link DependentServiceHealthIndicator} bean per
     * configured dependent service. Using a
     * {@link BeanDefinitionRegistry} rather than a static {@code @Bean}
     * method keeps the indicator names dynamic so Spring Boot's health
     * group rollup renders them under their real service names (e.g.
     * {@code consumerService} instead of a generic
     * {@code dependentService}).
     */
    @Bean
    @ConditionalOnMissingBean(name = "ftgoDependentServiceHealthIndicatorRegistrar")
    public DependentServiceHealthIndicatorRegistrar ftgoDependentServiceHealthIndicatorRegistrar(
            ApplicationContext applicationContext,
            FtgoResilienceProperties properties,
            ServiceEndpoints endpoints,
            WebClient.Builder webClientBuilder) {
        DependentServiceHealthIndicatorRegistrar registrar =
                new DependentServiceHealthIndicatorRegistrar(applicationContext, properties, endpoints, webClientBuilder);
        registrar.registerIndicators();
        return registrar;
    }

    /**
     * Helper that walks {@code ftgo.resilience.dependent-services} and
     * registers one {@link DependentServiceHealthIndicator} bean per
     * entry. Extracted so the registration logic is testable in
     * isolation.
     */
    public static class DependentServiceHealthIndicatorRegistrar {

        private final ApplicationContext applicationContext;
        private final FtgoResilienceProperties properties;
        private final ServiceEndpoints endpoints;
        private final WebClient.Builder webClientBuilder;

        public DependentServiceHealthIndicatorRegistrar(ApplicationContext applicationContext,
                                                        FtgoResilienceProperties properties,
                                                        ServiceEndpoints endpoints,
                                                        WebClient.Builder webClientBuilder) {
            this.applicationContext = applicationContext;
            this.properties = properties;
            this.endpoints = endpoints;
            this.webClientBuilder = webClientBuilder;
        }

        public void registerIndicators() {
            if (!(applicationContext.getAutowireCapableBeanFactory() instanceof BeanDefinitionRegistry registry)) {
                return;
            }
            for (String serviceName : properties.getResilience().getDependentServices()) {
                String beanName = toBeanName(serviceName);
                if (registry.containsBeanDefinition(beanName)) {
                    continue;
                }
                GenericBeanDefinition definition = new GenericBeanDefinition();
                definition.setBeanClass(DependentServiceHealthIndicator.class);
                definition.setScope(BeanDefinition.SCOPE_SINGLETON);
                definition.setInstanceSupplier(() ->
                        new DependentServiceHealthIndicator(serviceName, endpoints, webClientBuilder));
                registry.registerBeanDefinition(beanName, definition);
            }
        }

        /**
         * Converts {@code consumer-service} →
         * {@code consumerServiceHealthIndicator} so Spring Boot renders it
         * under the {@code consumerService} key in the actuator JSON.
         */
        static String toBeanName(String serviceName) {
            StringBuilder camel = new StringBuilder();
            boolean upperNext = false;
            for (int i = 0; i < serviceName.length(); i++) {
                char c = serviceName.charAt(i);
                if (c == '-' || c == '_') {
                    upperNext = true;
                    continue;
                }
                camel.append(upperNext ? Character.toUpperCase(c) : c);
                upperNext = false;
            }
            camel.append("HealthIndicator");
            return camel.toString();
        }
    }
}
