package com.ftgo.security.authorization;

import com.ftgo.security.jwt.FtgoUserDetails;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Custom {@link PermissionEvaluator} that checks both role-based permissions and resource ownership
 * for FTGO services.
 *
 * <p>Supports two Spring Security expression modes:
 *
 * <ol>
 *   <li>{@code hasPermission(authentication, targetDomainObject, permission)} — checks whether the
 *       user's roles grant the requested permission.
 *   <li>{@code hasPermission(authentication, targetId, targetType, permission)} — additionally
 *       checks resource ownership when the permission ends with {@code :own}.
 * </ol>
 *
 * <p>Usage in {@code @PreAuthorize}:
 *
 * <pre>{@code
 * @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'Order', 'read:own')")
 * public Order findById(Long id) { ... }
 * }</pre>
 *
 * @see ResourceOwnershipResolver
 * @see RolePermissionMapping
 */
public class FtgoPermissionEvaluator implements PermissionEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(FtgoPermissionEvaluator.class);

    private final List<ResourceOwnershipResolver> ownershipResolvers;

    public FtgoPermissionEvaluator(List<ResourceOwnershipResolver> ownershipResolvers) {
        this.ownershipResolvers = ownershipResolvers != null ? ownershipResolvers : List.of();
    }

    @Override
    public boolean hasPermission(
            Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        return hasRoleBasedPermission(authentication, permission.toString());
    }

    @Override
    public boolean hasPermission(
            Authentication authentication,
            Serializable targetId,
            String targetType,
            Object permission) {
        if (authentication == null || targetType == null || permission == null) {
            return false;
        }
        String permissionStr = permission.toString();

        // For ownership-scoped permissions (ending with :own), verify role + ownership
        if (permissionStr.endsWith(":own")) {
            if (!hasRoleBasedPermission(authentication, permissionStr)) {
                return false;
            }
            String userId = extractUserId(authentication);
            if (userId == null) {
                LOG.debug("Cannot check ownership — no user ID in authentication");
                return false;
            }
            return checkOwnership(userId, targetId, targetType);
        }

        // For non-ownership permissions, check role-based access only
        return hasRoleBasedPermission(authentication, permissionStr);
    }

    private boolean hasRoleBasedPermission(Authentication authentication, String permission) {
        // Check direct authority match (permission explicitly in JWT)
        boolean hasDirect =
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(permission));
        if (hasDirect) {
            return true;
        }

        // Check if any of the user's roles grant this permission via mapping
        Set<String> userRoles = extractRoles(authentication);

        // ADMIN has all permissions
        if (userRoles.contains(FtgoRole.ADMIN.name())) {
            return true;
        }

        for (String role : userRoles) {
            Set<String> rolePermissions = RolePermissionMapping.getPermissions(role);
            if (rolePermissions.contains(permission)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkOwnership(String userId, Serializable resourceId, String resourceType) {
        for (ResourceOwnershipResolver resolver : ownershipResolvers) {
            if (resolver.supports(resourceType)) {
                boolean isOwner = resolver.isOwner(userId, resourceId, resourceType);
                LOG.debug(
                        "Ownership check for user={}, type={}, id={}: {}",
                        userId,
                        resourceType,
                        resourceId,
                        isOwner);
                return isOwner;
            }
        }
        LOG.warn("No ownership resolver found for resource type: {}", resourceType);
        return false;
    }

    private String extractUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth
                && jwtAuth.getDetails() instanceof FtgoUserDetails details) {
            return details.getUserId();
        }
        return authentication.getName();
    }

    private Set<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .collect(Collectors.toSet());
    }
}
