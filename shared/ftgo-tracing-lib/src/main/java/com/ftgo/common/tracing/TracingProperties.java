package com.ftgo.common.tracing;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for distributed tracing.
 *
 * <p>Configure in application.yml:
 * <pre>
 * ftgo:
 *   tracing:
 *     service-name: ${spring.application.name}
 *     zipkin-endpoint: http://localhost:9411/api/v2/spans
 *     sampling-rate: 1.0
 *     propagation-type: B3
 * </pre>
 */
@ConfigurationProperties(prefix = "ftgo.tracing")
public class TracingProperties {

    /**
     * Service name reported to the trace collector.
     * Defaults to the Spring application name.
     */
    private String serviceName;

    /**
     * Zipkin collector endpoint for span reporting.
     */
    private String zipkinEndpoint = "http://localhost:9411/api/v2/spans";

    /**
     * Sampling rate: 1.0 = 100% (dev), 0.1 = 10% (prod).
     */
    private float samplingRate = 1.0f;

    /**
     * Trace context propagation type: B3 or W3C.
     */
    private String propagationType = "B3";

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getZipkinEndpoint() { return zipkinEndpoint; }
    public void setZipkinEndpoint(String zipkinEndpoint) { this.zipkinEndpoint = zipkinEndpoint; }

    public float getSamplingRate() { return samplingRate; }
    public void setSamplingRate(float samplingRate) { this.samplingRate = samplingRate; }

    public String getPropagationType() { return propagationType; }
    public void setPropagationType(String propagationType) { this.propagationType = propagationType; }
}
