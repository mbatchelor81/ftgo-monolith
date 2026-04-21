package net.chrisrichardson.ftgo.security.authorization;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the {@link RoleHierarchy} bean wired by
 * {@link MethodSecurityConfiguration}. The hierarchy is:
 *
 * <pre>{@code
 *   ADMIN > RESTAURANT_OWNER > CUSTOMER
 *   ADMIN > COURIER          > CUSTOMER
 * }</pre>
 */
class RoleHierarchyTest {

    private final RoleHierarchy hierarchy =
            new MethodSecurityConfiguration().ftgoRoleHierarchy();

    @Test
    void admin_inheritsEveryOtherRole() {
        Set<String> authorities = reachable(List.of(Role.ADMIN.authority()));

        assertThat(authorities).contains(
                Role.ADMIN.authority(),
                Role.RESTAURANT_OWNER.authority(),
                Role.COURIER.authority(),
                Role.CUSTOMER.authority());
    }

    @Test
    void restaurantOwner_inheritsCustomerButNotAdmin() {
        Set<String> authorities = reachable(List.of(Role.RESTAURANT_OWNER.authority()));

        assertThat(authorities).contains(
                Role.RESTAURANT_OWNER.authority(),
                Role.CUSTOMER.authority());
        assertThat(authorities).doesNotContain(
                Role.ADMIN.authority(),
                Role.COURIER.authority());
    }

    @Test
    void courier_inheritsCustomerButNotAdminOrRestaurantOwner() {
        Set<String> authorities = reachable(List.of(Role.COURIER.authority()));

        assertThat(authorities).contains(
                Role.COURIER.authority(),
                Role.CUSTOMER.authority());
        assertThat(authorities).doesNotContain(
                Role.ADMIN.authority(),
                Role.RESTAURANT_OWNER.authority());
    }

    @Test
    void customer_doesNotInheritAnyOtherRole() {
        Set<String> authorities = reachable(List.of(Role.CUSTOMER.authority()));

        assertThat(authorities).containsExactly(Role.CUSTOMER.authority());
    }

    private Set<String> reachable(List<String> granted) {
        List<GrantedAuthority> grantedAuthorities = granted.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return hierarchy.getReachableGrantedAuthorities(grantedAuthorities).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
