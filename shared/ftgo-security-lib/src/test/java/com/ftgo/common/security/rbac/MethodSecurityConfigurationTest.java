package com.ftgo.common.security.rbac;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class MethodSecurityConfigurationTest {

    private final MethodSecurityConfiguration config = new MethodSecurityConfiguration();

    @Test
    void adminInheritsAllRoles() {
        RoleHierarchy hierarchy = config.roleHierarchy();
        Collection<?> reachable = hierarchy.getReachableGrantedAuthorities(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertThat(reachable).extracting("authority")
                .contains("ROLE_ADMIN", "ROLE_RESTAURANT_OWNER", "ROLE_COURIER", "ROLE_CUSTOMER");
    }

    @Test
    void restaurantOwnerInheritsCourierAndCustomer() {
        RoleHierarchy hierarchy = config.roleHierarchy();
        Collection<?> reachable = hierarchy.getReachableGrantedAuthorities(
                List.of(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER")));
        assertThat(reachable).extracting("authority")
                .contains("ROLE_RESTAURANT_OWNER", "ROLE_COURIER", "ROLE_CUSTOMER")
                .doesNotContain("ROLE_ADMIN");
    }

    @Test
    void courierInheritsCustomer() {
        RoleHierarchy hierarchy = config.roleHierarchy();
        Collection<?> reachable = hierarchy.getReachableGrantedAuthorities(
                List.of(new SimpleGrantedAuthority("ROLE_COURIER")));
        assertThat(reachable).extracting("authority")
                .contains("ROLE_COURIER", "ROLE_CUSTOMER")
                .doesNotContain("ROLE_ADMIN", "ROLE_RESTAURANT_OWNER");
    }

    @Test
    void customerHasOnlyCustomerRole() {
        RoleHierarchy hierarchy = config.roleHierarchy();
        Collection<?> reachable = hierarchy.getReachableGrantedAuthorities(
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        assertThat(reachable).extracting("authority")
                .contains("ROLE_CUSTOMER")
                .doesNotContain("ROLE_ADMIN", "ROLE_RESTAURANT_OWNER", "ROLE_COURIER");
    }
}
