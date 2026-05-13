package net.chrisrichardson.ftgo.tracing;

import brave.Tracing;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import zipkin2.reporter.AsyncReporter;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoTracingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FtgoTracingAutoConfiguration.class));

    @Test
    void autoConfigurationRegistersTracingBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(Tracing.class);
            assertThat(context).hasSingleBean(BraveTracer.class);
            assertThat(context).hasSingleBean(AsyncReporter.class);
        });
    }

    @Test
    void disabledWhenPropertySetToFalse() {
        contextRunner
                .withPropertyValues("ftgo.tracing.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(Tracer.class);
                    assertThat(context).doesNotHaveBean(Tracing.class);
                });
    }

    @Test
    void customServiceNameIsApplied() {
        contextRunner
                .withPropertyValues("ftgo.tracing.service-name=order-service")
                .run(context -> {
                    Tracing tracing = context.getBean(Tracing.class);
                    assertThat(tracing).isNotNull();
                });
    }

    @Test
    void customSamplingProbabilityIsApplied() {
        contextRunner
                .withPropertyValues("ftgo.tracing.sampling-probability=0.5")
                .run(context -> assertThat(context).hasSingleBean(Tracing.class));
    }

    @Test
    void b3PropagationCanBeSelected() {
        contextRunner
                .withPropertyValues("ftgo.tracing.propagation.type=B3")
                .run(context -> assertThat(context).hasSingleBean(Tracing.class));
    }

    @Test
    void w3cPropagationIsDefault() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(Tracing.class));
    }
}
