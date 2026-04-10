package com.ftgo.security.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SecurityContextUtils}.
 */
class SecurityContextUtilsTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getCurrentUsername returns empty when no authentication")
    void getCurrentUsername_noAuth_returnsEmpty() {
        assertThat(SecurityContextUtils.getCurrentUsername()).isEmpty();
    }

    @Test
    @DisplayName("getCurrentUsername returns username when authenticated")
    void getCurrentUsername_authenticated_returnsUsername() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "testuser", "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(SecurityContextUtils.getCurrentUsername())
            .isPresent()
            .hasValue("testuser");
    }

    @Test
    @DisplayName("isAuthenticated returns false when no authentication")
    void isAuthenticated_noAuth_returnsFalse() {
        assertThat(SecurityContextUtils.isAuthenticated()).isFalse();
    }

    @Test
    @DisplayName("isAuthenticated returns true when authenticated")
    void isAuthenticated_authenticated_returnsTrue() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "testuser", "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(SecurityContextUtils.isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("hasAuthority returns true when user has the authority")
    void hasAuthority_withMatchingAuthority_returnsTrue() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "admin", "password",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(SecurityContextUtils.hasAuthority("ROLE_ADMIN")).isTrue();
    }

    @Test
    @DisplayName("hasAuthority returns false when user lacks the authority")
    void hasAuthority_withoutMatchingAuthority_returnsFalse() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "user", "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(SecurityContextUtils.hasAuthority("ROLE_ADMIN")).isFalse();
    }

    @Test
    @DisplayName("getCurrentAuthorities returns empty when no authentication")
    void getCurrentAuthorities_noAuth_returnsEmpty() {
        assertThat(SecurityContextUtils.getCurrentAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("getCurrentAuthorities returns authorities when authenticated")
    void getCurrentAuthorities_authenticated_returnsAuthorities() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "user", "password",
            List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_SERVICE")
            )
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(SecurityContextUtils.getCurrentAuthorities()).hasSize(2);
    }
}
