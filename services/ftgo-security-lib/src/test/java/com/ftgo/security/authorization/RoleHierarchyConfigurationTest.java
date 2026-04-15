package com.ftgo.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Unit tests for {@link RoleHierarchyConfiguration}. */
@DisplayName("RoleHierarchyConfiguration")
class RoleHierarchyConfigurationTest {

    private RoleHierarchy roleHierarchy;

    @BeforeEach
    void setUp() {
        roleHierarchy = new RoleHierarchyConfiguration().roleHierarchy();
    }

    private Collection<? extends GrantedAuthority> reachableAuthorities(String... roles) {
        List<SimpleGrantedAuthority> authorities =
                java.util.Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();
        return roleHierarchy.getReachableGrantedAuthorities(authorities);
    }

    private boolean hasAuthority(
            Collection<? extends GrantedAuthority> authorities, String authority) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(authority));
    }

    @Nested
    @DisplayName("ADMIN role hierarchy")
    class AdminHierarchy {

        @Test
        @DisplayName("ADMIN should inherit RESTAURANT_OWNER")
        void admin_shouldInheritRestaurantOwner() {
            Collection<? extends GrantedAuthority> authorities = reachableAuthorities("ROLE_ADMIN");
            assertThat(hasAuthority(authorities, "ROLE_RESTAURANT_OWNER")).isTrue();
        }

        @Test
        @DisplayName("ADMIN should inherit COURIER")
        void admin_shouldInheritCourier() {
            Collection<? extends GrantedAuthority> authorities = reachableAuthorities("ROLE_ADMIN");
            assertThat(hasAuthority(authorities, "ROLE_COURIER")).isTrue();
        }

        @Test
        @DisplayName("ADMIN should inherit CUSTOMER (via RESTAURANT_OWNER)")
        void admin_shouldInheritCustomer() {
            Collection<? extends GrantedAuthority> authorities = reachableAuthorities("ROLE_ADMIN");
            assertThat(hasAuthority(authorities, "ROLE_CUSTOMER")).isTrue();
        }

        @Test
        @DisplayName("ADMIN should have all 4 role authorities")
        void admin_shouldHaveAllRoles() {
            Collection<? extends GrantedAuthority> authorities = reachableAuthorities("ROLE_ADMIN");
            assertThat(hasAuthority(authorities, "ROLE_ADMIN")).isTrue();
            assertThat(hasAuthority(authorities, "ROLE_RESTAURANT_OWNER")).isTrue();
            assertThat(hasAuthority(authorities, "ROLE_COURIER")).isTrue();
            assertThat(hasAuthority(authorities, "ROLE_CUSTOMER")).isTrue();
        }
    }

    @Nested
    @DisplayName("RESTAURANT_OWNER role hierarchy")
    class RestaurantOwnerHierarchy {

        @Test
        @DisplayName("RESTAURANT_OWNER should inherit CUSTOMER")
        void restaurantOwner_shouldInheritCustomer() {
            Collection<? extends GrantedAuthority> authorities =
                    reachableAuthorities("ROLE_RESTAURANT_OWNER");
            assertThat(hasAuthority(authorities, "ROLE_CUSTOMER")).isTrue();
        }

        @Test
        @DisplayName("RESTAURANT_OWNER should NOT inherit ADMIN")
        void restaurantOwner_shouldNotInheritAdmin() {
            Collection<? extends GrantedAuthority> authorities =
                    reachableAuthorities("ROLE_RESTAURANT_OWNER");
            assertThat(hasAuthority(authorities, "ROLE_ADMIN")).isFalse();
        }

        @Test
        @DisplayName("RESTAURANT_OWNER should NOT inherit COURIER")
        void restaurantOwner_shouldNotInheritCourier() {
            Collection<? extends GrantedAuthority> authorities =
                    reachableAuthorities("ROLE_RESTAURANT_OWNER");
            assertThat(hasAuthority(authorities, "ROLE_COURIER")).isFalse();
        }
    }

    @Nested
    @DisplayName("COURIER role hierarchy")
    class CourierHierarchy {

        @Test
        @DisplayName("COURIER should NOT inherit any other role")
        void courier_shouldNotInheritOtherRoles() {
            Collection<? extends GrantedAuthority> authorities =
                    reachableAuthorities("ROLE_COURIER");
            assertThat(hasAuthority(authorities, "ROLE_COURIER")).isTrue();
            assertThat(hasAuthority(authorities, "ROLE_ADMIN")).isFalse();
            assertThat(hasAuthority(authorities, "ROLE_RESTAURANT_OWNER")).isFalse();
            assertThat(hasAuthority(authorities, "ROLE_CUSTOMER")).isFalse();
        }
    }

    @Nested
    @DisplayName("CUSTOMER role hierarchy")
    class CustomerHierarchy {

        @Test
        @DisplayName("CUSTOMER should NOT inherit any other role")
        void customer_shouldNotInheritOtherRoles() {
            Collection<? extends GrantedAuthority> authorities =
                    reachableAuthorities("ROLE_CUSTOMER");
            assertThat(hasAuthority(authorities, "ROLE_CUSTOMER")).isTrue();
            assertThat(hasAuthority(authorities, "ROLE_ADMIN")).isFalse();
            assertThat(hasAuthority(authorities, "ROLE_RESTAURANT_OWNER")).isFalse();
            assertThat(hasAuthority(authorities, "ROLE_COURIER")).isFalse();
        }
    }
}
