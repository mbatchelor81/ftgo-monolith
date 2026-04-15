package com.ftgo.security.authorization;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * Configures the role hierarchy for FTGO services.
 *
 * <p>The hierarchy ensures that higher-privilege roles automatically inherit the authorities of
 * lower-privilege roles:
 *
 * <pre>
 *   ROLE_ADMIN > ROLE_RESTAURANT_OWNER
 *   ROLE_ADMIN > ROLE_COURIER
 *   ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER
 * </pre>
 *
 * <p>This means an ADMIN user inherits all permissions of RESTAURANT_OWNER, COURIER, and CUSTOMER.
 * A RESTAURANT_OWNER inherits CUSTOMER permissions.
 */
@Configuration
public class RoleHierarchyConfiguration {

    /** Expression defining the role inheritance tree. */
    static final String HIERARCHY_EXPRESSION =
            "ROLE_ADMIN > ROLE_RESTAURANT_OWNER\n"
                    + "ROLE_ADMIN > ROLE_COURIER\n"
                    + "ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER";

    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("deprecation")
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(HIERARCHY_EXPRESSION);
        return hierarchy;
    }
}
