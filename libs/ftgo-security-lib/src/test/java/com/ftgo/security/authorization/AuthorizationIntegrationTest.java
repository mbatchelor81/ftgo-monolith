package com.ftgo.security.authorization;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("jwt-test")
class AuthorizationIntegrationTest {

    private static RSAKey rsaKey;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void generateKey() throws Exception {
        rsaKey = new RSAKeyGenerator(2048)
                .keyID("authz-test-key")
                .generate();
    }

    @Test
    void customerCanAccessOrderCreate() throws Exception {
        String token = createJwt("customer-1", List.of("CUSTOMER"), List.of("order:create"));

        mockMvc.perform(get("/api/authz/order-create")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("created"));
    }

    @Test
    void courierCannotAccessOrderCreate() throws Exception {
        String token = createJwt("courier-1", List.of("COURIER"), List.of("order:read"));

        mockMvc.perform(get("/api/authz/order-create")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAnyEndpoint() throws Exception {
        String token = createJwt("admin-1", List.of("ADMIN"),
                List.of("order:create", "order:read", "restaurant:read"));

        mockMvc.perform(get("/api/authz/admin-only")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("admin-area"));
    }

    @Test
    void nonAdminCannotAccessAdminEndpoint() throws Exception {
        String token = createJwt("customer-1", List.of("CUSTOMER"), List.of("order:create"));

        mockMvc.perform(get("/api/authz/admin-only")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void restaurantOwnerCanAccessRestaurantEndpoint() throws Exception {
        String token = createJwt("owner-1", List.of("RESTAURANT_OWNER"),
                List.of("restaurant:read", "restaurant:update"));

        mockMvc.perform(get("/api/authz/restaurant-manage")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("restaurant-managed"));
    }

    @Test
    void ownerCanAccessOwnResource() throws Exception {
        String token = createJwt("owner-user-100", List.of("CUSTOMER"), List.of("order:read"));

        mockMvc.perform(get("/api/authz/orders/100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("order-100"));
    }

    @Test
    void nonOwnerCannotAccessOtherUsersResource() throws Exception {
        String token = createJwt("other-user", List.of("CUSTOMER"), List.of("order:read"));

        mockMvc.perform(get("/api/authz/orders/100")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminBypassesOwnershipCheck() throws Exception {
        String token = createJwt("admin-user", List.of("ADMIN"), List.of("order:read"));

        mockMvc.perform(get("/api/authz/orders/100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("order-100"));
    }

    @Test
    void unauthenticatedUserGetsDenied() throws Exception {
        mockMvc.perform(get("/api/authz/order-create")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void permissionAnnotationEnforcesAccess() throws Exception {
        String token = createJwt("courier-1", List.of("COURIER"),
                List.of("courier:update-availability"));

        mockMvc.perform(get("/api/authz/courier-availability")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("availability-updated"));
    }

    @Test
    void userWithoutRequiredPermissionGetsDenied() throws Exception {
        String token = createJwt("customer-1", List.of("CUSTOMER"), List.of("order:read"));

        mockMvc.perform(get("/api/authz/courier-availability")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    private String createJwt(String subject, List<String> roles, List<String> permissions) throws Exception {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(300)));

        if (!roles.isEmpty()) {
            builder.claim("realm_access", Map.of("roles", roles));
        }
        if (!permissions.isEmpty()) {
            builder.claim("permissions", permissions);
        }

        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                builder.build());
        jwt.sign(new RSASSASigner(rsaKey));
        return jwt.serialize();
    }

    @SpringBootApplication
    @RestController
    static class AuthzTestApplication {

        @PreAuthorize("hasAuthority('order:create')")
        @GetMapping("/api/authz/order-create")
        public String orderCreate() {
            return "created";
        }

        @RequireRole.Admin
        @GetMapping("/api/authz/admin-only")
        public String adminOnly() {
            return "admin-area";
        }

        @PreAuthorize("hasAuthority('restaurant:update')")
        @GetMapping("/api/authz/restaurant-manage")
        public String restaurantManage() {
            return "restaurant-managed";
        }

        @PreAuthorize("hasPermission(#orderId, 'Order', 'order:read')")
        @GetMapping("/api/authz/orders/{orderId}")
        public String getOrder(@PathVariable Long orderId) {
            return "order-" + orderId;
        }

        @RequirePermission.CourierUpdateAvailability
        @GetMapping("/api/authz/courier-availability")
        public String courierAvailability() {
            return "availability-updated";
        }

        @Bean
        public JwtDecoder jwtDecoder() throws Exception {
            return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
        }

        @Bean
        public ResourceOwnershipChecker orderOwnershipChecker() {
            return new ResourceOwnershipChecker() {
                @Override
                public String getTargetType() {
                    return "Order";
                }

                @Override
                public boolean isOwner(String userId, Serializable resourceId) {
                    return userId.equals("owner-user-" + resourceId);
                }
            };
        }
    }
}
