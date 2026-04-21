package com.ftgo.test.fixtures;

/**
 * Immutable test fixture representing a consumer for use in service tests.
 *
 * <p>Intentionally decoupled from any JPA entity so this module can be
 * consumed by plain {@code libs/*} modules (no Spring Boot / Hibernate on
 * the classpath). Service tests that need a persisted {@code Consumer}
 * should map this fixture into their own entity.
 *
 * @param id        database identity
 * @param firstName consumer given name
 * @param lastName  consumer family name
 * @param email     contact email
 */
public record ConsumerFixture(Long id, String firstName, String lastName, String email) {
}
