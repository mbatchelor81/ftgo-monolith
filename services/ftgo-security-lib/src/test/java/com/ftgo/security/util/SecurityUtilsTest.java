package com.ftgo.security.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityUtilsTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUsername_whenAuthenticated_returnsUsername() {
        setAuthentication("alice", "ROLE_USER");

        assertThat(SecurityUtils.getCurrentUsername()).contains("alice");
    }

    @Test
    void getCurrentUsername_whenAnonymous_returnsEmpty() {
        assertThat(SecurityUtils.getCurrentUsername()).isEmpty();
    }

    @Test
    void getCurrentAuthorities_returnsGrantedAuthorities() {
        setAuthentication("alice", "ROLE_USER", "ROLE_ADMIN");

        Collection<String> authorities = SecurityUtils.getCurrentAuthorities();

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void hasAuthority_returnsTrueWhenPresent() {
        setAuthentication("alice", "ROLE_ADMIN");

        assertThat(SecurityUtils.hasAuthority("ROLE_ADMIN")).isTrue();
        assertThat(SecurityUtils.hasAuthority("ROLE_USER")).isFalse();
    }

    @Test
    void hasRole_prefixesRoleAutomatically() {
        setAuthentication("alice", "ROLE_ADMIN");

        assertThat(SecurityUtils.hasRole("ADMIN")).isTrue();
        assertThat(SecurityUtils.hasRole("ROLE_ADMIN")).isTrue();
        assertThat(SecurityUtils.hasRole("USER")).isFalse();
    }

    @Test
    void isAuthenticated_returnsTrueWhenAuthenticated() {
        setAuthentication("alice", "ROLE_USER");

        assertThat(SecurityUtils.isAuthenticated()).isTrue();
    }

    @Test
    void isAuthenticated_returnsFalseWhenAnonymous() {
        assertThat(SecurityUtils.isAuthenticated()).isFalse();
    }

    private void setAuthentication(String username, String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities =
                java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
