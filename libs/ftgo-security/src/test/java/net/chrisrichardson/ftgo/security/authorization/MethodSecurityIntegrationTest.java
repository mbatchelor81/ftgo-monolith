package net.chrisrichardson.ftgo.security.authorization;

import net.chrisrichardson.ftgo.security.jwt.JwtClaimNames;
import net.chrisrichardson.ftgo.security.jwt.JwtTokenType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration test for EM-37 RBAC. Boots a {@link SpringBootApplication}
 * that imports the full FTGO security auto-configuration and hits four
 * {@code @PreAuthorize}-gated endpoints with JWTs carrying different roles /
 * permissions / user ids. Verifies:
 *
 * <ul>
 *   <li>Role-hasAuthority gating — a {@code CUSTOMER}-only endpoint.</li>
 *   <li>Permission gating — a {@code PERM_order:admin}-gated endpoint.</li>
 *   <li>Role hierarchy inheritance — an {@code ADMIN} token reaches a
 *       {@code hasRole('CUSTOMER')}-gated endpoint.</li>
 *   <li>Resource ownership — a {@code /accounts/{id}} endpoint that allows
 *       owners and admins but denies other authenticated users.</li>
 *   <li>Unauthenticated requests return 401, denied requests return 403.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "ftgo.security.jwt.secret=" + MethodSecurityIntegrationTest.JWT_SECRET,
        "ftgo.security.jwt.issuer=ftgo-auth",
        "ftgo.security.jwt.audience=ftgo-services",
        "ftgo.security.jwt.access-token-ttl=PT5M",
        "ftgo.security.jwt.refresh-token-ttl=PT30M"
})
class MethodSecurityIntegrationTest {

    static final String JWT_SECRET = "test-secret-key-for-ftgo-security-library-integration-test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder encoder;

    // ----- Role-gated endpoint -----------------------------------------

    @Test
    void customerEndpoint_withCustomerRole_returns200() throws Exception {
        mockMvc.perform(get("/customer-only")
                        .header("Authorization", "Bearer " + issue("user-1", Role.CUSTOMER)))
                .andExpect(status().isOk());
    }

    @Test
    void customerEndpoint_withCourierRole_returns200_viaRoleHierarchy() throws Exception {
        // COURIER inherits CUSTOMER via the role hierarchy.
        mockMvc.perform(get("/customer-only")
                        .header("Authorization", "Bearer " + issue("user-2", Role.COURIER)))
                .andExpect(status().isOk());
    }

    @Test
    void customerEndpoint_withAdminRole_returns200_viaRoleHierarchy() throws Exception {
        mockMvc.perform(get("/customer-only")
                        .header("Authorization", "Bearer " + issue("user-3", Role.ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void customerEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/customer-only").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // ----- Permission-gated endpoint -----------------------------------

    @Test
    void orderAdminEndpoint_withOrderAdminPermission_returns200() throws Exception {
        mockMvc.perform(get("/orders/admin")
                        .header("Authorization", "Bearer " + issue(
                                "user-4", List.of(Role.RESTAURANT_OWNER), List.of("order:admin"))))
                .andExpect(status().isOk());
    }

    @Test
    void orderAdminEndpoint_withoutOrderAdminPermission_returns403() throws Exception {
        mockMvc.perform(get("/orders/admin")
                        .header("Authorization", "Bearer " + issue(
                                "user-5", List.of(Role.CUSTOMER), List.of("order:read")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    // ----- Resource ownership ------------------------------------------

    @Test
    void accountEndpoint_ownerAccessingOwnResource_returns200() throws Exception {
        mockMvc.perform(get("/accounts/user-42")
                        .header("Authorization", "Bearer " + issue("user-42", Role.CUSTOMER)))
                .andExpect(status().isOk());
    }

    @Test
    void accountEndpoint_nonOwnerNonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/accounts/user-42")
                        .header("Authorization", "Bearer " + issue("user-99", Role.CUSTOMER))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void accountEndpoint_adminAccessingAnyResource_returns200() throws Exception {
        mockMvc.perform(get("/accounts/user-42")
                        .header("Authorization", "Bearer " + issue("user-admin", Role.ADMIN)))
                .andExpect(status().isOk());
    }

    // ----- Helpers -----------------------------------------------------

    private String issue(String userId, Role role) {
        return issue(userId, List.of(role), List.of());
    }

    private String issue(String userId, List<Role> roles, List<String> permissions) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ftgo-auth")
                .audience(List.of("ftgo-services"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .subject(userId)
                .id(UUID.randomUUID().toString())
                .claim(JwtClaimNames.TOKEN_TYPE, JwtTokenType.ACCESS.claimValue())
                .claim(JwtClaimNames.USER_ID, userId)
                .claim(JwtClaimNames.USERNAME, "user-" + userId)
                .claim(JwtClaimNames.ROLES, roles.stream().map(Role::claimValue).toList())
                .claim(JwtClaimNames.PERMISSIONS, permissions)
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    @SpringBootApplication
    static class TestApplication {

        @RestController
        static class TestController {

            @GetMapping("/customer-only")
            @PreAuthorize("hasRole('CUSTOMER')")
            public String customerOnly() {
                return "customer-only";
            }

            @GetMapping("/orders/admin")
            @PreAuthorize("hasAuthority('PERM_order:admin')")
            public String orderAdmin() {
                return "order-admin";
            }

            @GetMapping("/accounts/{id}")
            @PostAuthorize("hasPermission(returnObject, 'own')")
            public AccountResource getAccount(@PathVariable String id) {
                return new AccountResource(id);
            }
        }

        record AccountResource(String id) implements OwnedResource {
            @Override
            public String getOwnerId() {
                return id;
            }
        }
    }
}
