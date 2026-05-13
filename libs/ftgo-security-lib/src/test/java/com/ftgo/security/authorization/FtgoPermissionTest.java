package com.ftgo.security.authorization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoPermissionTest {

    @Test
    void authorityUsesColonSeparatedFormat() {
        assertThat(FtgoPermission.ORDER_CREATE.authority()).isEqualTo("order:create");
        assertThat(FtgoPermission.COURIER_UPDATE_AVAILABILITY.authority()).isEqualTo("courier:update-availability");
    }

    @Test
    void allPermissionsHaveNonEmptyAuthority() {
        for (FtgoPermission permission : FtgoPermission.values()) {
            assertThat(permission.authority()).isNotBlank();
            assertThat(permission.authority()).contains(":");
        }
    }
}
