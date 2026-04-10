package com.ftgo.security.authorization;

import com.ftgo.security.jwt.FtgoUserDetails;
import com.ftgo.security.jwt.JwtAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

/**
 * Custom permission evaluator for resource ownership validation.
 *
 * <p>Supports two evaluation modes used in {@code @PreAuthorize} expressions:
 *
 * <h3>1. Simple permission check</h3>
 * <pre>
 * &#64;PreAuthorize("hasPermission(#consumerId, 'Consumer', 'read')")
 * </pre>
 * Checks if the authenticated user owns the resource (targetId matches userId)
 * or has the corresponding fine-grained permission.
 *
 * <h3>2. Object-based permission check</h3>
 * <pre>
 * &#64;PreAuthorize("hasPermission(#order, 'cancel')")
 * </pre>
 *
 * <p>ADMIN role always has access (via role hierarchy in method security).
 * For non-admin users, this evaluator checks ownership by comparing the
 * resource ID against the authenticated user's ID.
 */
public class FtgoPermissionEvaluator implements PermissionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(FtgoPermissionEvaluator.class);

    /**
     * Evaluates whether the user has permission on a target identified by ID and type.
     *
     * <p>Returns {@code true} if:
     * <ul>
     *   <li>The user has ROLE_ADMIN (checked via role hierarchy elsewhere), OR</li>
     *   <li>The targetId (as String) matches the authenticated user's ID
     *       (resource ownership), OR</li>
     *   <li>The user has the fine-grained permission {@code <targetType>:<permission>}
     *       (case-insensitive target type)</li>
     * </ul>
     *
     * @param authentication the current authentication
     * @param targetId       the resource identifier (e.g., consumerId, orderId)
     * @param targetType     the resource type (e.g., "Consumer", "Order")
     * @param permission     the action (e.g., "read", "cancel")
     * @return {@code true} if access is granted
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                  String targetType, Object permission) {
        if (authentication == null || targetType == null || permission == null) {
            return false;
        }

        FtgoUserDetails userDetails = extractUserDetails(authentication);
        if (userDetails == null) {
            log.debug("Permission denied: no FtgoUserDetails in authentication");
            return false;
        }

        // Check ADMIN role
        if (userDetails.hasRole("ROLE_ADMIN")) {
            log.debug("Permission granted: user {} has ADMIN role", userDetails.getUserId());
            return true;
        }

        // Check resource ownership (targetId matches userId)
        if (targetId != null && String.valueOf(targetId).equals(userDetails.getUserId())) {
            log.debug("Permission granted: user {} owns resource {}:{}",
                userDetails.getUserId(), targetType, targetId);
            return true;
        }

        // Check fine-grained permission
        String requiredPermission = targetType.toLowerCase() + ":" + permission;
        if (userDetails.hasPermission(requiredPermission)) {
            log.debug("Permission granted: user {} has permission {}",
                userDetails.getUserId(), requiredPermission);
            return true;
        }

        log.debug("Permission denied: user {} lacks permission on {}:{} (action={})",
            userDetails.getUserId(), targetType, targetId, permission);
        return false;
    }

    /**
     * Evaluates permission on a domain object. Not currently used in FTGO.
     * Always returns {@code false} — use the ID-based overload instead.
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject,
                                  Object permission) {
        log.debug("Object-based permission evaluation not supported; returning false");
        return false;
    }

    private FtgoUserDetails extractUserDetails(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getUserDetails();
        }
        return null;
    }
}
