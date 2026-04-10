package com.ftgo.security.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Configures role hierarchy and method-level security for the FTGO platform.
 *
 * <h3>Role Hierarchy</h3>
 * <pre>
 *   ROLE_ADMIN &gt; ROLE_RESTAURANT_OWNER
 *   ROLE_ADMIN &gt; ROLE_COURIER
 *   ROLE_RESTAURANT_OWNER &gt; ROLE_CUSTOMER
 *   ROLE_COURIER &gt; ROLE_CUSTOMER
 * </pre>
 *
 * <p>This means ADMIN inherits all permissions of RESTAURANT_OWNER, COURIER,
 * and CUSTOMER. RESTAURANT_OWNER and COURIER both inherit CUSTOMER permissions.
 *
 * <p>{@code @EnableMethodSecurity} activates {@code @PreAuthorize} and
 * {@code @PostAuthorize} annotations on service methods.
 */
@Configuration
@EnableMethodSecurity
public class RoleHierarchyConfig {

    private static final Logger log = LoggerFactory.getLogger(RoleHierarchyConfig.class);

    /**
     * Defines the role hierarchy for the FTGO platform.
     *
     * <p>The hierarchy ensures that higher-privileged roles inherit the
     * permissions of lower-privileged roles:
     * <ul>
     *   <li>ADMIN &gt; RESTAURANT_OWNER &gt; CUSTOMER</li>
     *   <li>ADMIN &gt; COURIER &gt; CUSTOMER</li>
     * </ul>
     */
    @Bean
    @SuppressWarnings("deprecation")
    public RoleHierarchy roleHierarchy() {
        log.info("Configuring FTGO role hierarchy: ADMIN > RESTAURANT_OWNER > CUSTOMER, ADMIN > COURIER > CUSTOMER");
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(
            "ROLE_ADMIN > ROLE_RESTAURANT_OWNER\n" +
            "ROLE_ADMIN > ROLE_COURIER\n" +
            "ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER\n" +
            "ROLE_COURIER > ROLE_CUSTOMER"
        );
        return hierarchy;
    }

    /**
     * Creates the custom permission evaluator for resource ownership validation.
     */
    @Bean
    public FtgoPermissionEvaluator ftgoPermissionEvaluator() {
        return new FtgoPermissionEvaluator();
    }

    /**
     * Configures the method security expression handler to use the role hierarchy.
     * This allows {@code @PreAuthorize("hasRole('CUSTOMER')")} to match ADMIN users
     * through the hierarchy.
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            RoleHierarchy roleHierarchy,
            FtgoPermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
}
