package net.chrisrichardson.ftgo.security.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Converts a decoded {@link Jwt} into a {@link JwtAuthenticationToken} whose
 * principal is a {@link FtgoJwtPrincipal} and whose authorities are drawn
 * from the {@code roles} and {@code permissions} claims.
 *
 * <p>Authority mapping:
 * <ul>
 *   <li>Role {@code CONSUMER} &rarr; authority {@code ROLE_CONSUMER}.</li>
 *   <li>Permission {@code order:read} &rarr; authority {@code PERM_order:read}.</li>
 * </ul>
 *
 * <p>Only access tokens are accepted — refresh tokens carry
 * {@code token_type=refresh} and must not be usable to call protected
 * endpoints, so they are rejected with an empty authority set so Spring
 * Security denies the request.
 */
public class FtgoJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    public static final String ROLE_AUTHORITY_PREFIX = "ROLE_";
    public static final String PERMISSION_AUTHORITY_PREFIX = "PERM_";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // NOTE: we deliberately do NOT call token.setDetails(principal). Spring
        // Security's JwtAuthenticationProvider.authenticate() unconditionally
        // overwrites details with WebAuthenticationDetails after the converter
        // returns. Consumers must retrieve the principal via
        // SecurityUtils.getCurrentPrincipal(), which rebuilds it from the
        // decoded Jwt using FtgoJwtPrincipal.fromJwt(...).
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities, resolveUsername(jwt));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        if (!isAccessToken(jwt)) {
            return Collections.emptyList();
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : extractClaimAsSet(jwt, JwtClaimNames.ROLES)) {
            authorities.add(new SimpleGrantedAuthority(ROLE_AUTHORITY_PREFIX + role));
        }
        for (String permission : extractClaimAsSet(jwt, JwtClaimNames.PERMISSIONS)) {
            authorities.add(new SimpleGrantedAuthority(PERMISSION_AUTHORITY_PREFIX + permission));
        }
        return authorities;
    }

    private boolean isAccessToken(Jwt jwt) {
        String tokenType = jwt.getClaimAsString(JwtClaimNames.TOKEN_TYPE);
        return tokenType == null || JwtTokenType.ACCESS.claimValue().equals(tokenType);
    }

    private static String resolveUsername(Jwt jwt) {
        String username = jwt.getClaimAsString(JwtClaimNames.USERNAME);
        return username != null ? username : jwt.getSubject();
    }

    private static Set<String> extractClaimAsSet(Jwt jwt, String claim) {
        List<String> values = jwt.getClaimAsStringList(claim);
        return values == null ? Set.of() : new LinkedHashSet<>(values);
    }
}
