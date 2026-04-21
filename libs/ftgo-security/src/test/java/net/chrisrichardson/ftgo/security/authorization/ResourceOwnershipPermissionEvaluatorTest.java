package net.chrisrichardson.ftgo.security.authorization;

import net.chrisrichardson.ftgo.security.jwt.JwtClaimNames;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceOwnershipPermissionEvaluatorTest {

    private final ResourceOwnershipPermissionEvaluator evaluator =
            new ResourceOwnershipPermissionEvaluator();

    @Test
    void hasPermission_anonymousUser_returnsFalse() {
        Authentication anonymous = new AnonymousAuthenticationToken(
                "key", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        assertThat(evaluator.hasPermission(anonymous, "user-1", "own")).isFalse();
        assertThat(evaluator.hasPermission(anonymous, "user-1", "consumer", "own")).isFalse();
    }

    @Test
    void hasPermission_nullAuthentication_returnsFalse() {
        assertThat(evaluator.hasPermission(null, "user-1", "own")).isFalse();
    }

    @Test
    void hasPermission_nullTarget_returnsFalse() {
        Authentication auth = jwtAuth("user-1", List.of(Role.CUSTOMER.authority()));
        assertThat(evaluator.hasPermission(auth, null, "own")).isFalse();
    }

    @Test
    void hasPermission_nullPermission_returnsFalse() {
        Authentication auth = jwtAuth("user-1", List.of(Role.CUSTOMER.authority()));
        assertThat(evaluator.hasPermission(auth, "user-1", null)).isFalse();
    }

    @Test
    void hasPermission_ownerMatchesUserId_returnsTrue() {
        Authentication auth = jwtAuth("user-42", List.of(Role.CUSTOMER.authority()));

        assertThat(evaluator.hasPermission(auth, "user-42", "own")).isTrue();
    }

    @Test
    void hasPermission_ownerDoesNotMatchUserId_returnsFalse() {
        Authentication auth = jwtAuth("user-42", List.of(Role.CUSTOMER.authority()));

        assertThat(evaluator.hasPermission(auth, "user-99", "own")).isFalse();
    }

    @Test
    void hasPermission_ownedResourceMatches_returnsTrue() {
        Authentication auth = jwtAuth("user-42", List.of(Role.CUSTOMER.authority()));
        OwnedResource resource = () -> "user-42";

        assertThat(evaluator.hasPermission(auth, resource, "own")).isTrue();
    }

    @Test
    void hasPermission_ownedResourceMismatch_returnsFalse() {
        Authentication auth = jwtAuth("user-42", List.of(Role.CUSTOMER.authority()));
        OwnedResource resource = () -> "user-99";

        assertThat(evaluator.hasPermission(auth, resource, "own")).isFalse();
    }

    @Test
    void hasPermission_ownedResourceWithNullOwner_returnsFalse() {
        Authentication auth = jwtAuth("user-42", List.of(Role.CUSTOMER.authority()));
        OwnedResource resource = () -> null;

        assertThat(evaluator.hasPermission(auth, resource, "own")).isFalse();
    }

    @Test
    void hasPermission_adminUser_bypassesOwnershipCheck() {
        Authentication auth = jwtAuth("user-admin", List.of(Role.ADMIN.authority()));

        assertThat(evaluator.hasPermission(auth, "user-99", "own")).isTrue();
        assertThat(evaluator.hasPermission(auth, (OwnedResource) () -> "user-99", "own")).isTrue();
        // ADMIN bypasses even unknown permission verbs — by design, ADMIN is
        // the catch-all escape hatch for ownership-gated operations.
        assertThat(evaluator.hasPermission(auth, "user-99", "read")).isTrue();
    }

    @Test
    void hasPermission_unknownPermissionVerbWithoutAdmin_returnsFalse() {
        Authentication auth = jwtAuth("user-42", List.of(Role.CUSTOMER.authority()));

        assertThat(evaluator.hasPermission(auth, "user-42", "read")).isFalse();
        assertThat(evaluator.hasPermission(auth, "user-42", "delete")).isFalse();
    }

    @Test
    void hasPermission_nonJwtAuthentication_returnsFalseBecauseNoUserIdClaim() {
        // HTTP Basic authentication produces a UsernamePasswordAuthenticationToken
        // without a JWT — the evaluator cannot derive a user_id and must deny.
        Authentication basic = new UsernamePasswordAuthenticationToken(
                "alice", "ignored",
                AuthorityUtils.createAuthorityList(Role.CUSTOMER.authority()));

        assertThat(evaluator.hasPermission(basic, "alice", "own")).isFalse();
    }

    @Test
    void hasPermission_withTargetIdAndTargetType_delegatesToObjectOverload() {
        Authentication auth = jwtAuth("user-42", List.of(Role.CUSTOMER.authority()));

        assertThat(evaluator.hasPermission(auth, "user-42", "consumer", "own")).isTrue();
        assertThat(evaluator.hasPermission(auth, "user-99", "consumer", "own")).isFalse();
    }

    private static JwtAuthenticationToken jwtAuth(String userId, List<String> authorities) {
        Jwt jwt = Jwt.withTokenValue("ignored")
                .header("alg", "HS256")
                .subject(userId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(600))
                .claim(JwtClaimNames.USER_ID, userId)
                .claim(JwtClaimNames.USERNAME, "user")
                .claim(JwtClaimNames.TOKEN_TYPE, "access")
                .build();
        Collection<GrantedAuthority> grants = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(java.util.stream.Collectors.toList());
        return new JwtAuthenticationToken(jwt, grants, userId);
    }
}
