package com.ftgo.resilience.config;

import com.ftgo.resilience.discovery.KubernetesServiceRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configures Kubernetes-native DNS-based service discovery.
 *
 * <p>In Kubernetes, each Service gets a DNS entry of the form:
 * {@code <service-name>.<namespace>.svc.cluster.local}
 *
 * <p>This configuration provides a {@link KubernetesServiceRegistry} bean that
 * resolves service names to their cluster-internal URLs, eliminating the need
 * for a separate service discovery tool (e.g., Eureka, Consul).
 */
@Configuration
@EnableConfigurationProperties(ServiceDiscoveryProperties.class)
public class ServiceDiscoveryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public KubernetesServiceRegistry kubernetesServiceRegistry(ServiceDiscoveryProperties properties) {
        return new KubernetesServiceRegistry(properties);
    }
}
