package com.ftgo.security.authorization;

import com.ftgo.security.jwt.FtgoUserDetails;
import com.ftgo.security.jwt.JwtAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoPermissionEvaluatorTest {

    private FtgoPermissionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new FtgoPermissionEvaluator();
    }

    @Test
    void hasPermission_adminRole_grantsAccess() {
        JwtAuthenticationToken auth = createAuth("admin-1", Set.of("ROLE_ADMIN"), Set.of());
        assertThat(evaluator.hasPermission(auth, 42L, "Order", "read")).isTrue();
    }

    @Test
    void hasPermission_ownerMatchesTargetId_grantsAccess() {
        JwtAuthenticationToken auth = createAuth("user-42", Set.of("ROLE_CUSTOMER"), Set.of());
        assertThat(evaluator.hasPermission(auth, "user-42", "Consumer", "read")).isTrue();
    }

    @Test
    void hasPermission_ownerDoesNotMatchTargetId_deniesAccess() {
        JwtAuthenticationToken auth = createAuth("user-1", Set.of("ROLE_CUSTOMER"), Set.of());
        assertThat(evaluator.hasPermission(auth, "user-99", "Consumer", "read")).isFalse();
    }

    @Test
    void hasPermission_withMatchingFineGrainedPermission_grantsAccess() {
        JwtAuthenticationToken auth = createAuth("user-1", Set.of("ROLE_CUSTOMER"),
            Set.of("order:read", "order:create"));
        assertThat(evaluator.hasPermission(auth, 99L, "Order", "read")).isTrue();
    }

    @Test
    void hasPermission_withoutMatchingPermission_deniesAccess() {
        JwtAuthenticationToken auth = createAuth("user-1", Set.of("ROLE_CUSTOMER"),
            Set.of("order:read"));
        assertThat(evaluator.hasPermission(auth, 99L, "Order", "cancel")).isFalse();
    }

    @Test
    void hasPermission_nullAuthentication_deniesAccess() {
        assertThat(evaluator.hasPermission(null, 1L, "Order", "read")).isFalse();
    }

    @Test
    void hasPermission_nullTargetType_deniesAccess() {
        JwtAuthenticationToken auth = createAuth("user-1", Set.of("ROLE_CUSTOMER"), Set.of());
        assertThat(evaluator.hasPermission(auth, 1L, null, "read")).isFalse();
    }

    @Test
    void hasPermission_objectBased_returnsFalse() {
        JwtAuthenticationToken auth = createAuth("user-1", Set.of("ROLE_CUSTOMER"), Set.of());
        assertThat(evaluator.hasPermission(auth, new Object(), "read")).isFalse();
    }

    private JwtAuthenticationToken createAuth(String userId, Set<String> roles, Set<String> permissions) {
        var authorities = new java.util.ArrayList<SimpleGrantedAuthority>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        for (String perm : permissions) {
            authorities.add(new SimpleGrantedAuthority(perm));
        }
        FtgoUserDetails userDetails = new FtgoUserDetails(userId, roles, permissions, authorities);
        return new JwtAuthenticationToken(userId, "test-token", userDetails, authorities);
    }
}
