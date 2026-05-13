package com.ftgo.security.authorization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoRoleTest {

    @Test
    void customerHasExpectedPermissions() {
        assertThat(FtgoRole.CUSTOMER.getPermissions()).containsExactlyInAnyOrder(
                FtgoPermission.ORDER_CREATE,
                FtgoPermission.ORDER_READ,
                FtgoPermission.ORDER_CANCEL,
                FtgoPermission.ORDER_REVISE,
                FtgoPermission.CONSUMER_READ
        );
    }

    @Test
    void restaurantOwnerHasExpectedPermissions() {
        assertThat(FtgoRole.RESTAURANT_OWNER.getPermissions()).containsExactlyInAnyOrder(
                FtgoPermission.ORDER_READ,
                FtgoPermission.ORDER_ACCEPT,
                FtgoPermission.ORDER_REJECT,
                FtgoPermission.ORDER_PREPARE,
                FtgoPermission.RESTAURANT_READ,
                FtgoPermission.RESTAURANT_UPDATE,
                FtgoPermission.MENU_UPDATE
        );
    }

    @Test
    void courierHasExpectedPermissions() {
        assertThat(FtgoRole.COURIER.getPermissions()).containsExactlyInAnyOrder(
                FtgoPermission.ORDER_READ,
                FtgoPermission.ORDER_PICKUP,
                FtgoPermission.ORDER_DELIVER,
                FtgoPermission.COURIER_READ,
                FtgoPermission.COURIER_UPDATE_AVAILABILITY
        );
    }

    @Test
    void adminHasAllPermissions() {
        assertThat(FtgoRole.ADMIN.getPermissions())
                .containsExactlyInAnyOrder(FtgoPermission.values());
    }

    @Test
    void hasPermissionReturnsCorrectResult() {
        assertThat(FtgoRole.CUSTOMER.hasPermission(FtgoPermission.ORDER_CREATE)).isTrue();
        assertThat(FtgoRole.CUSTOMER.hasPermission(FtgoPermission.ORDER_ACCEPT)).isFalse();
    }

    @Test
    void authorityReturnsRolePrefixedName() {
        assertThat(FtgoRole.ADMIN.authority()).isEqualTo("ROLE_ADMIN");
        assertThat(FtgoRole.CUSTOMER.authority()).isEqualTo("ROLE_CUSTOMER");
    }
}
