package com.ftgo.common.security.rbac;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Configures method-level security with role hierarchy and custom permission evaluation.
 *
 * <p>Enabled via {@code ftgo.security.method-security.enabled=true} (default: true).
 *
 * <p>Role hierarchy:
 * <pre>
 *   ADMIN > RESTAURANT_OWNER > COURIER > CUSTOMER
 * </pre>
 */
@Configuration
@EnableMethodSecurity
@ConditionalOnProperty(prefix = "ftgo.security.method-security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MethodSecurityConfiguration {

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(
                "ROLE_ADMIN > ROLE_RESTAURANT_OWNER\n" +
                "ROLE_RESTAURANT_OWNER > ROLE_COURIER\n" +
                "ROLE_COURIER > ROLE_CUSTOMER"
        );
        return hierarchy;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            RoleHierarchy roleHierarchy,
            ResourceOwnershipEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
}
