package com.ftgo.common.security.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ResourceOwnershipEvaluatorTest {

    private ResourceOwnershipEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new ResourceOwnershipEvaluator();
    }

    @Test
    void ownerCanAccessOwnResource() {
        Authentication auth = new TestingAuthenticationToken("user-123", null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        assertThat(evaluator.hasPermission(auth, "user-123", "read")).isTrue();
    }

    @Test
    void nonOwnerCannotAccessResource() {
        Authentication auth = new TestingAuthenticationToken("user-456", null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        assertThat(evaluator.hasPermission(auth, "user-123", "read")).isFalse();
    }

    @Test
    void adminBypassesOwnershipCheck() {
        Authentication auth = new TestingAuthenticationToken("admin-1", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertThat(evaluator.hasPermission(auth, "user-123", "read")).isTrue();
    }

    @Test
    void nullAuthenticationReturnsFalse() {
        assertThat(evaluator.hasPermission(null, "user-123", "read")).isFalse();
    }

    @Test
    void nullTargetReturnsFalse() {
        Authentication auth = new TestingAuthenticationToken("user-123", null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        assertThat(evaluator.hasPermission(auth, null, "read")).isFalse();
    }

    @Test
    void serializedIdOwnershipCheck() {
        Authentication auth = new TestingAuthenticationToken("user-123", null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        assertThat(evaluator.hasPermission(auth, 123L, "Consumer", "read")).isFalse();
        assertThat(evaluator.hasPermission(auth, "user-123", "Consumer", "read")).isTrue();
    }

    @Test
    void adminBypassesSerializedIdCheck() {
        Authentication auth = new TestingAuthenticationToken("admin-1", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertThat(evaluator.hasPermission(auth, 999L, "Consumer", "read")).isTrue();
    }
}
