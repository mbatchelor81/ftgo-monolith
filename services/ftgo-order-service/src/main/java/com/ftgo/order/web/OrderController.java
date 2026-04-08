package com.ftgo.order.web;

import com.ftgo.order.api.CreateOrderRequest;
import com.ftgo.order.api.ReviseOrderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Order lifecycle management.
 *
 * <p>Provides endpoints for creating, retrieving, revising, and
 * progressing orders through their state machine.</p>
 */
@Tag(name = "Orders", description = "Order lifecycle management")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Operation(summary = "Create a new order")
    @ApiResponse(responseCode = "201", description = "Order created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @PostMapping
    public ResponseEntity<Void> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // TODO: delegate to OrderService once domain logic is migrated
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Get an order by ID")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @GetMapping("/{orderId}")
    public ResponseEntity<Void> getOrder(
            @Parameter(description = "Unique order identifier")
            @PathVariable long orderId) {
        // TODO: delegate to OrderService once domain logic is migrated
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cancel an order")
    @ApiResponse(responseCode = "200", description = "Order cancelled")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "409", description = "Order cannot be cancelled in its current state")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "Unique order identifier")
            @PathVariable long orderId) {
        // TODO: delegate to OrderService once domain logic is migrated
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Accept an order (restaurant acknowledges)")
    @ApiResponse(responseCode = "200", description = "Order accepted")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "409", description = "Order cannot be accepted in its current state")
    @PostMapping("/{orderId}/accept")
    public ResponseEntity<Void> acceptOrder(
            @Parameter(description = "Unique order identifier")
            @PathVariable long orderId) {
        // TODO: delegate to OrderService once domain logic is migrated
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Revise order line items")
    @ApiResponse(responseCode = "200", description = "Order revised")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "409", description = "Order cannot be revised in its current state")
    @PostMapping("/{orderId}/revise")
    public ResponseEntity<Void> reviseOrder(
            @Parameter(description = "Unique order identifier")
            @PathVariable long orderId,
            @Valid @RequestBody ReviseOrderRequest request) {
        // TODO: delegate to OrderService once domain logic is migrated
        return ResponseEntity.ok().build();
    }
}
