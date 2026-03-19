package com.ftgo.common.security.rbac;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom {@link PermissionEvaluator} that validates resource ownership.
 *
 * <p>Supports ownership checks in SpEL expressions:
 * {@code @PreAuthorize("hasPermission(#consumerId, 'Consumer', 'read')")}
 *
 * <p>The evaluator extracts the authenticated user's ID from the principal
 * and compares it against the target resource ID. ADMIN role always passes.
 */
@Component
public class ResourceOwnershipEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || permission == null) {
            return false;
        }
        if (hasAdminRole(authentication)) {
            return true;
        }
        String targetId = String.valueOf(targetDomainObject);
        String principalId = extractPrincipalId(authentication);
        return targetId.equals(principalId);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                 String targetType, Object permission) {
        if (authentication == null || targetId == null || targetType == null) {
            return false;
        }
        if (hasAdminRole(authentication)) {
            return true;
        }
        String resourceId = String.valueOf(targetId);
        String principalId = extractPrincipalId(authentication);
        return resourceId.equals(principalId);
    }

    private boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private String extractPrincipalId(Authentication authentication) {
        return authentication.getName();
    }
}
