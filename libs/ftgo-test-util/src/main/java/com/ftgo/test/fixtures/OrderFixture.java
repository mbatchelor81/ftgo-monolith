package com.ftgo.test.fixtures;

import net.chrisrichardson.ftgo.common.Money;

import java.util.List;

/**
 * Immutable test fixture representing an order snapshot for use in
 * service tests.
 *
 * <p>Matches the domain semantics of the legacy {@code Order} entity but
 * stays framework-agnostic: no JPA, no Spring annotations. Service tests
 * that need a persisted row should map this fixture into the service's
 * own {@code @Entity}.
 *
 * @param id           order identity
 * @param consumerId   placing consumer
 * @param restaurantId fulfilling restaurant
 * @param state        lifecycle state (e.g. {@code APPROVED}, {@code ACCEPTED})
 * @param lineItems    ordered items
 * @param orderTotal   computed total at creation time
 */
public record OrderFixture(
        Long id,
        Long consumerId,
        Long restaurantId,
        String state,
        List<LineItemFixture> lineItems,
        Money orderTotal) {

    /**
     * @param menuItemId menu item identity (restaurant-scoped)
     * @param name       menu item display name
     * @param price      unit price at order time
     * @param quantity   ordered quantity (must be positive)
     */
    public record LineItemFixture(String menuItemId, String name, Money price, int quantity) {
    }
}
