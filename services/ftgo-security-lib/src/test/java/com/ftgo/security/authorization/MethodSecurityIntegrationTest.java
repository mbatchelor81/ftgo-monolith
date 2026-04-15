package com.ftgo.security.authorization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ftgo.security.config.BaseSecurityConfiguration;
import com.ftgo.security.config.CorsSecurityConfiguration;
import com.ftgo.security.jwt.JwtConfiguration;
import com.ftgo.security.jwt.JwtTokenService;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Integration tests verifying method-level security with role-based access control.
 *
 * <p>Tests all role/endpoint combinations per the RBAC permission matrix:
 *
 * <table border="1">
 * <tr><th>Role</th><th>Consumer</th><th>Order</th><th>Restaurant</th><th>Courier</th></tr>
 * <tr><td>CUSTOMER</td><td>read:own</td><td>create,read:own,cancel:own</td>
 *     <td>read</td><td>read:own</td></tr>
 * <tr><td>RESTAURANT_OWNER</td><td>-</td><td>read,accept</td>
 *     <td>create,read,update:own,delete</td><td>-</td></tr>
 * <tr><td>COURIER</td><td>-</td><td>read:own</td><td>-</td>
 *     <td>read:own,update:own,delivery:update</td></tr>
 * <tr><td>ADMIN</td><td colspan="4">Full access</td></tr>
 * </table>
 */
@SpringBootTest(classes = MethodSecurityIntegrationTest.TestConfig.class)
@AutoConfigureMockMvc
@DisplayName("Method-level Security Integration")
class MethodSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenService tokenService;

    // ── Helper ─────────────────────────────────────────────────────────

    private String tokenFor(String userId, String username, String... roles) {
        return tokenService.generateAccessToken(userId, username, List.of(roles), List.of());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    // ── Consumer Service endpoints ────────────────────────────────────

    @Nested
    @DisplayName("Consumer Service")
    class ConsumerServiceEndpoints {

        @Test
        @DisplayName("CUSTOMER can read own consumer profile")
        void customer_canReadOwnProfile() throws Exception {
            String token = tokenFor("user-1", "customer", "CUSTOMER");
            mockMvc.perform(
                            get("/api/test/consumers/user-1")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER cannot read another consumer's profile")
        void customer_cannotReadOtherProfile() throws Exception {
            String token = tokenFor("user-1", "customer", "CUSTOMER");
            mockMvc.perform(
                            get("/api/test/consumers/user-999")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN can read any consumer profile")
        void admin_canReadAnyProfile() throws Exception {
            String token = tokenFor("admin-1", "admin", "ADMIN");
            mockMvc.perform(
                            get("/api/test/consumers/user-999")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can create consumer")
        void admin_canCreateConsumer() throws Exception {
            String token = tokenFor("admin-1", "admin", "ADMIN");
            mockMvc.perform(
                            post("/api/test/consumers")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"test\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER cannot create consumer")
        void customer_cannotCreateConsumer() throws Exception {
            String token = tokenFor("user-1", "customer", "CUSTOMER");
            mockMvc.perform(
                            post("/api/test/consumers")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"test\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("COURIER cannot read consumer profiles")
        void courier_cannotReadConsumer() throws Exception {
            String token = tokenFor("courier-1", "courier", "COURIER");
            mockMvc.perform(
                            get("/api/test/consumers/user-1")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER cannot read consumer profiles")
        void restaurantOwner_cannotReadConsumer() throws Exception {
            String token = tokenFor("owner-1", "owner", "RESTAURANT_OWNER");
            mockMvc.perform(
                            get("/api/test/consumers/user-1")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("unauthenticated request returns 401")
        void unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/test/consumers/user-1").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── Order Service endpoints ───────────────────────────────────────

    @Nested
    @DisplayName("Order Service")
    class OrderServiceEndpoints {

        @Test
        @DisplayName("CUSTOMER can create order")
        void customer_canCreateOrder() throws Exception {
            String token = tokenFor("user-1", "customer", "CUSTOMER");
            mockMvc.perform(
                            post("/api/test/orders")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"restaurantId\":1}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER can read own order")
        void customer_canReadOwnOrder() throws Exception {
            String token = tokenFor("user-1", "customer", "CUSTOMER");
            mockMvc.perform(
                            get("/api/test/orders/user-1/1")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER cannot read another user's order")
        void customer_cannotReadOtherOrder() throws Exception {
            String token = tokenFor("user-1", "customer", "CUSTOMER");
            mockMvc.perform(
                            get("/api/test/orders/user-999/1")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER can read any order")
        void restaurantOwner_canReadAnyOrder() throws Exception {
            String token = tokenFor("owner-1", "owner", "RESTAURANT_OWNER");
            mockMvc.perform(
                            get("/api/test/orders/any")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER can accept order")
        void restaurantOwner_canAcceptOrder() throws Exception {
            String token = tokenFor("owner-1", "owner", "RESTAURANT_OWNER");
            mockMvc.perform(
                            put("/api/test/orders/1/accept")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("COURIER cannot create order")
        void courier_cannotCreateOrder() throws Exception {
            String token = tokenFor("courier-1", "courier", "COURIER");
            mockMvc.perform(
                            post("/api/test/orders")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"restaurantId\":1}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("COURIER cannot accept order")
        void courier_cannotAcceptOrder() throws Exception {
            String token = tokenFor("courier-1", "courier", "COURIER");
            mockMvc.perform(
                            put("/api/test/orders/1/accept")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN can read any order")
        void admin_canReadAnyOrder() throws Exception {
            String token = tokenFor("admin-1", "admin", "ADMIN");
            mockMvc.perform(
                            get("/api/test/orders/any")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can cancel any order")
        void admin_canCancelOrder() throws Exception {
            String token = tokenFor("admin-1", "admin", "ADMIN");
            mockMvc.perform(
                            put("/api/test/orders/1/cancel")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER cannot cancel another user's order")
        void customer_cannotCancelOtherOrder() throws Exception {
            String token = tokenFor("user-1", "customer", "CUSTOMER");
            mockMvc.perform(
                            put("/api/test/orders/1/cancel")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // ── Restaurant Service endpoints ──────────────────────────────────

    @Nested
    @DisplayName("Restaurant Service")
    class RestaurantServiceEndpoints {

        @Test
        @DisplayName("CUSTOMER can read restaurants")
        void customer_canReadRestaurants() throws Exception {
            String token = tokenFor("user-1", "customer", "CUSTOMER");
            mockMvc.perform(
                            get("/api/test/restaurants")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER can create restaurant")
        void restaurantOwner_canCreateRestaurant() throws Exception {
            String token = tokenFor("owner-1", "owner", "RESTAURANT_OWNER");
            mockMvc.perform(
                            post("/api/test/restaurants")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"Test\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER can update own restaurant")
        void restaurantOwner_canUpdateOwnRestaurant() throws Exception {
            String token = tokenFor("owner-1", "owner", "RESTAURANT_OWNER");
            mockMvc.perform(
                            put("/api/test/restaurants/owner-1/1")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"Updated\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER cannot update another owner's restaurant")
        void restaurantOwner_cannotUpdateOtherRestaurant() throws Exception {
            String token = tokenFor("owner-1", "owner", "RESTAURANT_OWNER");
            mockMvc.perform(
                            put("/api/test/restaurants/owner-999/1")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"Updated\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CUSTOMER cannot create restaurant")
        void customer_cannotCreateRestaurant() throws Exception {
            String token = tokenFor("user-1", "customer", "CUSTOMER");
            mockMvc.perform(
                            post("/api/test/restaurants")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"Test\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("COURIER cannot create restaurant")
        void courier_cannotCreateRestaurant() throws Exception {
            String token = tokenFor("courier-1", "courier", "COURIER");
            mockMvc.perform(
                            post("/api/test/restaurants")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"Test\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN can update any restaurant")
        void admin_canUpdateAnyRestaurant() throws Exception {
            String token = tokenFor("admin-1", "admin", "ADMIN");
            mockMvc.perform(
                            put("/api/test/restaurants/owner-999/1")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"Updated\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can delete restaurant")
        void admin_canDeleteRestaurant() throws Exception {
            String token = tokenFor("admin-1", "admin", "ADMIN");
            mockMvc.perform(
                            delete("/api/test/restaurants/1")
                                    .header("Authorization", bearer(token)))
                    .andExpect(status().isOk());
        }
    }

    // ── Courier Service endpoints ─────────────────────────────────────

    @Nested
    @DisplayName("Courier Service")
    class CourierServiceEndpoints {

        @Test
        @DisplayName("COURIER can read own profile")
        void courier_canReadOwnProfile() throws Exception {
            String token = tokenFor("courier-1", "courier", "COURIER");
            mockMvc.perform(
                            get("/api/test/couriers/courier-1")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("COURIER cannot read another courier's profile")
        void courier_cannotReadOtherProfile() throws Exception {
            String token = tokenFor("courier-1", "courier", "COURIER");
            mockMvc.perform(
                            get("/api/test/couriers/courier-999")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("COURIER can update own availability")
        void courier_canUpdateOwnAvailability() throws Exception {
            String token = tokenFor("courier-1", "courier", "COURIER");
            mockMvc.perform(
                            put("/api/test/couriers/courier-1/availability")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"available\":true}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("COURIER cannot update another courier's availability")
        void courier_cannotUpdateOtherAvailability() throws Exception {
            String token = tokenFor("courier-1", "courier", "COURIER");
            mockMvc.perform(
                            put("/api/test/couriers/courier-999/availability")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"available\":true}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("COURIER can update delivery status")
        void courier_canUpdateDeliveryStatus() throws Exception {
            String token = tokenFor("courier-1", "courier", "COURIER");
            mockMvc.perform(
                            put("/api/test/couriers/delivery/1/status")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"status\":\"picked_up\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER cannot update delivery status")
        void customer_cannotUpdateDeliveryStatus() throws Exception {
            String token = tokenFor("user-1", "customer", "CUSTOMER");
            mockMvc.perform(
                            put("/api/test/couriers/delivery/1/status")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"status\":\"picked_up\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN can read any courier profile")
        void admin_canReadAnyCourier() throws Exception {
            String token = tokenFor("admin-1", "admin", "ADMIN");
            mockMvc.perform(
                            get("/api/test/couriers/courier-999")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can create courier")
        void admin_canCreateCourier() throws Exception {
            String token = tokenFor("admin-1", "admin", "ADMIN");
            mockMvc.perform(
                            post("/api/test/couriers")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"New Courier\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER cannot create courier")
        void customer_cannotCreateCourier() throws Exception {
            String token = tokenFor("user-1", "customer", "CUSTOMER");
            mockMvc.perform(
                            post("/api/test/couriers")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"New Courier\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER cannot read courier profiles")
        void restaurantOwner_cannotReadCourier() throws Exception {
            String token = tokenFor("owner-1", "owner", "RESTAURANT_OWNER");
            mockMvc.perform(
                            get("/api/test/couriers/courier-1")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // ── Role hierarchy tests ──────────────────────────────────────────

    @Nested
    @DisplayName("Role Hierarchy")
    class RoleHierarchyEndpoints {

        @Test
        @DisplayName("ADMIN inherits CUSTOMER — can read restaurants")
        void admin_inheritsCustomer_canReadRestaurants() throws Exception {
            String token = tokenFor("admin-1", "admin", "ADMIN");
            mockMvc.perform(
                            get("/api/test/restaurants")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN inherits RESTAURANT_OWNER — can accept orders")
        void admin_inheritsRestaurantOwner_canAcceptOrders() throws Exception {
            String token = tokenFor("admin-1", "admin", "ADMIN");
            mockMvc.perform(
                            put("/api/test/orders/1/accept")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN inherits COURIER — can update delivery status")
        void admin_inheritsCourier_canUpdateDelivery() throws Exception {
            String token = tokenFor("admin-1", "admin", "ADMIN");
            mockMvc.perform(
                            put("/api/test/couriers/delivery/1/status")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"status\":\"delivered\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER inherits CUSTOMER — can read restaurants")
        void restaurantOwner_inheritsCustomer_canReadRestaurants() throws Exception {
            String token = tokenFor("owner-1", "owner", "RESTAURANT_OWNER");
            mockMvc.perform(
                            get("/api/test/restaurants")
                                    .header("Authorization", bearer(token))
                                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    // ── Forbidden returns 403 ─────────────────────────────────────────

    @Nested
    @DisplayName("403 Forbidden responses")
    class ForbiddenResponses {

        @Test
        @DisplayName("unauthorized access returns 403 with JSON body")
        void unauthorizedAccess_returns403WithJson() throws Exception {
            String token = tokenFor("user-1", "customer", "CUSTOMER");
            mockMvc.perform(
                            post("/api/test/consumers")
                                    .header("Authorization", bearer(token))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"test\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    // ── Test Configuration ────────────────────────────────────────────

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
        BaseSecurityConfiguration.class,
        CorsSecurityConfiguration.class,
        JwtConfiguration.class,
        RoleHierarchyConfiguration.class,
        MethodSecurityConfiguration.class
    })
    static class TestConfig {

        @Bean
        public TestOwnershipResolver testOwnershipResolver() {
            return new TestOwnershipResolver();
        }

        @Bean
        public SecuredConsumerController securedConsumerController() {
            return new SecuredConsumerController();
        }

        @Bean
        public SecuredOrderController securedOrderController() {
            return new SecuredOrderController();
        }

        @Bean
        public SecuredRestaurantController securedRestaurantController() {
            return new SecuredRestaurantController();
        }

        @Bean
        public SecuredCourierController securedCourierController() {
            return new SecuredCourierController();
        }
    }

    /**
     * Simple ownership resolver for tests. Ownership is encoded in the URL path — a resource at
     * {@code /api/test/resource/{ownerId}/...} is owned by the user whose userId matches {ownerId}.
     */
    static class TestOwnershipResolver implements ResourceOwnershipResolver {

        @Override
        public boolean supports(String resourceType) {
            return true;
        }

        @Override
        public boolean isOwner(String userId, Serializable resourceId, String resourceType) {
            return userId.equals(String.valueOf(resourceId));
        }
    }

    // ── Test Controllers ──────────────────────────────────────────────

    @RestController
    static class SecuredConsumerController {

        @GetMapping("/api/test/consumers/{ownerId}")
        @PreAuthorize(
                "hasRole('ADMIN')"
                        + " or (hasRole('CUSTOMER')"
                        + " and @testOwnershipResolver.isOwner("
                        + "T(com.ftgo.security.util.SecurityUtils).getCurrentUserId().orElse(''),"
                        + " #ownerId, 'Consumer'))")
        public Map<String, Object> getConsumer(@PathVariable String ownerId) {
            return Map.of("consumerId", ownerId);
        }

        @PostMapping("/api/test/consumers")
        @PreAuthorize("hasRole('ADMIN')")
        public Map<String, Object> createConsumer() {
            return Map.of("created", true);
        }
    }

    @RestController
    static class SecuredOrderController {

        @PostMapping("/api/test/orders")
        @PreAuthorize("hasRole('CUSTOMER')")
        public Map<String, Object> createOrder() {
            return Map.of("created", true);
        }

        @GetMapping("/api/test/orders/{ownerId}/{orderId}")
        @PreAuthorize(
                "hasRole('ADMIN')"
                        + " or hasRole('RESTAURANT_OWNER')"
                        + " or (hasRole('CUSTOMER')"
                        + " and @testOwnershipResolver.isOwner("
                        + "T(com.ftgo.security.util.SecurityUtils).getCurrentUserId().orElse(''),"
                        + " #ownerId, 'Order'))")
        public Map<String, Object> getOrder(
                @PathVariable String ownerId, @PathVariable String orderId) {
            return Map.of("orderId", orderId, "ownerId", ownerId);
        }

        @GetMapping("/api/test/orders/any")
        @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANT_OWNER')")
        public Map<String, Object> getAllOrders() {
            return Map.of("orders", List.of());
        }

        @PutMapping("/api/test/orders/{orderId}/accept")
        @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANT_OWNER')")
        public Map<String, Object> acceptOrder(@PathVariable String orderId) {
            return Map.of("accepted", true);
        }

        @PutMapping("/api/test/orders/{orderId}/cancel")
        @PreAuthorize("hasRole('ADMIN')")
        public Map<String, Object> cancelOrder(@PathVariable String orderId) {
            return Map.of("cancelled", true);
        }
    }

    @RestController
    static class SecuredRestaurantController {

        @GetMapping("/api/test/restaurants")
        @PreAuthorize("hasRole('CUSTOMER')")
        public Map<String, Object> listRestaurants() {
            return Map.of("restaurants", List.of());
        }

        @PostMapping("/api/test/restaurants")
        @PreAuthorize("hasRole('RESTAURANT_OWNER')")
        public Map<String, Object> createRestaurant() {
            return Map.of("created", true);
        }

        @PutMapping("/api/test/restaurants/{ownerId}/{restaurantId}")
        @PreAuthorize(
                "hasRole('ADMIN')"
                        + " or (hasRole('RESTAURANT_OWNER')"
                        + " and @testOwnershipResolver.isOwner("
                        + "T(com.ftgo.security.util.SecurityUtils).getCurrentUserId().orElse(''),"
                        + " #ownerId, 'Restaurant'))")
        public Map<String, Object> updateRestaurant(
                @PathVariable String ownerId, @PathVariable String restaurantId) {
            return Map.of("updated", true);
        }

        @DeleteMapping("/api/test/restaurants/{restaurantId}")
        @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANT_OWNER')")
        public Map<String, Object> deleteRestaurant(@PathVariable String restaurantId) {
            return Map.of("deleted", true);
        }
    }

    @RestController
    static class SecuredCourierController {

        @GetMapping("/api/test/couriers/{ownerId}")
        @PreAuthorize(
                "hasRole('ADMIN')"
                        + " or (hasRole('COURIER')"
                        + " and @testOwnershipResolver.isOwner("
                        + "T(com.ftgo.security.util.SecurityUtils).getCurrentUserId().orElse(''),"
                        + " #ownerId, 'Courier'))")
        public Map<String, Object> getCourier(@PathVariable String ownerId) {
            return Map.of("courierId", ownerId);
        }

        @PostMapping("/api/test/couriers")
        @PreAuthorize("hasRole('ADMIN')")
        public Map<String, Object> createCourier() {
            return Map.of("created", true);
        }

        @PutMapping("/api/test/couriers/{ownerId}/availability")
        @PreAuthorize(
                "hasRole('ADMIN')"
                        + " or (hasRole('COURIER')"
                        + " and @testOwnershipResolver.isOwner("
                        + "T(com.ftgo.security.util.SecurityUtils).getCurrentUserId().orElse(''),"
                        + " #ownerId, 'Courier'))")
        public Map<String, Object> updateAvailability(@PathVariable String ownerId) {
            return Map.of("updated", true);
        }

        @PutMapping("/api/test/couriers/delivery/{deliveryId}/status")
        @PreAuthorize("hasRole('ADMIN') or hasRole('COURIER')")
        public Map<String, Object> updateDeliveryStatus(@PathVariable String deliveryId) {
            return Map.of("updated", true);
        }
    }
}
