package net.chrisrichardson.ftgo.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
    void getCurrentUsername_withAuthenticatedUser_returnsUsername() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", "pw", List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        assertThat(SecurityUtils.getCurrentUsername()).contains("alice");
        assertThat(SecurityUtils.isAuthenticated()).isTrue();
        assertThat(SecurityUtils.getCurrentAuthorities()).containsExactly("ROLE_USER");
    }

    @Test
    void getCurrentUsername_withoutAuthentication_returnsEmpty() {
        assertThat(SecurityUtils.getCurrentUsername()).isEmpty();
        assertThat(SecurityUtils.isAuthenticated()).isFalse();
        assertThat(SecurityUtils.getCurrentAuthorities()).isEmpty();
    }

    @Test
    void getCurrentUsername_withAnonymousUser_returnsEmpty() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")))
        );

        assertThat(SecurityUtils.getCurrentUsername()).isEmpty();
        assertThat(SecurityUtils.isAuthenticated()).isFalse();
    }
}
