package com.ftgo.security.jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Converts a validated {@link Jwt} into a Spring Security {@link JwtAuthenticationToken}
 * populated with FTGO-specific authorities derived from the token's
 * {@code roles} and {@code permissions} claims.
 *
 * <p>The resulting authentication token stores an {@link FtgoUserDetails}
 * as its {@code details} property, making it accessible throughout the
 * request lifecycle via {@code SecurityContextHolder}.
 */
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtClaimsExtractor claimsExtractor;

    public JwtAuthenticationConverter(JwtClaimsExtractor claimsExtractor) {
        this.claimsExtractor = claimsExtractor;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        FtgoUserDetails userDetails = claimsExtractor.extractUserDetails(jwt);
        Collection<GrantedAuthority> authorities = buildAuthorities(userDetails);

        JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt, authorities, userDetails.getUsername());
        authToken.setDetails(userDetails);
        return authToken;
    }

    private Collection<GrantedAuthority> buildAuthorities(FtgoUserDetails userDetails) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        for (String role : userDetails.getRoles()) {
            String prefixed = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            authorities.add(new SimpleGrantedAuthority(prefixed));
        }

        for (String permission : userDetails.getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }

        return authorities;
    }
}
