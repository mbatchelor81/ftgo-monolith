package com.ftgo.security.jwt;

import com.ftgo.security.util.RequestUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Filter that intercepts incoming HTTP requests to extract and validate JWT
 * tokens from the {@code Authorization: Bearer <token>} header.
 *
 * <p>On successful validation, a {@link JwtAuthenticationToken} is placed
 * into the {@link SecurityContextHolder}, making the authenticated user's
 * details available throughout the request lifecycle.
 *
 * <p>If no Bearer token is present, or if the token is invalid/expired,
 * the filter does nothing and allows the request to proceed to the next
 * filter in the chain (where Spring Security's default authentication
 * entry point will return 401 if authentication is required).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        Optional<String> tokenOpt = RequestUtils.extractBearerToken(request);

        if (tokenOpt.isPresent()) {
            String token = tokenOpt.get();
            try {
                if (tokenProvider.validateToken(token)) {
                    String tokenType = tokenProvider.getTokenType(token);
                    if (!JwtTokenProvider.TOKEN_TYPE_ACCESS.equals(tokenType)) {
                        log.debug("Rejecting non-access token type: {}", tokenType);
                        filterChain.doFilter(request, response);
                        return;
                    }

                    String userId = tokenProvider.getUserId(token);
                    Set<String> roles = tokenProvider.getRoles(token);
                    Set<String> permissions = tokenProvider.getPermissions(token);

                    Collection<GrantedAuthority> authorities = buildAuthorities(roles, permissions);
                    FtgoUserDetails userDetails = new FtgoUserDetails(userId, roles, permissions, authorities);
                    JwtAuthenticationToken authentication =
                        new JwtAuthenticationToken(userId, token, userDetails, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT authentication set for user: {}", userId);
                }
            } catch (Exception e) {
                log.debug("JWT authentication failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Builds Spring Security granted authorities from roles and permissions.
     * Roles are prefixed with "ROLE_" if not already prefixed.
     */
    private Collection<GrantedAuthority> buildAuthorities(Set<String> roles, Set<String> permissions) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            authorities.add(new SimpleGrantedAuthority(authorityName));
        }
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        return authorities;
    }
}
