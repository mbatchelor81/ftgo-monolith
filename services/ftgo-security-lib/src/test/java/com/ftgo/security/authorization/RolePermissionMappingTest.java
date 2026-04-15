package com.ftgo.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link RolePermissionMapping}. */
@DisplayName("RolePermissionMapping")
class RolePermissionMappingTest {

    @Nested
    @DisplayName("CUSTOMER permissions")
    class CustomerPermissions {

        private final Set<String> permissions =
                RolePermissionMapping.getPermissions(FtgoRole.CUSTOMER);

        @Test
        @DisplayName("should include consumer:read:own")
        void shouldIncludeConsumerReadOwn() {
            assertThat(permissions).contains(FtgoPermission.CONSUMER_READ_OWN);
        }

        @Test
        @DisplayName("should include order:create")
        void shouldIncludeOrderCreate() {
            assertThat(permissions).contains(FtgoPermission.ORDER_CREATE);
        }

        @Test
        @DisplayName("should include order:read:own and order:cancel:own")
        void shouldIncludeOwnOrderOperations() {
            assertThat(permissions)
                    .contains(FtgoPermission.ORDER_READ_OWN, FtgoPermission.ORDER_CANCEL_OWN);
        }

        @Test
        @DisplayName("should include restaurant:read")
        void shouldIncludeRestaurantRead() {
            assertThat(permissions).contains(FtgoPermission.RESTAURANT_READ);
        }

        @Test
        @DisplayName("should include courier:read:own for delivery tracking")
        void shouldIncludeCourierReadOwn() {
            assertThat(permissions).contains(FtgoPermission.COURIER_READ_OWN);
        }

        @Test
        @DisplayName("should NOT include admin-only permissions")
        void shouldNotIncludeAdminPermissions() {
            assertThat(permissions)
                    .doesNotContain(
                            FtgoPermission.CONSUMER_CREATE,
                            FtgoPermission.CONSUMER_READ,
                            FtgoPermission.ORDER_READ,
                            FtgoPermission.ORDER_CANCEL,
                            FtgoPermission.ORDER_REVISE);
        }
    }

    @Nested
    @DisplayName("RESTAURANT_OWNER permissions")
    class RestaurantOwnerPermissions {

        private final Set<String> permissions =
                RolePermissionMapping.getPermissions(FtgoRole.RESTAURANT_OWNER);

        @Test
        @DisplayName("should include order:read and order:accept")
        void shouldIncludeOrderReadAndAccept() {
            assertThat(permissions)
                    .contains(FtgoPermission.ORDER_READ, FtgoPermission.ORDER_ACCEPT);
        }

        @Test
        @DisplayName("should include order:status:update")
        void shouldIncludeOrderStatusUpdate() {
            assertThat(permissions).contains(FtgoPermission.ORDER_STATUS_UPDATE);
        }

        @Test
        @DisplayName("should include full restaurant CRUD for own restaurants")
        void shouldIncludeRestaurantCrud() {
            assertThat(permissions)
                    .contains(
                            FtgoPermission.RESTAURANT_CREATE,
                            FtgoPermission.RESTAURANT_READ,
                            FtgoPermission.RESTAURANT_UPDATE_OWN,
                            FtgoPermission.RESTAURANT_DELETE);
        }

        @Test
        @DisplayName("should NOT include courier permissions")
        void shouldNotIncludeCourierPermissions() {
            assertThat(permissions)
                    .doesNotContain(
                            FtgoPermission.COURIER_CREATE,
                            FtgoPermission.COURIER_UPDATE_AVAILABILITY);
        }
    }

    @Nested
    @DisplayName("COURIER permissions")
    class CourierPermissions {

        private final Set<String> permissions =
                RolePermissionMapping.getPermissions(FtgoRole.COURIER);

        @Test
        @DisplayName("should include order:read:own for assigned orders")
        void shouldIncludeOrderReadOwn() {
            assertThat(permissions).contains(FtgoPermission.ORDER_READ_OWN);
        }

        @Test
        @DisplayName("should include own courier operations")
        void shouldIncludeOwnCourierOperations() {
            assertThat(permissions)
                    .contains(
                            FtgoPermission.COURIER_READ_OWN,
                            FtgoPermission.COURIER_UPDATE_OWN_AVAILABILITY);
        }

        @Test
        @DisplayName("should include courier:delivery:update")
        void shouldIncludeCourierDeliveryUpdate() {
            assertThat(permissions).contains(FtgoPermission.COURIER_DELIVERY_UPDATE);
        }

        @Test
        @DisplayName("should NOT include restaurant management permissions")
        void shouldNotIncludeRestaurantPermissions() {
            assertThat(permissions)
                    .doesNotContain(
                            FtgoPermission.RESTAURANT_CREATE,
                            FtgoPermission.RESTAURANT_UPDATE,
                            FtgoPermission.RESTAURANT_DELETE);
        }
    }

    @Nested
    @DisplayName("ADMIN permissions")
    class AdminPermissions {

        private final Set<String> permissions =
                RolePermissionMapping.getPermissions(FtgoRole.ADMIN);

        @Test
        @DisplayName("should include all permissions from CUSTOMER")
        void shouldIncludeAllCustomerPermissions() {
            assertThat(permissions)
                    .containsAll(RolePermissionMapping.getPermissions(FtgoRole.CUSTOMER));
        }

        @Test
        @DisplayName("should include all permissions from RESTAURANT_OWNER")
        void shouldIncludeAllRestaurantOwnerPermissions() {
            assertThat(permissions)
                    .containsAll(RolePermissionMapping.getPermissions(FtgoRole.RESTAURANT_OWNER));
        }

        @Test
        @DisplayName("should include all permissions from COURIER")
        void shouldIncludeAllCourierPermissions() {
            assertThat(permissions)
                    .containsAll(RolePermissionMapping.getPermissions(FtgoRole.COURIER));
        }

        @Test
        @DisplayName("should include admin-only broad permissions")
        void shouldIncludeAdminOnlyPermissions() {
            assertThat(permissions)
                    .contains(
                            FtgoPermission.CONSUMER_CREATE,
                            FtgoPermission.CONSUMER_READ,
                            FtgoPermission.ORDER_READ,
                            FtgoPermission.ORDER_CANCEL,
                            FtgoPermission.ORDER_REVISE,
                            FtgoPermission.RESTAURANT_UPDATE,
                            FtgoPermission.COURIER_CREATE,
                            FtgoPermission.COURIER_READ,
                            FtgoPermission.COURIER_UPDATE_AVAILABILITY);
        }
    }

    @Nested
    @DisplayName("getPermissions(String)")
    class GetPermissionsByName {

        @Test
        @DisplayName("should return permissions for valid role name")
        void shouldReturnPermissionsForValidName() {
            assertThat(RolePermissionMapping.getPermissions("CUSTOMER"))
                    .isEqualTo(RolePermissionMapping.getPermissions(FtgoRole.CUSTOMER));
        }

        @Test
        @DisplayName("should return empty set for unknown role name")
        void shouldReturnEmptyForUnknownRole() {
            assertThat(RolePermissionMapping.getPermissions("UNKNOWN")).isEmpty();
        }
    }

    @Test
    @DisplayName("getAllMappings should return mappings for all 4 roles")
    void getAllMappings_shouldReturnAllFourRoles() {
        Map<FtgoRole, Set<String>> allMappings = RolePermissionMapping.getAllMappings();
        assertThat(allMappings).hasSize(4);
        assertThat(allMappings)
                .containsKeys(
                        FtgoRole.CUSTOMER,
                        FtgoRole.RESTAURANT_OWNER,
                        FtgoRole.COURIER,
                        FtgoRole.ADMIN);
    }

    @Test
    @DisplayName("every role should have at least one permission")
    void everyRole_shouldHaveAtLeastOnePermission() {
        for (FtgoRole role : FtgoRole.values()) {
            assertThat(RolePermissionMapping.getPermissions(role))
                    .as("Permissions for role " + role)
                    .isNotEmpty();
        }
    }
}
