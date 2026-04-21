package com.ftgo.test.fixtures;

/**
 * Immutable test fixture representing a courier for use in service tests.
 *
 * @param id        courier identity
 * @param firstName courier given name
 * @param lastName  courier family name
 * @param available whether the courier is on-shift and can accept deliveries
 */
public record CourierFixture(Long id, String firstName, String lastName, boolean available) {
}
