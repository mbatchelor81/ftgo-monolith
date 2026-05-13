package com.ftgo.security.authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoPermissionEvaluatorTest {

    private FtgoPermissionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        ResourceOwnershipChecker orderChecker = new ResourceOwnershipChecker() {
            @Override
            public String getTargetType() {
                return "Order";
            }

            @Override
            public boolean isOwner(String userId, Serializable resourceId) {
                return "owner-user".equals(userId);
            }
        };
        evaluator = new FtgoPermissionEvaluator(List.of(orderChecker));
    }

    @Test
    void hasPermission_returnsTrue_whenUserHasAuthority() {
        Authentication auth = authWithAuthorities("order:create");
        assertThat(evaluator.hasPermission(auth, null, "order:create")).isTrue();
    }

    @Test
    void hasPermission_returnsFalse_whenUserLacksAuthority() {
        Authentication auth = authWithAuthorities("order:read");
        assertThat(evaluator.hasPermission(auth, null, "order:create")).isFalse();
    }

    @Test
    void hasPermission_returnsFalse_whenAuthenticationIsNull() {
        assertThat(evaluator.hasPermission(null, null, "order:create")).isFalse();
    }

    @Test
    void hasPermissionWithOwnership_returnsTrue_whenUserIsOwner() {
        Authentication auth = authWithNameAndAuthorities("owner-user", "order:read");
        assertThat(evaluator.hasPermission(auth, 1L, "Order", "order:read")).isTrue();
    }

    @Test
    void hasPermissionWithOwnership_returnsFalse_whenUserIsNotOwner() {
        Authentication auth = authWithNameAndAuthorities("other-user", "order:read");
        assertThat(evaluator.hasPermission(auth, 1L, "Order", "order:read")).isFalse();
    }

    @Test
    void hasPermissionWithOwnership_returnsTrue_forAdmin_regardlessOfOwnership() {
        Authentication auth = authWithNameAndAuthorities("other-user", "order:read", "ROLE_ADMIN");
        assertThat(evaluator.hasPermission(auth, 1L, "Order", "order:read")).isTrue();
    }

    @Test
    void hasPermissionWithOwnership_returnsFalse_whenNoCheckerRegistered() {
        Authentication auth = authWithNameAndAuthorities("owner-user", "restaurant:read");
        assertThat(evaluator.hasPermission(auth, 1L, "Restaurant", "restaurant:read")).isFalse();
    }

    @Test
    void hasPermissionWithOwnership_returnsTrue_whenTargetIdIsNull() {
        Authentication auth = authWithAuthorities("order:read");
        assertThat(evaluator.hasPermission(auth, null, "Order", "order:read")).isTrue();
    }

    @Test
    void hasPermissionWithOwnership_returnsFalse_whenAuthorityMissing() {
        Authentication auth = authWithNameAndAuthorities("owner-user", "order:create");
        assertThat(evaluator.hasPermission(auth, 1L, "Order", "order:read")).isFalse();
    }

    @Test
    void constructor_handlesNullCheckersList() {
        FtgoPermissionEvaluator eval = new FtgoPermissionEvaluator(null);
        Authentication auth = authWithAuthorities("order:read");
        assertThat(eval.hasPermission(auth, 1L, "Order", "order:read")).isFalse();
    }

    @Test
    void constructor_handlesEmptyCheckersList() {
        FtgoPermissionEvaluator eval = new FtgoPermissionEvaluator(Collections.emptyList());
        Authentication auth = authWithAuthorities("order:read");
        assertThat(eval.hasPermission(auth, 1L, "Order", "order:read")).isFalse();
    }

    private Authentication authWithAuthorities(String... authorities) {
        return authWithNameAndAuthorities("test-user", authorities);
    }

    private Authentication authWithNameAndAuthorities(String name, String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities = java.util.Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .toList();
        TestingAuthenticationToken token = new TestingAuthenticationToken(name, null, grantedAuthorities);
        token.setAuthenticated(true);
        return token;
    }
}
