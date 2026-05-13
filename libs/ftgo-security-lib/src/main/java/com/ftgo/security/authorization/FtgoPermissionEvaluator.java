package com.ftgo.security.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Custom {@link PermissionEvaluator} for the FTGO platform.
 *
 * <p>Supports two evaluation modes used in {@code @PreAuthorize} expressions:
 *
 * <ol>
 *   <li><b>Authority-only</b> — {@code hasPermission(null, 'order:create')}
 *       checks that the authenticated principal holds the given permission
 *       authority.</li>
 *   <li><b>Ownership + authority</b> —
 *       {@code hasPermission(#orderId, 'Order', 'order:read')} first verifies
 *       the authority, then delegates to a registered
 *       {@link ResourceOwnershipChecker} to confirm that the user owns the
 *       resource. ADMIN users bypass the ownership check.</li>
 * </ol>
 */
public class FtgoPermissionEvaluator implements PermissionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(FtgoPermissionEvaluator.class);

    private final Map<String, ResourceOwnershipChecker> checkersByType;

    public FtgoPermissionEvaluator(List<ResourceOwnershipChecker> checkers) {
        this.checkersByType = (checkers == null)
                ? Collections.emptyMap()
                : checkers.stream().collect(Collectors.toMap(
                        ResourceOwnershipChecker::getTargetType,
                        Function.identity()));
    }

    /**
     * Authority-only check: {@code hasPermission(targetDomainObject, permission)}.
     *
     * <p>When {@code targetDomainObject} is {@code null} this becomes a pure
     * authority check. When non-null, it is treated as the resource id and
     * no ownership check is performed (use the three-arg variant for ownership).
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        return hasAuthority(authentication, permission.toString());
    }

    /**
     * Ownership + authority check:
     * {@code hasPermission(targetId, targetType, permission)}.
     *
     * <p>Verifies the user holds the required permission authority. Then, unless
     * the user is an ADMIN, delegates to the {@link ResourceOwnershipChecker}
     * registered for {@code targetType}.
     */
    @Override
    public boolean hasPermission(Authentication authentication,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {
        if (authentication == null || targetType == null || permission == null) {
            return false;
        }

        if (!hasAuthority(authentication, permission.toString())) {
            return false;
        }

        if (isAdmin(authentication)) {
            return true;
        }

        if (targetId == null) {
            return true;
        }

        ResourceOwnershipChecker checker = checkersByType.get(targetType);
        if (checker == null) {
            log.warn("No ResourceOwnershipChecker registered for type '{}'; denying access", targetType);
            return false;
        }

        String userId = authentication.getName();
        return checker.isOwner(userId, targetId);
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(authority));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(FtgoRole.ADMIN.authority()::equals);
    }
}
