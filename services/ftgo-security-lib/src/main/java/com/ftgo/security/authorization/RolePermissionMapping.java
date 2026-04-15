package com.ftgo.security.authorization;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps each {@link FtgoRole} to its default set of {@link FtgoPermission permissions}.
 *
 * <p>This mapping is used by {@link FtgoPermissionEvaluator} to determine whether a user's role
 * grants a specific permission, even when the permission is not explicitly included in the JWT
 * claims.
 *
 * <p><strong>Permission matrix:</strong>
 *
 * <table border="1">
 * <tr><th>Role</th><th>Consumer</th><th>Order</th><th>Restaurant</th><th>Courier</th></tr>
 * <tr><td>CUSTOMER</td>
 *     <td>read:own</td>
 *     <td>create, read:own, cancel:own</td>
 *     <td>read</td>
 *     <td>read:own (track delivery)</td></tr>
 * <tr><td>RESTAURANT_OWNER</td>
 *     <td>-</td>
 *     <td>read, accept, status:update</td>
 *     <td>create, read, update:own, delete</td>
 *     <td>-</td></tr>
 * <tr><td>COURIER</td>
 *     <td>-</td>
 *     <td>read:own (assigned)</td>
 *     <td>-</td>
 *     <td>read:own, update:own:availability, delivery:update</td></tr>
 * <tr><td>ADMIN</td>
 *     <td colspan="4">Full access to all resources</td></tr>
 * </table>
 */
public final class RolePermissionMapping {

    private static final Map<FtgoRole, Set<String>> ROLE_PERMISSIONS;

    static {
        Map<FtgoRole, Set<String>> map = new EnumMap<>(FtgoRole.class);

        map.put(
                FtgoRole.CUSTOMER,
                Set.of(
                        FtgoPermission.CONSUMER_READ_OWN,
                        FtgoPermission.ORDER_CREATE,
                        FtgoPermission.ORDER_READ_OWN,
                        FtgoPermission.ORDER_CANCEL_OWN,
                        FtgoPermission.RESTAURANT_READ,
                        FtgoPermission.COURIER_READ_OWN));

        map.put(
                FtgoRole.RESTAURANT_OWNER,
                Set.of(
                        FtgoPermission.ORDER_READ,
                        FtgoPermission.ORDER_ACCEPT,
                        FtgoPermission.ORDER_STATUS_UPDATE,
                        FtgoPermission.RESTAURANT_CREATE,
                        FtgoPermission.RESTAURANT_READ,
                        FtgoPermission.RESTAURANT_UPDATE_OWN,
                        FtgoPermission.RESTAURANT_DELETE));

        map.put(
                FtgoRole.COURIER,
                Set.of(
                        FtgoPermission.ORDER_READ_OWN,
                        FtgoPermission.COURIER_READ_OWN,
                        FtgoPermission.COURIER_UPDATE_OWN_AVAILABILITY,
                        FtgoPermission.COURIER_DELIVERY_UPDATE));

        // ADMIN: aggregate all permissions from other roles plus admin-only permissions
        Set<String> allPermissions = new LinkedHashSet<>();
        map.values().forEach(allPermissions::addAll);
        allPermissions.add(FtgoPermission.CONSUMER_CREATE);
        allPermissions.add(FtgoPermission.CONSUMER_READ);
        allPermissions.add(FtgoPermission.ORDER_READ);
        allPermissions.add(FtgoPermission.ORDER_CANCEL);
        allPermissions.add(FtgoPermission.ORDER_REVISE);
        allPermissions.add(FtgoPermission.RESTAURANT_UPDATE);
        allPermissions.add(FtgoPermission.COURIER_CREATE);
        allPermissions.add(FtgoPermission.COURIER_READ);
        allPermissions.add(FtgoPermission.COURIER_UPDATE_AVAILABILITY);
        map.put(FtgoRole.ADMIN, Collections.unmodifiableSet(allPermissions));

        ROLE_PERMISSIONS = Collections.unmodifiableMap(map);
    }

    private RolePermissionMapping() {
        // Utility class — no instantiation
    }

    /** Returns the permissions granted to the given role. */
    public static Set<String> getPermissions(FtgoRole role) {
        return ROLE_PERMISSIONS.getOrDefault(role, Collections.emptySet());
    }

    /**
     * Returns the permissions granted to the role identified by name.
     *
     * @param roleName the role name (e.g., {@code "CUSTOMER"})
     * @return permissions for the role, or empty set if the role name is unknown
     */
    public static Set<String> getPermissions(String roleName) {
        try {
            return getPermissions(FtgoRole.valueOf(roleName));
        } catch (IllegalArgumentException e) {
            return Collections.emptySet();
        }
    }

    /** Returns an unmodifiable view of the complete role-to-permissions mapping. */
    public static Map<FtgoRole, Set<String>> getAllMappings() {
        return ROLE_PERMISSIONS;
    }
}
