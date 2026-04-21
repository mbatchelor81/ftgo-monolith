package net.chrisrichardson.ftgo.security.authorization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void authority_prependsRolePrefix() {
        assertThat(Role.CUSTOMER.authority()).isEqualTo("ROLE_CUSTOMER");
        assertThat(Role.RESTAURANT_OWNER.authority()).isEqualTo("ROLE_RESTAURANT_OWNER");
        assertThat(Role.COURIER.authority()).isEqualTo("ROLE_COURIER");
        assertThat(Role.ADMIN.authority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void claimValue_matchesEnumName() {
        for (Role role : Role.values()) {
            assertThat(role.claimValue()).isEqualTo(role.name());
        }
    }

    @Test
    void rolesContainAllFourCanonicalRoles() {
        assertThat(Role.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder("CUSTOMER", "RESTAURANT_OWNER", "COURIER", "ADMIN");
    }
}
