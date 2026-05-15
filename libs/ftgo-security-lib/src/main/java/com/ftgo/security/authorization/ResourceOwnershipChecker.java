package com.ftgo.security.authorization;

import java.io.Serializable;

/**
 * Strategy interface for verifying resource ownership.
 *
 * <p>Implementations determine whether a given user (identified by their
 * JWT {@code sub} claim) owns a specific domain resource. Services register
 * beans implementing this interface; the {@link FtgoPermissionEvaluator}
 * delegates ownership checks to the matching checker.
 *
 * <p>Example implementation in an order service:
 * <pre>{@code
 * @Component
 * public class OrderOwnershipChecker implements ResourceOwnershipChecker {
 *     private final OrderRepository repository;
 *
 *     public OrderOwnershipChecker(OrderRepository repository) {
 *         this.repository = repository;
 *     }
 *
 *     @Override
 *     public String getTargetType() { return "Order"; }
 *
 *     @Override
 *     public boolean isOwner(String userId, Serializable resourceId) {
 *         return repository.findById((Long) resourceId)
 *                 .map(order -> order.getConsumerId().toString().equals(userId))
 *                 .orElse(false);
 *     }
 * }
 * }</pre>
 */
public interface ResourceOwnershipChecker {

    /**
     * The domain type this checker handles (e.g. {@code "Order"}, {@code "Restaurant"}).
     * Must match the {@code targetType} argument in
     * {@code @PreAuthorize("hasPermission(#id, 'Order', 'order:read')")}.
     */
    String getTargetType();

    /**
     * Returns {@code true} if the user identified by {@code userId} owns
     * the resource identified by {@code resourceId}.
     *
     * @param userId     the authenticated user's identifier (JWT {@code sub} claim)
     * @param resourceId the domain resource identifier
     * @return {@code true} if the user owns the resource
     */
    boolean isOwner(String userId, Serializable resourceId);
}
