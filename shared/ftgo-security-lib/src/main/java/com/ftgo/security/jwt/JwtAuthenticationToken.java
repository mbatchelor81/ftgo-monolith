package com.ftgo.security.jwt;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Authentication token representing a validated JWT.
 *
 * <p>Stored in the {@link org.springframework.security.core.context.SecurityContextHolder}
 * after successful JWT validation. The {@link #getPrincipal()} returns the user ID
 * (the JWT {@code sub} claim), and {@link #getDetails()} returns the full
 * {@link FtgoUserDetails}.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String userId;
    private final String token;
    private final FtgoUserDetails userDetails;

    /**
     * Creates an authenticated JWT token.
     *
     * @param userId      the user identifier from the JWT {@code sub} claim
     * @param token       the raw JWT string
     * @param userDetails the extracted user details
     * @param authorities the granted authorities derived from JWT claims
     */
    public JwtAuthenticationToken(String userId,
                                  String token,
                                  FtgoUserDetails userDetails,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userId = userId;
        this.token = token;
        this.userDetails = userDetails;
        setAuthenticated(true);
        setDetails(userDetails);
    }

    /**
     * Returns the raw JWT token string.
     */
    @Override
    public Object getCredentials() {
        return token;
    }

    /**
     * Returns the user ID (JWT {@code sub} claim).
     */
    @Override
    public Object getPrincipal() {
        return userId;
    }

    /**
     * Returns the extracted user details.
     */
    public FtgoUserDetails getUserDetails() {
        return userDetails;
    }
}
