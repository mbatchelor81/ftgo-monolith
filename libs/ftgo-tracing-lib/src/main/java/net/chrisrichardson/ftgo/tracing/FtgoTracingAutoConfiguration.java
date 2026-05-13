package net.chrisrichardson.ftgo.tracing;

import brave.Tracing;
import brave.handler.SpanHandler;
import brave.propagation.B3Propagation;
import brave.propagation.Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.brave.ZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@AutoConfiguration
@ConditionalOnClass(Tracer.class)
@ConditionalOnProperty(name = "ftgo.tracing.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FtgoTracingProperties.class)
public class FtgoTracingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public URLConnectionSender zipkinSender(FtgoTracingProperties properties) {
        return URLConnectionSender.create(properties.getZipkin().getEndpoint());
    }

    @Bean
    @ConditionalOnMissingBean
    public AsyncReporter<zipkin2.Span> zipkinReporter(URLConnectionSender sender) {
        return AsyncReporter.create(sender);
    }

    @Bean
    @ConditionalOnMissingBean(name = "zipkinSpanHandler")
    public SpanHandler zipkinSpanHandler(AsyncReporter<zipkin2.Span> reporter) {
        return ZipkinSpanHandler.create(reporter);
    }

    @Bean
    @ConditionalOnMissingBean
    public Tracing braveTracing(FtgoTracingProperties properties, SpanHandler spanHandler) {
        Propagation.Factory propagationFactory = resolvePropagation(properties);
        return Tracing.newBuilder()
                .localServiceName(properties.getServiceName())
                .propagationFactory(propagationFactory)
                .sampler(Sampler.create(properties.getSamplingProbability()))
                .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder().build())
                .addSpanHandler(spanHandler)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public brave.Tracer braveTracer(Tracing tracing) {
        return tracing.tracer();
    }

    @Bean
    @ConditionalOnMissingBean(Tracer.class)
    public BraveTracer micrometerTracer(brave.Tracer braveTracer, Tracing tracing) {
        return new BraveTracer(
                braveTracer,
                new BraveCurrentTraceContext(tracing.currentTraceContext()),
                new BraveBaggageManager());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
    public TracingInterceptor tracingInterceptor(Tracer tracer) {
        return new TracingInterceptor(tracer);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
    public TracingWebMvcConfigurer tracingWebMvcConfigurer(TracingInterceptor interceptor) {
        return new TracingWebMvcConfigurer(interceptor);
    }

    private Propagation.Factory resolvePropagation(FtgoTracingProperties properties) {
        if (properties.getPropagation().getType() == FtgoTracingProperties.PropagationType.B3) {
            return B3Propagation.newFactoryBuilder()
                    .injectFormat(B3Propagation.Format.MULTI)
                    .build();
        }
        return W3CTraceContextPropagation.FACTORY;
    }
}
