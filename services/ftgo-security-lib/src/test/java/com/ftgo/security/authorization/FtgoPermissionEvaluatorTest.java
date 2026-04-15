package com.ftgo.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ftgo.security.jwt.FtgoUserDetails;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Unit tests for {@link FtgoPermissionEvaluator}. */
@DisplayName("FtgoPermissionEvaluator")
@ExtendWith(MockitoExtension.class)
class FtgoPermissionEvaluatorTest {

    @Mock private ResourceOwnershipResolver orderOwnershipResolver;

    private FtgoPermissionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new FtgoPermissionEvaluator(List.of(orderOwnershipResolver));
    }

    private JwtAuthenticationToken createAuth(String userId, String username, Set<String> roles) {
        Jwt jwt =
                Jwt.withTokenValue("test-token")
                        .header("alg", "RS256")
                        .claim("sub", username)
                        .claim("userId", userId)
                        .claim("roles", roles)
                        .build();

        List<SimpleGrantedAuthority> authorities =
                roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();

        JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt, authorities, username);
        FtgoUserDetails details = new FtgoUserDetails(userId, username, roles, Set.of(), Map.of());
        authToken.setDetails(details);
        return authToken;
    }

    @Nested
    @DisplayName("hasPermission(auth, targetObject, permission)")
    class ObjectPermission {

        @Test
        @DisplayName("should deny when authentication is null")
        void shouldDenyWhenAuthNull() {
            assertThat(evaluator.hasPermission(null, new Object(), "order:read")).isFalse();
        }

        @Test
        @DisplayName("should deny when permission is null")
        void shouldDenyWhenPermissionNull() {
            JwtAuthenticationToken auth = createAuth("u1", "user", Set.of("CUSTOMER"));
            assertThat(evaluator.hasPermission(auth, new Object(), null)).isFalse();
        }

        @Test
        @DisplayName("ADMIN should have any permission")
        void admin_shouldHaveAnyPermission() {
            JwtAuthenticationToken auth = createAuth("admin-1", "admin", Set.of("ADMIN"));
            assertThat(evaluator.hasPermission(auth, new Object(), "order:read")).isTrue();
            assertThat(evaluator.hasPermission(auth, new Object(), "consumer:create")).isTrue();
            assertThat(evaluator.hasPermission(auth, new Object(), "restaurant:delete")).isTrue();
        }

        @Test
        @DisplayName("CUSTOMER should have order:create permission")
        void customer_shouldHaveOrderCreate() {
            JwtAuthenticationToken auth = createAuth("c1", "customer", Set.of("CUSTOMER"));
            assertThat(evaluator.hasPermission(auth, new Object(), FtgoPermission.ORDER_CREATE))
                    .isTrue();
        }

        @Test
        @DisplayName("CUSTOMER should NOT have consumer:create permission")
        void customer_shouldNotHaveConsumerCreate() {
            JwtAuthenticationToken auth = createAuth("c1", "customer", Set.of("CUSTOMER"));
            assertThat(evaluator.hasPermission(auth, new Object(), FtgoPermission.CONSUMER_CREATE))
                    .isFalse();
        }

        @Test
        @DisplayName("RESTAURANT_OWNER should have order:accept permission")
        void restaurantOwner_shouldHaveOrderAccept() {
            JwtAuthenticationToken auth = createAuth("ro1", "owner", Set.of("RESTAURANT_OWNER"));
            assertThat(evaluator.hasPermission(auth, new Object(), FtgoPermission.ORDER_ACCEPT))
                    .isTrue();
        }

        @Test
        @DisplayName("COURIER should have courier:delivery:update permission")
        void courier_shouldHaveCourierDeliveryUpdate() {
            JwtAuthenticationToken auth = createAuth("cr1", "courier", Set.of("COURIER"));
            assertThat(
                            evaluator.hasPermission(
                                    auth, new Object(), FtgoPermission.COURIER_DELIVERY_UPDATE))
                    .isTrue();
        }

        @Test
        @DisplayName("COURIER should NOT have restaurant:create permission")
        void courier_shouldNotHaveRestaurantCreate() {
            JwtAuthenticationToken auth = createAuth("cr1", "courier", Set.of("COURIER"));
            assertThat(
                            evaluator.hasPermission(
                                    auth, new Object(), FtgoPermission.RESTAURANT_CREATE))
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("hasPermission(auth, targetId, targetType, permission)")
    class IdPermission {

        @Test
        @DisplayName("should deny when authentication is null")
        void shouldDenyWhenAuthNull() {
            assertThat(evaluator.hasPermission(null, 1L, "Order", "order:read")).isFalse();
        }

        @Test
        @DisplayName("should deny when targetType is null")
        void shouldDenyWhenTargetTypeNull() {
            JwtAuthenticationToken auth = createAuth("u1", "user", Set.of("CUSTOMER"));
            assertThat(evaluator.hasPermission(auth, 1L, null, "order:read")).isFalse();
        }

        @Test
        @DisplayName("ADMIN should have permission on any resource")
        void admin_shouldHavePermissionOnAnyResource() {
            JwtAuthenticationToken auth = createAuth("admin-1", "admin", Set.of("ADMIN"));
            assertThat(evaluator.hasPermission(auth, 1L, "Order", "order:read")).isTrue();
        }

        @Test
        @DisplayName("CUSTOMER with :own permission should check ownership")
        void customer_withOwnPermission_shouldCheckOwnership() {
            JwtAuthenticationToken auth = createAuth("user-42", "customer", Set.of("CUSTOMER"));
            when(orderOwnershipResolver.supports("Order")).thenReturn(true);
            when(orderOwnershipResolver.isOwner("user-42", 1L, "Order")).thenReturn(true);

            assertThat(evaluator.hasPermission(auth, 1L, "Order", "order:read:own")).isTrue();
        }

        @Test
        @DisplayName("CUSTOMER with :own permission should deny non-owner")
        void customer_withOwnPermission_shouldDenyNonOwner() {
            JwtAuthenticationToken auth = createAuth("user-42", "customer", Set.of("CUSTOMER"));
            when(orderOwnershipResolver.supports("Order")).thenReturn(true);
            when(orderOwnershipResolver.isOwner("user-42", 1L, "Order")).thenReturn(false);

            assertThat(evaluator.hasPermission(auth, 1L, "Order", "order:read:own")).isFalse();
        }

        @Test
        @DisplayName("should deny :own permission when no resolver found for resource type")
        void shouldDenyWhenNoResolverFound() {
            JwtAuthenticationToken auth = createAuth("u1", "user", Set.of("CUSTOMER"));
            when(orderOwnershipResolver.supports("UnknownType")).thenReturn(false);

            assertThat(evaluator.hasPermission(auth, 1L, "UnknownType", "order:read:own"))
                    .isFalse();
        }

        @Test
        @DisplayName("non-own permission should be resolved via role mapping only")
        void nonOwnPermission_shouldUseRoleMappingOnly() {
            JwtAuthenticationToken auth = createAuth("ro1", "owner", Set.of("RESTAURANT_OWNER"));
            // order:read is in RESTAURANT_OWNER's permissions
            assertThat(evaluator.hasPermission(auth, 1L, "Order", "order:read")).isTrue();
        }
    }

    @Nested
    @DisplayName("with no ownership resolvers")
    class NoResolvers {

        @BeforeEach
        void setUp() {
            evaluator = new FtgoPermissionEvaluator(null);
        }

        @Test
        @DisplayName("should still evaluate role-based permissions")
        void shouldEvaluateRoleBasedPermissions() {
            JwtAuthenticationToken auth = createAuth("c1", "customer", Set.of("CUSTOMER"));
            assertThat(evaluator.hasPermission(auth, new Object(), FtgoPermission.ORDER_CREATE))
                    .isTrue();
        }

        @Test
        @DisplayName(":own permission should deny when no resolvers available")
        void ownPermission_shouldDenyWithNoResolvers() {
            JwtAuthenticationToken auth = createAuth("c1", "customer", Set.of("CUSTOMER"));
            assertThat(evaluator.hasPermission(auth, 1L, "Order", "order:read:own")).isFalse();
        }
    }

    @Nested
    @DisplayName("direct authority match")
    class DirectAuthorityMatch {

        @Test
        @DisplayName("should grant permission when directly in JWT authorities")
        void shouldGrantWhenDirectlyInAuthorities() {
            Jwt jwt =
                    Jwt.withTokenValue("test-token")
                            .header("alg", "RS256")
                            .claim("sub", "user")
                            .claim("userId", "u1")
                            .build();

            List<SimpleGrantedAuthority> authorities =
                    List.of(
                            new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                            new SimpleGrantedAuthority("special:custom:permission"));

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt, authorities, "user");
            FtgoUserDetails details =
                    new FtgoUserDetails("u1", "user", Set.of("CUSTOMER"), Set.of(), Map.of());
            authToken.setDetails(details);

            assertThat(
                            evaluator.hasPermission(
                                    authToken, new Object(), "special:custom:permission"))
                    .isTrue();
        }
    }
}
