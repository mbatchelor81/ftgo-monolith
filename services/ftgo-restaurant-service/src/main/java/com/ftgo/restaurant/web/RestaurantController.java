package com.ftgo.restaurant.web;

import com.ftgo.restaurant.api.CreateRestaurantRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Restaurant management.
 *
 * <p>Provides endpoints for registering restaurants and managing menus.</p>
 *
 * <h3>Authorization Rules</h3>
 * <ul>
 *   <li>Create restaurant — RESTAURANT_OWNER, ADMIN</li>
 *   <li>Get restaurant — CUSTOMER (view), RESTAURANT_OWNER, ADMIN</li>
 * </ul>
 */
@Tag(name = "Restaurants", description = "Restaurant registration and menu management")
@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

    @Operation(summary = "Register a new restaurant")
    @ApiResponse(responseCode = "201", description = "Restaurant created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @PostMapping
    public ResponseEntity<Void> createRestaurant(@Valid @RequestBody CreateRestaurantRequest request) {
        // TODO: delegate to RestaurantService once domain logic is migrated
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Get a restaurant by ID")
    @ApiResponse(responseCode = "200", description = "Restaurant found")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{restaurantId}")
    public ResponseEntity<Void> getRestaurant(
            @Parameter(description = "Unique restaurant identifier")
            @PathVariable long restaurantId) {
        // TODO: delegate to RestaurantService once domain logic is migrated
        return ResponseEntity.ok().build();
    }
}
