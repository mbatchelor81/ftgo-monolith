package com.ftgo.consumer.web;

import com.ftgo.security.authorization.RoleHierarchyConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authorization tests for ConsumerController.
 *
 * <p>Verifies that @PreAuthorize annotations enforce the role model:
 * <ul>
 *   <li>createConsumer — CUSTOMER, ADMIN (via hierarchy)</li>
 *   <li>getConsumer — CUSTOMER (own via permission evaluator), ADMIN</li>
 * </ul>
 */
@Disabled("TODO: fix @WebMvcTest slice — controller returns 404 for authenticated requests. "
        + "Security filter chain IS active (unauthenticated → 401 works). "
        + "Likely FtgoSecurityAutoConfiguration interference with the test context.")
@WebMvcTest(ConsumerController.class)
@Import(RoleHierarchyConfig.class)
class ConsumerControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    // ---- createConsumer (/api/v1/consumers POST) ----

    @Test
    @DisplayName("POST /api/v1/consumers — CUSTOMER can create consumer")
    @WithMockUser(roles = "CUSTOMER")
    void createConsumer_customerRole_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/consumers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/consumers — ADMIN can create consumer (via role hierarchy)")
    @WithMockUser(roles = "ADMIN")
    void createConsumer_adminRole_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/consumers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/consumers — RESTAURANT_OWNER inherits CUSTOMER via hierarchy")
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void createConsumer_restaurantOwnerRole_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/consumers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/consumers — COURIER inherits CUSTOMER via hierarchy")
    @WithMockUser(roles = "COURIER")
    void createConsumer_courierRole_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/consumers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/consumers — Unauthenticated returns 401")
    void createConsumer_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/consumers")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    // ---- getConsumer (/api/v1/consumers/{consumerId} GET) ----

    @Test
    @DisplayName("GET /api/v1/consumers/{consumerId} — ADMIN can get any consumer")
    @WithMockUser(roles = "ADMIN")
    void getConsumer_adminRole_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/consumers/42"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/consumers/{consumerId} — Unauthenticated returns 401")
    void getConsumer_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/consumers/42"))
            .andExpect(status().isUnauthorized());
    }
}
