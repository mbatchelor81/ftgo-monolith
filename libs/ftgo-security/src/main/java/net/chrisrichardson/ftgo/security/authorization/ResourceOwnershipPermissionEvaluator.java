package net.chrisrichardson.ftgo.security.authorization;

import net.chrisrichardson.ftgo.security.jwt.JwtClaimNames;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * FTGO {@link PermissionEvaluator} that implements resource-ownership checks
 * for method-level security.
 *
 * <p>Used from {@code @PreAuthorize} expressions:
 *
 * <pre>{@code
 *   // Using the target id directly (e.g. a userId or an ownerId)
 *   @PreAuthorize("hasPermission(#consumerId, 'consumer', 'own')")
 *   ConsumerDto findById(String consumerId) { ... }
 *
 *   // Using an OwnedResource returned by the method
 *   @PostAuthorize("hasPermission(returnObject, 'own')")
 *   OrderDto getOrder(String id) { ... }
 * }</pre>
 *
 * <p>Evaluation rules:
 * <ol>
 *   <li>Anonymous / unauthenticated requests fail the check.</li>
 *   <li>Callers with the {@code ROLE_ADMIN} authority pass every check —
 *       this is how the "ADMIN inherits all permissions" requirement from
 *       EM-37 is realised for ownership checks (the role hierarchy handles
 *       plain {@code hasRole(...)} expressions).</li>
 *   <li>Only the permission literal {@code "own"} is recognised; other
 *       values fall through to {@code false}. Extending the vocabulary is
 *       deliberately opt-in so new permission verbs stay discoverable.</li>
 *   <li>The caller's {@code user_id} JWT claim must equal the resource's
 *       owner id. {@link OwnedResource} instances expose their owner via
 *       {@link OwnedResource#getOwnerId()}; raw identifiers are compared
 *       via {@link Objects#toString}.</li>
 * </ol>
 */
public class ResourceOwnershipPermissionEvaluator implements PermissionEvaluator {

    /** The permission literal recognised by this evaluator. */
    public static final String OWN_PERMISSION = "own";

    private static final String ADMIN_AUTHORITY = Role.ADMIN.authority();

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (!isAuthenticated(authentication) || targetDomainObject == null || permission == null) {
            return false;
        }
        if (hasAdminAuthority(authentication)) {
            return true;
        }
        if (!OWN_PERMISSION.equals(permission.toString())) {
            return false;
        }
        return currentUserId(authentication)
                .map(userId -> userId.equals(extractOwnerId(targetDomainObject)))
                .orElse(false);
    }

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {
        return hasPermission(authentication, targetId, permission);
    }

    private static boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());
    }

    private static boolean hasAdminAuthority(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (ADMIN_AUTHORITY.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private static Optional<String> currentUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.ofNullable(jwtAuth.getToken().getClaimAsString(JwtClaimNames.USER_ID));
        }
        return Optional.empty();
    }

    private static String extractOwnerId(Object target) {
        if (target instanceof OwnedResource owned) {
            String ownerId = owned.getOwnerId();
            return ownerId == null ? null : ownerId;
        }
        return Objects.toString(target, null);
    }
}
