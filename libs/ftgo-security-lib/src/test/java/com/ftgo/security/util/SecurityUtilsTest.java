package com.ftgo.security.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilsTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentUsernameReturnsEmptyWhenNoAuthentication() {
        assertThat(SecurityUtils.currentUsername()).isEmpty();
    }

    @Test
    void currentUsernameReturnsNameWhenAuthenticated() {
        setAuthentication("alice", "ROLE_USER");
        assertThat(SecurityUtils.currentUsername()).contains("alice");
    }

    @Test
    void hasAuthorityReturnsTrueForMatchingAuthority() {
        setAuthentication("bob", "ROLE_ADMIN");
        assertThat(SecurityUtils.hasAuthority("ROLE_ADMIN")).isTrue();
        assertThat(SecurityUtils.hasAuthority("ROLE_USER")).isFalse();
    }

    @Test
    void hasRoleHandlesPrefixTransparently() {
        setAuthentication("carol", "ROLE_MANAGER");
        assertThat(SecurityUtils.hasRole("MANAGER")).isTrue();
        assertThat(SecurityUtils.hasRole("ROLE_MANAGER")).isTrue();
    }

    @Test
    void isAuthenticatedReturnsFalseWhenNoContext() {
        assertThat(SecurityUtils.isAuthenticated()).isFalse();
    }

    @Test
    void isAuthenticatedReturnsTrueWhenAuthenticated() {
        setAuthentication("dave", "ROLE_USER");
        assertThat(SecurityUtils.isAuthenticated()).isTrue();
    }

    private void setAuthentication(String username, String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities =
                List.of(authorities).stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
