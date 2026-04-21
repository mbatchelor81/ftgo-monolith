package net.chrisrichardson.ftgo.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import jakarta.servlet.Filter;

/**
 * Spring configuration that registers {@link CorrelationIdFilter} at the
 * highest precedence so every downstream filter and controller sees the
 * correlation ID in MDC.
 *
 * <p>Activated only on servlet-based web applications so non-web modules
 * that depend on ftgo-logging (e.g. shared libraries reusing
 * {@link MdcKeys}) don't pull in the filter.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Filter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CorrelationIdConfiguration {

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration() {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorrelationIdFilter());
        registration.addUrlPatterns("/*");
        registration.setName("correlationIdFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
