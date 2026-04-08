package com.ftgo.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link JwtAuthenticationFilter}.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String SECRET = "this-is-a-very-secure-secret-key-for-testing-purposes-only-at-least-32-chars";
    private static final String ISSUER = "ftgo-test";

    private JwtTokenProvider tokenProvider;
    private JwtAuthenticationFilter filter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setIssuer(ISSUER);
        properties.setExpiration(1800);
        properties.setRefreshExpiration(86400);
        tokenProvider = new JwtTokenProvider(properties);
        filter = new JwtAuthenticationFilter(tokenProvider);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Valid access token sets authentication in SecurityContext")
    void doFilter_validAccessToken_setsAuthentication() throws ServletException, IOException {
        String token = tokenProvider.generateAccessToken("user-42",
            Set.of("ROLE_ADMIN"), Set.of("order:read"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull().isInstanceOf(JwtAuthenticationToken.class);

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
        assertThat(jwtAuth.getPrincipal()).isEqualTo("user-42");
        assertThat(jwtAuth.isAuthenticated()).isTrue();

        FtgoUserDetails userDetails = jwtAuth.getUserDetails();
        assertThat(userDetails.getUserId()).isEqualTo("user-42");
        assertThat(userDetails.getRoles()).containsExactly("ROLE_ADMIN");
        assertThat(userDetails.getPermissions()).containsExactly("order:read");
    }

    @Test
    @DisplayName("No Authorization header does not set authentication")
    void doFilter_noAuthHeader_noAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Invalid token does not set authentication")
    void doFilter_invalidToken_noAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Expired token does not set authentication")
    void doFilter_expiredToken_noAuthentication() throws ServletException, IOException {
        JwtProperties expiredProps = new JwtProperties();
        expiredProps.setSecret(SECRET);
        expiredProps.setIssuer(ISSUER);
        expiredProps.setExpiration(0);
        expiredProps.setRefreshExpiration(0);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);

        String token = expiredProvider.generateAccessToken("user-1", Set.of(), Set.of());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Refresh token does not set authentication (only access tokens allowed)")
    void doFilter_refreshToken_noAuthentication() throws ServletException, IOException {
        String refreshToken = tokenProvider.generateRefreshToken("user-42");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + refreshToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Non-Bearer authorization header does not set authentication")
    void doFilter_nonBearerAuth_noAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Roles are mapped to Spring Security authorities with ROLE_ prefix")
    void doFilter_rolesWithoutPrefix_addsRolePrefix() throws ServletException, IOException {
        String token = tokenProvider.generateAccessToken("user-1",
            Set.of("ADMIN"), Set.of("order:read"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities())
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            .anyMatch(a -> a.getAuthority().equals("order:read"));
    }
}
