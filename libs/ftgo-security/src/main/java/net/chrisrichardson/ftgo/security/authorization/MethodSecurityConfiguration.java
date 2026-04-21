package net.chrisrichardson.ftgo.security.authorization;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables {@link EnableMethodSecurity @EnableMethodSecurity} across every
 * FTGO service and wires the shared {@link RoleHierarchy}, custom
 * {@link PermissionEvaluator}, and the expression handler that binds them
 * to {@code @PreAuthorize} / {@code @PostAuthorize} evaluation.
 *
 * <p>Role hierarchy (EM-37 acceptance criterion "ADMIN inherits all
 * permissions"):
 *
 * <pre>{@code
 *   ROLE_ADMIN            > ROLE_RESTAURANT_OWNER
 *   ROLE_ADMIN            > ROLE_COURIER
 *   ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER
 *   ROLE_COURIER          > ROLE_CUSTOMER
 * }</pre>
 *
 * <p>Imported automatically by every service via
 * {@link net.chrisrichardson.ftgo.security.FtgoSecurityAutoConfiguration}.
 * Services can override any bean by declaring their own implementation.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfiguration {

    /**
     * Hierarchy expression used by the {@link RoleHierarchy} bean. Exposed as
     * a constant so tests can assert on the exact hierarchy without
     * duplicating the string literal.
     */
    public static final String ROLE_HIERARCHY = String.join("\n",
            "ROLE_ADMIN > ROLE_RESTAURANT_OWNER",
            "ROLE_ADMIN > ROLE_COURIER",
            "ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER",
            "ROLE_COURIER > ROLE_CUSTOMER");

    @Bean
    @ConditionalOnMissingBean
    public RoleHierarchy ftgoRoleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(ROLE_HIERARCHY);
        return hierarchy;
    }

    @Bean
    @ConditionalOnMissingBean
    public PermissionEvaluator ftgoPermissionEvaluator() {
        return new ResourceOwnershipPermissionEvaluator();
    }

    /**
     * Expression handler used by Spring Security's method-security machinery.
     * Declared {@code static} because it must be wired before any bean whose
     * methods carry {@code @PreAuthorize} is post-processed — otherwise the
     * default handler (without the role hierarchy / permission evaluator)
     * wins for those beans.
     */
    @Bean
    @ConditionalOnMissingBean(MethodSecurityExpressionHandler.class)
    static MethodSecurityExpressionHandler ftgoMethodSecurityExpressionHandler(
            RoleHierarchy roleHierarchy, PermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
}
