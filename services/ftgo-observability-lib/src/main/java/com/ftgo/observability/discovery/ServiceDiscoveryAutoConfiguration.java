package com.ftgo.observability.discovery;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for Kubernetes-native DNS-based service discovery.
 *
 * <p>Enables {@link ServiceDiscoveryProperties} so that each FTGO service can resolve downstream
 * service URLs using Kubernetes DNS conventions. Services can override URLs for local development
 * via the {@code ftgo.discovery.services} map in their {@code application.yml}.
 */
@Configuration
@EnableConfigurationProperties(ServiceDiscoveryProperties.class)
public class ServiceDiscoveryAutoConfiguration {}
