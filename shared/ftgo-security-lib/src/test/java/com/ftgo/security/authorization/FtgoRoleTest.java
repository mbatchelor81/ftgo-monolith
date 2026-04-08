package com.ftgo.security.authorization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoRoleTest {

    @Test
    void roleEnum_hasFourRoles() {
        assertThat(FtgoRole.values()).hasSize(4);
    }

    @Test
    void roleEnum_containsExpectedRoles() {
        assertThat(FtgoRole.values()).containsExactly(
            FtgoRole.CUSTOMER,
            FtgoRole.RESTAURANT_OWNER,
            FtgoRole.COURIER,
            FtgoRole.ADMIN
        );
    }

    @Test
    void authority_prefixesWithRole() {
        assertThat(FtgoRole.CUSTOMER.authority()).isEqualTo("ROLE_CUSTOMER");
        assertThat(FtgoRole.RESTAURANT_OWNER.authority()).isEqualTo("ROLE_RESTAURANT_OWNER");
        assertThat(FtgoRole.COURIER.authority()).isEqualTo("ROLE_COURIER");
        assertThat(FtgoRole.ADMIN.authority()).isEqualTo("ROLE_ADMIN");
    }
}
