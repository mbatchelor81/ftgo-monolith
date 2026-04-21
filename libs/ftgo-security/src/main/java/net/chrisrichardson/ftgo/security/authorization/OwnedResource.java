package net.chrisrichardson.ftgo.security.authorization;

/**
 * Marker contract for domain objects whose access is restricted to the user
 * that owns them.
 *
 * <p>{@link ResourceOwnershipPermissionEvaluator} uses {@link #getOwnerId()}
 * to compare the resource's owner against the {@code user_id} claim on the
 * current JWT. Any service that exposes resources tied to a user (consumer
 * profiles, courier profiles, orders, …) should make its JPA entity or DTO
 * implement this interface so {@code @PreAuthorize("hasPermission(returnObject, 'own')")}
 * works without bespoke plumbing.
 */
public interface OwnedResource {

    /**
     * Return the stable identifier of the user that owns this resource.
     *
     * <p>Must match the {@code user_id} claim written into FTGO access
     * tokens. Return {@code null} for resources that are genuinely
     * unowned — the evaluator will simply deny the ownership check.
     */
    String getOwnerId();
}
