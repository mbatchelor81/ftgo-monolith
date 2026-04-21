package net.chrisrichardson.ftgo.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class FtgoJwtAuthenticationConverterTest {

    private final FtgoJwtAuthenticationConverter converter = new FtgoJwtAuthenticationConverter();

    @Test
    void convert_accessToken_exposesRolesAndPermissionsAsAuthorities() {
        Jwt jwt = accessToken("user-42", "alice", List.of("CONSUMER", "ADMIN"),
                List.of("order:read", "order:write"));

        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth.getName()).isEqualTo("alice");
        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertThat(authorities).containsExactlyInAnyOrder(
                "ROLE_CONSUMER", "ROLE_ADMIN", "PERM_order:read", "PERM_order:write");

        // Principal is not stored on the token (JwtAuthenticationProvider would
        // overwrite Authentication.getDetails); it is rebuilt from the JWT by
        // SecurityUtils.getCurrentPrincipal() at call time.
        FtgoJwtPrincipal principal = FtgoJwtPrincipal.fromJwt(jwt);
        assertThat(principal.userId()).isEqualTo("user-42");
        assertThat(principal.username()).isEqualTo("alice");
        assertThat(principal.roles()).containsExactlyInAnyOrder("CONSUMER", "ADMIN");
        assertThat(principal.permissions()).containsExactlyInAnyOrder("order:read", "order:write");
        assertThat(principal.hasRole("CONSUMER")).isTrue();
        assertThat(principal.hasPermission("order:read")).isTrue();
    }

    @Test
    void convert_refreshToken_stripsAuthoritiesSoAccessIsDenied() {
        Jwt jwt = Jwt.withTokenValue("ignored")
                .header("alg", "HS256")
                .subject("user-42")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(600))
                .claim(JwtClaimNames.USER_ID, "user-42")
                .claim(JwtClaimNames.USERNAME, "alice")
                .claim(JwtClaimNames.TOKEN_TYPE, JwtTokenType.REFRESH.claimValue())
                .claim(JwtClaimNames.ROLES, List.of("CONSUMER"))
                .claim(JwtClaimNames.PERMISSIONS, List.of("order:read"))
                .build();

        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth.getAuthorities()).isEmpty();
    }

    @Test
    void convert_tokenWithoutUsername_fallsBackToSubject() {
        Jwt jwt = Jwt.withTokenValue("ignored")
                .header("alg", "HS256")
                .subject("user-42")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(600))
                .claim(JwtClaimNames.USER_ID, "user-42")
                .claim(JwtClaimNames.TOKEN_TYPE, JwtTokenType.ACCESS.claimValue())
                .build();

        AbstractAuthenticationToken auth = converter.convert(jwt);

        assertThat(auth.getName()).isEqualTo("user-42");
        assertThat(FtgoJwtPrincipal.fromJwt(jwt).username()).isEqualTo("user-42");
    }

    private static Jwt accessToken(String userId, String username,
                                   List<String> roles, List<String> permissions) {
        return Jwt.withTokenValue("ignored")
                .header("alg", "HS256")
                .subject(userId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(600))
                .claim(JwtClaimNames.USER_ID, userId)
                .claim(JwtClaimNames.USERNAME, username)
                .claim(JwtClaimNames.TOKEN_TYPE, JwtTokenType.ACCESS.claimValue())
                .claim(JwtClaimNames.ROLES, roles)
                .claim(JwtClaimNames.PERMISSIONS, permissions)
                .build();
    }
}
