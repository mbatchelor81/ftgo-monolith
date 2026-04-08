package com.ftgo.courier.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Courier management.
 *
 * <p>Provides endpoints for registering couriers and managing availability.</p>
 */
@Tag(name = "Couriers", description = "Courier availability and delivery scheduling")
@RestController
@RequestMapping("/api/v1/couriers")
public class CourierController {

    @Operation(summary = "Register a new courier")
    @ApiResponse(responseCode = "201", description = "Courier created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @PostMapping
    public ResponseEntity<Void> createCourier(@RequestBody Object request) {
        // TODO: delegate to CourierService once domain logic is migrated
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Get a courier by ID")
    @ApiResponse(responseCode = "200", description = "Courier found")
    @ApiResponse(responseCode = "404", description = "Courier not found")
    @GetMapping("/{courierId}")
    public ResponseEntity<Void> getCourier(
            @Parameter(description = "Unique courier identifier")
            @PathVariable long courierId) {
        // TODO: delegate to CourierService once domain logic is migrated
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update courier availability")
    @ApiResponse(responseCode = "200", description = "Availability updated")
    @ApiResponse(responseCode = "404", description = "Courier not found")
    @PostMapping("/{courierId}/availability")
    public ResponseEntity<Void> updateAvailability(
            @Parameter(description = "Unique courier identifier")
            @PathVariable long courierId,
            @RequestBody Object request) {
        // TODO: delegate to CourierService once domain logic is migrated
        return ResponseEntity.ok().build();
    }
}
