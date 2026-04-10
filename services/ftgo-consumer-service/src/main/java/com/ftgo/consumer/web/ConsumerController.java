package com.ftgo.consumer.web;

import com.ftgo.consumer.api.CreateConsumerRequest;
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
 * REST controller for Consumer management.
 *
 * <p>Provides endpoints for registering and retrieving consumers.</p>
 *
 * <h3>Authorization Rules</h3>
 * <ul>
 *   <li>Create consumer — CUSTOMER, ADMIN</li>
 *   <li>Get consumer — CUSTOMER (own), ADMIN (any)</li>
 * </ul>
 */
@Tag(name = "Consumers", description = "Consumer registration and management")
@RestController
@RequestMapping("/api/v1/consumers")
public class ConsumerController {

    @Operation(summary = "Register a new consumer")
    @ApiResponse(responseCode = "201", description = "Consumer created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<Void> createConsumer(@Valid @RequestBody CreateConsumerRequest request) {
        // TODO: delegate to ConsumerService once domain logic is migrated
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Get a consumer by ID")
    @ApiResponse(responseCode = "200", description = "Consumer found")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Consumer not found")
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#consumerId, 'Consumer', 'read')")
    @GetMapping("/{consumerId}")
    public ResponseEntity<Void> getConsumer(
            @Parameter(description = "Unique consumer identifier")
            @PathVariable long consumerId) {
        // TODO: delegate to ConsumerService once domain logic is migrated
        return ResponseEntity.ok().build();
    }
}
