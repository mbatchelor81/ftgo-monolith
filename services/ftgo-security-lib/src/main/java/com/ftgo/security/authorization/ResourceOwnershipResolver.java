package com.ftgo.security.authorization;

import java.io.Serializable;

/**
 * Strategy interface for resolving resource ownership.
 *
 * <p>Each service provides implementations for its domain entities so that the {@link
 * FtgoPermissionEvaluator} can verify whether the authenticated user owns a specific resource.
 *
 * <p>Example implementation for the Order Service:
 *
 * <pre>{@code
 * @Component
 * public class OrderOwnershipResolver implements ResourceOwnershipResolver {
 *
 *     private final OrderRepository orderRepository;
 *
 *     public OrderOwnershipResolver(OrderRepository orderRepository) {
 *         this.orderRepository = orderRepository;
 *     }
 *
 *     @Override
 *     public boolean supports(String resourceType) {
 *         return "Order".equals(resourceType);
 *     }
 *
 *     @Override
 *     public boolean isOwner(String userId, Serializable resourceId, String resourceType) {
 *         return orderRepository.findById((Long) resourceId)
 *             .map(order -> userId.equals(String.valueOf(order.getConsumerId())))
 *             .orElse(false);
 *     }
 * }
 * }</pre>
 */
public interface ResourceOwnershipResolver {

    /**
     * Returns {@code true} if the given user owns the specified resource.
     *
     * @param userId the authenticated user's identifier
     * @param resourceId the identifier of the target resource
     * @param resourceType the type name of the resource (e.g., {@code "Order"})
     * @return {@code true} if the user owns the resource
     */
    boolean isOwner(String userId, Serializable resourceId, String resourceType);

    /**
     * Returns {@code true} if this resolver handles the given resource type.
     *
     * @param resourceType the type name to check
     * @return {@code true} if this resolver supports the type
     */
    boolean supports(String resourceType);
}
