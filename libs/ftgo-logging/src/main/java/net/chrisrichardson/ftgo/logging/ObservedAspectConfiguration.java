package net.chrisrichardson.ftgo.logging;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Registers {@link ObservedAspect} so Spring beans that carry the
 * {@code @Observed} annotation produce Micrometer Observation spans
 * (which the Brave bridge turns into Zipkin spans — see EM-42).
 *
 * <p>The aspect is only contributed when:
 * <ul>
 *   <li>{@code io.micrometer:micrometer-observation} is on the classpath
 *       (i.e. the service applies
 *       {@code ftgo.observability-conventions}), and</li>
 *   <li>Spring has already published an {@link ObservationRegistry}
 *       bean — which Spring Boot does whenever a
 *       {@code micrometer-tracing-bridge-*} library is detected.</li>
 * </ul>
 *
 * <p>Non-tracing consumers of {@code ftgo-logging} (e.g. the bare CLI
 * utilities that only reuse {@link MdcKeys}) never trigger this
 * configuration because both {@link ObservationRegistry} and
 * {@link ObservedAspect} will be absent from their classpath.
 */
@AutoConfiguration(afterName = "org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration")
@ConditionalOnClass({ObservedAspect.class, ObservationRegistry.class})
@ConditionalOnBean(ObservationRegistry.class)
public class ObservedAspectConfiguration {

    @Bean
    @ConditionalOnMissingBean(ObservedAspect.class)
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
