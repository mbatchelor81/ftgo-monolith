package net.chrisrichardson.ftgo.logging;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Registers {@link LoggingAspect} so every Spring {@code @Service} /
 * {@code @Repository} bean automatically logs method entry / exit at
 * DEBUG and failures at ERROR.
 *
 * <p>The aspect is only contributed when:
 * <ul>
 *   <li>AspectJ is on the classpath (i.e. the service applies
 *       {@code ftgo.observability-conventions} or pulls in
 *       {@code spring-boot-starter-aop} directly), and</li>
 *   <li>{@code ftgo.logging.aspect.enabled} is not explicitly set to
 *       {@code false} (default: enabled).</li>
 * </ul>
 *
 * <p>Non-AOP consumers of {@code ftgo-logging} never trigger this
 * configuration because {@code Aspect} will be absent from their
 * classpath.
 */
@AutoConfiguration
@ConditionalOnClass(Aspect.class)
@ConditionalOnProperty(prefix = "ftgo.logging.aspect", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

    @Bean
    @ConditionalOnMissingBean(LoggingAspect.class)
    public LoggingAspect ftgoLoggingAspect() {
        return new LoggingAspect();
    }
}
