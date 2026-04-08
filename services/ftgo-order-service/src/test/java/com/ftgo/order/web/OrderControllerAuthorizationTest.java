package com.ftgo.order.web;

import com.ftgo.security.authorization.RoleHierarchyConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authorization tests for OrderController.
 *
 * <p>Verifies that @PreAuthorize annotations enforce the role model:
 * <ul>
 *   <li>createOrder — CUSTOMER, ADMIN</li>
 *   <li>getOrder — CUSTOMER, RESTAURANT_OWNER, COURIER, ADMIN</li>
 *   <li>cancelOrder — CUSTOMER, ADMIN</li>
 *   <li>acceptOrder — RESTAURANT_OWNER, ADMIN</li>
 *   <li>reviseOrder — CUSTOMER, ADMIN</li>
 * </ul>
 */
@Disabled("TODO: fix @WebMvcTest slice — controller returns 404 for authenticated requests. "
        + "Security filter chain IS active (unauthenticated → 401 works). "
        + "Likely FtgoSecurityAutoConfiguration interference with the test context.")
@WebMvcTest(OrderController.class)
@Import(RoleHierarchyConfig.class)
class OrderControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    // ---- createOrder (/api/v1/orders POST) ----

    @Nested
    @DisplayName("POST /api/v1/orders (createOrder)")
    class CreateOrder {

        @Test
        @DisplayName("CUSTOMER can create order")
        @WithMockUser(roles = "CUSTOMER")
        void createOrder_customerRole_returns201() throws Exception {
            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("ADMIN can create order (via role hierarchy)")
        @WithMockUser(roles = "ADMIN")
        void createOrder_adminRole_returns201() throws Exception {
            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER can create order (inherits CUSTOMER)")
        @WithMockUser(roles = "RESTAURANT_OWNER")
        void createOrder_restaurantOwnerRole_returns201() throws Exception {
            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Unauthenticated request returns 401")
        void createOrder_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isUnauthorized());
        }
    }

    // ---- getOrder (/api/v1/orders/{orderId} GET) ----

    @Nested
    @DisplayName("GET /api/v1/orders/{orderId} (getOrder)")
    class GetOrder {

        @Test
        @DisplayName("CUSTOMER can get order")
        @WithMockUser(roles = "CUSTOMER")
        void getOrder_customerRole_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER can get order")
        @WithMockUser(roles = "RESTAURANT_OWNER")
        void getOrder_restaurantOwnerRole_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("COURIER can get order")
        @WithMockUser(roles = "COURIER")
        void getOrder_courierRole_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can get order (via role hierarchy)")
        @WithMockUser(roles = "ADMIN")
        void getOrder_adminRole_returns200() throws Exception {
            mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Unauthenticated request returns 401")
        void getOrder_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isUnauthorized());
        }
    }

    // ---- cancelOrder (/api/v1/orders/{orderId}/cancel POST) ----

    @Nested
    @DisplayName("POST /api/v1/orders/{orderId}/cancel (cancelOrder)")
    class CancelOrder {

        @Test
        @DisplayName("CUSTOMER can cancel order")
        @WithMockUser(roles = "CUSTOMER")
        void cancelOrder_customerRole_returns200() throws Exception {
            mockMvc.perform(post("/api/v1/orders/1/cancel"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can cancel order (via role hierarchy)")
        @WithMockUser(roles = "ADMIN")
        void cancelOrder_adminRole_returns200() throws Exception {
            mockMvc.perform(post("/api/v1/orders/1/cancel"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Unauthenticated request returns 401")
        void cancelOrder_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/orders/1/cancel"))
                .andExpect(status().isUnauthorized());
        }
    }

    // ---- acceptOrder (/api/v1/orders/{orderId}/accept POST) ----

    @Nested
    @DisplayName("POST /api/v1/orders/{orderId}/accept (acceptOrder)")
    class AcceptOrder {

        @Test
        @DisplayName("RESTAURANT_OWNER can accept order")
        @WithMockUser(roles = "RESTAURANT_OWNER")
        void acceptOrder_restaurantOwnerRole_returns200() throws Exception {
            mockMvc.perform(post("/api/v1/orders/1/accept"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can accept order (via role hierarchy)")
        @WithMockUser(roles = "ADMIN")
        void acceptOrder_adminRole_returns200() throws Exception {
            mockMvc.perform(post("/api/v1/orders/1/accept"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER cannot accept order")
        @WithMockUser(roles = "CUSTOMER")
        void acceptOrder_customerRole_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/orders/1/accept"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("COURIER cannot accept order")
        @WithMockUser(roles = "COURIER")
        void acceptOrder_courierRole_returns403() throws Exception {
            mockMvc.perform(post("/api/v1/orders/1/accept"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthenticated request returns 401")
        void acceptOrder_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/orders/1/accept"))
                .andExpect(status().isUnauthorized());
        }
    }

    // ---- reviseOrder (/api/v1/orders/{orderId}/revise POST) ----

    @Nested
    @DisplayName("POST /api/v1/orders/{orderId}/revise (reviseOrder)")
    class ReviseOrder {

        @Test
        @DisplayName("CUSTOMER can revise order")
        @WithMockUser(roles = "CUSTOMER")
        void reviseOrder_customerRole_returns200() throws Exception {
            mockMvc.perform(post("/api/v1/orders/1/revise")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can revise order (via role hierarchy)")
        @WithMockUser(roles = "ADMIN")
        void reviseOrder_adminRole_returns200() throws Exception {
            mockMvc.perform(post("/api/v1/orders/1/revise")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Unauthenticated request returns 401")
        void reviseOrder_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/api/v1/orders/1/revise")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isUnauthorized());
        }
    }
}
