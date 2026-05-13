package com.ftgo.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoJwtAuthenticationConverterTest {

    @Test
    void convertsRolesWithPrefix() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        JwtClaimsExtractor extractor = new JwtClaimsExtractor(properties);
        FtgoJwtAuthenticationConverter converter = new FtgoJwtAuthenticationConverter(extractor, properties);

        Jwt jwt = buildJwt(Map.of(
                "sub", "user-1",
                "realm_access", Map.of("roles", List.of("ftgo-consumer", "ftgo-admin"))
        ));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
        assertThat(token.getName()).isEqualTo("user-1");

        Collection<String> authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertThat(authorities).containsExactlyInAnyOrder("ROLE_ftgo-consumer", "ROLE_ftgo-admin");
    }

    @Test
    void doesNotDoublePrefix() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        JwtClaimsExtractor extractor = new JwtClaimsExtractor(properties);
        FtgoJwtAuthenticationConverter converter = new FtgoJwtAuthenticationConverter(extractor, properties);

        Jwt jwt = buildJwt(Map.of(
                "sub", "user-2",
                "realm_access", Map.of("roles", List.of("ROLE_already-prefixed"))
        ));

        AbstractAuthenticationToken token = converter.convert(jwt);

        Collection<String> authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertThat(authorities).containsExactly("ROLE_already-prefixed");
    }

    @Test
    void includesPermissionsAsAuthorities() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        JwtClaimsExtractor extractor = new JwtClaimsExtractor(properties);
        FtgoJwtAuthenticationConverter converter = new FtgoJwtAuthenticationConverter(extractor, properties);

        Jwt jwt = buildJwt(Map.of(
                "sub", "user-3",
                "permissions", List.of("order:read", "order:write")
        ));

        AbstractAuthenticationToken token = converter.convert(jwt);

        Collection<String> authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertThat(authorities).containsExactlyInAnyOrder("order:read", "order:write");
    }

    @Test
    void combinesRolesAndPermissions() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        JwtClaimsExtractor extractor = new JwtClaimsExtractor(properties);
        FtgoJwtAuthenticationConverter converter = new FtgoJwtAuthenticationConverter(extractor, properties);

        Jwt jwt = buildJwt(Map.of(
                "sub", "user-4",
                "realm_access", Map.of("roles", List.of("ftgo-admin")),
                "permissions", List.of("system:manage")
        ));

        AbstractAuthenticationToken token = converter.convert(jwt);

        Collection<String> authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertThat(authorities).containsExactlyInAnyOrder("ROLE_ftgo-admin", "system:manage");
    }

    @Test
    void emptyAuthoritiesWhenNoClaims() {
        FtgoJwtProperties properties = new FtgoJwtProperties();
        JwtClaimsExtractor extractor = new JwtClaimsExtractor(properties);
        FtgoJwtAuthenticationConverter converter = new FtgoJwtAuthenticationConverter(extractor, properties);

        Jwt jwt = buildJwt(Map.of("sub", "user-5"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities()).isEmpty();
    }

    private Jwt buildJwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
        claims.forEach(builder::claim);
        return builder.build();
    }
}
