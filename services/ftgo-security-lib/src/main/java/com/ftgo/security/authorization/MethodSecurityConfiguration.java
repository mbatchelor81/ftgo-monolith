package com.ftgo.security.authorization;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables method-level security across all FTGO services.
 *
 * <p>Configures Spring's {@code @PreAuthorize} / {@code @PostAuthorize} annotations with:
 *
 * <ul>
 *   <li>A custom {@link FtgoPermissionEvaluator} for ownership-based permission checks
 *   <li>The {@link RoleHierarchy} for privilege inheritance
 * </ul>
 *
 * <p>Example usage in service classes:
 *
 * <pre>{@code
 * @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'Order', 'order:read:own')")
 * public Order findOrderById(Long id) { ... }
 *
 * @PreAuthorize("hasRole('CUSTOMER')")
 * public Order createOrder(CreateOrderRequest request) { ... }
 * }</pre>
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfiguration {

    private final RoleHierarchy roleHierarchy;

    public MethodSecurityConfiguration(
            @Autowired(required = false) @Nullable RoleHierarchy roleHierarchy) {
        this.roleHierarchy = roleHierarchy;
    }

    @Bean
    @ConditionalOnMissingBean(PermissionEvaluator.class)
    public FtgoPermissionEvaluator ftgoPermissionEvaluator(
            @Autowired(required = false) List<ResourceOwnershipResolver> resolvers) {
        return new FtgoPermissionEvaluator(resolvers);
    }

    @Bean
    @ConditionalOnMissingBean(MethodSecurityExpressionHandler.class)
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            PermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler =
                new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        if (roleHierarchy != null) {
            handler.setRoleHierarchy(roleHierarchy);
        }
        return handler;
    }
}
