package com.ftgo.common.tracing;

import brave.Tracing;
import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagationConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.B3Propagation;
import brave.propagation.CurrentTraceContext;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.brave.ZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

/**
 * Auto-configuration for distributed tracing using Micrometer Tracing with Brave bridge
 * and Zipkin reporter.
 *
 * <p>Provides:
 * <ul>
 *   <li>Brave {@link Tracing} configured with B3 propagation and Zipkin reporting</li>
 *   <li>MDC-based trace context for logging (traceId, spanId in log lines)</li>
 *   <li>Configurable sampling rate per environment</li>
 *   <li>Micrometer {@link BraveTracer} bridge for Spring integration</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(TracingProperties.class)
public class TracingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public URLConnectionSender zipkinSender(TracingProperties properties) {
        return URLConnectionSender.create(properties.getZipkinEndpoint());
    }

    @Bean
    @ConditionalOnMissingBean
    public AsyncReporter<zipkin2.Span> zipkinReporter(URLConnectionSender sender) {
        return AsyncReporter.create(sender);
    }

    @Bean
    @ConditionalOnMissingBean
    public ZipkinSpanHandler zipkinSpanHandler(AsyncReporter<zipkin2.Span> reporter) {
        return (ZipkinSpanHandler) ZipkinSpanHandler.create(reporter);
    }

    @Bean
    @ConditionalOnMissingBean
    public CurrentTraceContext currentTraceContext() {
        return ThreadLocalCurrentTraceContext.newBuilder()
                .addScopeDecorator(MDCScopeDecorator.get())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Tracing braveTracing(
            TracingProperties properties,
            ZipkinSpanHandler spanHandler,
            CurrentTraceContext currentTraceContext) {

        String serviceName = properties.getServiceName() != null
                ? properties.getServiceName()
                : "ftgo-service";

        return Tracing.newBuilder()
                .localServiceName(serviceName)
                .currentTraceContext(currentTraceContext)
                .addSpanHandler(spanHandler)
                .propagationFactory(BaggagePropagation.newFactoryBuilder(
                        B3Propagation.FACTORY)
                        .build())
                .sampler(Sampler.create(properties.getSamplingRate()))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public io.micrometer.tracing.Tracer micrometerTracer(Tracing braveTracing,
                                                          CurrentTraceContext currentTraceContext) {
        brave.Tracer braveTracer = braveTracing.tracer();
        BraveCurrentTraceContext bridgeContext = new BraveCurrentTraceContext(currentTraceContext);
        return new BraveTracer(braveTracer, bridgeContext, new BraveBaggageManager());
    }
}
