package com.ftgo.security.authorization;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.List;

/**
 * Auto-configuration for FTGO role-based authorization.
 *
 * <p>Enables Spring's method-level security ({@code @PreAuthorize},
 * {@code @PostAuthorize}) and registers the {@link FtgoPermissionEvaluator}
 * to support ownership-based access control via {@code hasPermission()}
 * expressions.
 *
 * <p>Activated when {@code ftgo.security.enabled=true} (the default).
 */
@AutoConfiguration
@EnableMethodSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "ftgo.security.enabled", havingValue = "true", matchIfMissing = true)
public class FtgoAuthorizationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PermissionEvaluator.class)
    public FtgoPermissionEvaluator ftgoPermissionEvaluator(
            List<ResourceOwnershipChecker> checkers) {
        return new FtgoPermissionEvaluator(checkers);
    }

    @Bean
    @ConditionalOnMissingBean(MethodSecurityExpressionHandler.class)
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            PermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
}
