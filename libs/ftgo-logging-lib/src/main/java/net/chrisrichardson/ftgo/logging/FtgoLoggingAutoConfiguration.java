package net.chrisrichardson.ftgo.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@AutoConfiguration
@ConditionalOnClass(name = "ch.qos.logback.classic.LoggerContext")
@ConditionalOnProperty(name = "ftgo.logging.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FtgoLoggingProperties.class)
public class FtgoLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "correlationIdFilter")
    @ConditionalOnClass(name = "jakarta.servlet.Filter")
    @ConditionalOnProperty(name = "ftgo.logging.correlation-id-enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorrelationIdFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        registration.setName("correlationIdFilter");
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceContextFilter")
    @ConditionalOnClass(name = "jakarta.servlet.Filter")
    public FilterRegistrationBean<ServiceContextFilter> serviceContextFilter(FtgoLoggingProperties properties) {
        FilterRegistrationBean<ServiceContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ServiceContextFilter(properties.getServiceName()));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.addUrlPatterns("/*");
        registration.setName("serviceContextFilter");
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingStartupListener loggingStartupListener(FtgoLoggingProperties properties) {
        return new LoggingStartupListener(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.aspectj.lang.ProceedingJoinPoint")
    public LoggedAspect loggedAspect() {
        return new LoggedAspect();
    }
}
