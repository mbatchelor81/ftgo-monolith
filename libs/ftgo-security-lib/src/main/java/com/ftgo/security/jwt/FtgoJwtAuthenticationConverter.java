package com.ftgo.security.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Converts a decoded JWT into a {@link JwtAuthenticationToken} with
 * authorities derived from both role and permission claims.
 *
 * <p>Roles are prefixed with the configured prefix (default {@code ROLE_}).
 * Permissions are mapped directly as granted authorities.
 */
public class FtgoJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtClaimsExtractor claimsExtractor;
    private final FtgoJwtProperties properties;

    public FtgoJwtAuthenticationConverter(JwtClaimsExtractor claimsExtractor,
                                          FtgoJwtProperties properties) {
        this.claimsExtractor = claimsExtractor;
        this.properties = properties;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        String principalName = claimsExtractor.extractUserId(jwt);
        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        List<String> roles = claimsExtractor.extractRoles(jwt);
        String prefix = properties.getRolePrefix();
        for (String role : roles) {
            String authority = role.startsWith(prefix) ? role : prefix + role;
            authorities.add(new SimpleGrantedAuthority(authority));
        }

        List<String> permissions = claimsExtractor.extractPermissions(jwt);
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }

        return authorities;
    }
}
