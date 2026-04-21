package net.chrisrichardson.ftgo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.security.jwt.JwtAuthenticationConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Spring Boot auto-configuration for FTGO shared security.
 *
 * <p>Any service that declares {@code implementation project(':libs:ftgo-security')}
 * picks up {@link BaseSecurityConfiguration} and the JSON
 * {@link SecurityExceptionHandler} beans automatically. Services can override
 * either by defining their own bean of the same name / type.
 */
@AutoConfiguration
@ConditionalOnClass(EnableWebSecurity.class)
@ConditionalOnWebApplication
@Import({BaseSecurityConfiguration.class, JwtAuthenticationConfiguration.class})
public class FtgoSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SecurityExceptionHandler ftgoSecurityExceptionHandler(ObjectMapper objectMapper) {
        return new SecurityExceptionHandler(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationEntryPoint.class)
    public AuthenticationEntryPoint ftgoAuthenticationEntryPoint(SecurityExceptionHandler handler) {
        return handler;
    }

    @Bean
    @ConditionalOnMissingBean(AccessDeniedHandler.class)
    public AccessDeniedHandler ftgoAccessDeniedHandler(SecurityExceptionHandler handler) {
        return handler;
    }
}
