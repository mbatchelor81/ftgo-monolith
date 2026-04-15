package com.ftgo.observability.config;

import brave.Tracing;
import brave.handler.SpanHandler;
import brave.propagation.B3Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

/**
 * Auto-configuration for distributed tracing across all FTGO services.
 *
 * <p>Sets up Brave-based tracing with B3 propagation and Zipkin export. Sampling rate is
 * configurable via {@code management.tracing.sampling.probability} (default 1.0 for dev, 0.1 for
 * prod).
 */
@Configuration
@ConditionalOnClass(Tracing.class)
public class TracingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public URLConnectionSender zipkinSender(
            @Value("${management.zipkin.tracing.endpoint:http://localhost:9411/api/v2/spans}")
                    String endpoint) {
        return URLConnectionSender.create(endpoint);
    }

    @Bean
    @ConditionalOnMissingBean(SpanHandler.class)
    public AsyncZipkinSpanHandler zipkinSpanHandler(URLConnectionSender sender) {
        return AsyncZipkinSpanHandler.create(sender);
    }

    @Bean
    @ConditionalOnMissingBean
    public Tracing braveTracing(
            @Value("${spring.application.name:unknown}") String serviceName,
            @Value("${management.tracing.sampling.probability:1.0}") float samplingProbability,
            SpanHandler spanHandler) {
        return Tracing.newBuilder()
                .localServiceName(serviceName)
                .propagationFactory(
                        B3Propagation.newFactoryBuilder()
                                .injectFormat(B3Propagation.Format.MULTI)
                                .build())
                .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder().build())
                .sampler(Sampler.create(samplingProbability))
                .addSpanHandler(spanHandler)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public brave.Tracer braveNativeTracer(Tracing tracing) {
        return tracing.tracer();
    }

    @Bean
    @ConditionalOnMissingBean(io.micrometer.tracing.Tracer.class)
    public BraveTracer micrometerTracer(Tracing tracing) {
        BraveCurrentTraceContext braveCurrentTraceContext =
                new BraveCurrentTraceContext(tracing.currentTraceContext());
        return new BraveTracer(
                tracing.tracer(), braveCurrentTraceContext, new BraveBaggageManager());
    }
}
