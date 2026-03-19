package com.ftgo.common.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilsTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUsername_withAuthentication_returnsUsername() {
        var auth = new UsernamePasswordAuthenticationToken("testuser", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(SecurityUtils.getCurrentUsername()).contains("testuser");
    }

    @Test
    void getCurrentUsername_withNoAuthentication_returnsEmpty() {
        assertThat(SecurityUtils.getCurrentUsername()).isEmpty();
    }

    @Test
    void isAuthenticated_withAuthentication_returnsTrue() {
        var auth = new UsernamePasswordAuthenticationToken("testuser", "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(SecurityUtils.isAuthenticated()).isTrue();
    }

    @Test
    void isAuthenticated_withNoAuthentication_returnsFalse() {
        assertThat(SecurityUtils.isAuthenticated()).isFalse();
    }
}
