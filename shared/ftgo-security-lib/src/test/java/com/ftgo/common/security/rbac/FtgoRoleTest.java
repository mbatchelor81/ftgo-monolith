package com.ftgo.common.security.rbac;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class FtgoRoleTest {

    @Test
    void allRolesHaveCorrectAuthorityPrefix() {
        for (FtgoRole role : FtgoRole.values()) {
            assertThat(role.getAuthority()).startsWith("ROLE_");
        }
    }

    @Test
    void roleNameMatchesEnumName() {
        assertThat(FtgoRole.CUSTOMER.roleName()).isEqualTo("CUSTOMER");
        assertThat(FtgoRole.COURIER.roleName()).isEqualTo("COURIER");
        assertThat(FtgoRole.RESTAURANT_OWNER.roleName()).isEqualTo("RESTAURANT_OWNER");
        assertThat(FtgoRole.ADMIN.roleName()).isEqualTo("ADMIN");
    }

    @Test
    void fourRolesAreDefined() {
        assertThat(FtgoRole.values()).hasSize(4);
    }
}
