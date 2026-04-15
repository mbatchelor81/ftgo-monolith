package com.ftgo.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Unit tests for {@link FtgoRole}. */
@DisplayName("FtgoRole")
class FtgoRoleTest {

    @Test
    @DisplayName("should define exactly 4 roles")
    void shouldDefineFourRoles() {
        assertThat(FtgoRole.values()).hasSize(4);
    }

    @Test
    @DisplayName("should contain CUSTOMER, RESTAURANT_OWNER, COURIER, and ADMIN")
    void shouldContainExpectedRoles() {
        assertThat(FtgoRole.values())
                .containsExactlyInAnyOrder(
                        FtgoRole.CUSTOMER,
                        FtgoRole.RESTAURANT_OWNER,
                        FtgoRole.COURIER,
                        FtgoRole.ADMIN);
    }

    @ParameterizedTest
    @EnumSource(FtgoRole.class)
    @DisplayName("authority() should return ROLE_ prefixed name")
    void authority_shouldReturnRolePrefixedName(FtgoRole role) {
        assertThat(role.authority()).isEqualTo("ROLE_" + role.name());
    }

    @Test
    @DisplayName("ADMIN authority should be ROLE_ADMIN")
    void adminAuthority_shouldBeRoleAdmin() {
        assertThat(FtgoRole.ADMIN.authority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("CUSTOMER authority should be ROLE_CUSTOMER")
    void customerAuthority_shouldBeRoleCustomer() {
        assertThat(FtgoRole.CUSTOMER.authority()).isEqualTo("ROLE_CUSTOMER");
    }
}
