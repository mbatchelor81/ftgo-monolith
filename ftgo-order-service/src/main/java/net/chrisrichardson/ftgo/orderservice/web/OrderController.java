package net.chrisrichardson.ftgo.orderservice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderRevision;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "/orders")
@Tag(name = "Orders", description = "Place, revise, cancel, and track the lifecycle of customer orders.")
public class OrderController {

  private OrderService orderService;

  private OrderRepository orderRepository;


  public OrderController(OrderService orderService, OrderRepository orderRepository) {
    this.orderService = orderService;
    this.orderRepository = orderRepository;
  }

  @Operation(summary = "Create a new order",
      description = "Creates an APPROVED order for the given consumer, restaurant, and line items.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Order created"),
      @ApiResponse(responseCode = "400", description = "Request validation failed"),
      @ApiResponse(responseCode = "404", description = "Consumer or restaurant not found")
  })
  @RequestMapping(method = RequestMethod.POST)
  public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
    Order order = orderService.createOrder(request.getConsumerId(),
            request.getRestaurantId(),
            request.getLineItems().stream().map(x -> new MenuItemIdAndQuantity(x.getMenuItemId(), x.getQuantity())).collect(toList())
    );
    return new CreateOrderResponse(order.getId());
  }


  @Operation(summary = "Get an order by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Order found"),
      @ApiResponse(responseCode = "404", description = "No order with the given ID")
  })
  @RequestMapping(path = "/{orderId}", method = RequestMethod.GET)
  public ResponseEntity<GetOrderResponse> getOrder(
      @Parameter(description = "Order identifier", example = "42") @PathVariable long orderId) {
    Optional<Order> order = orderRepository.findById(orderId);
    return order.map(o -> new ResponseEntity<>(makeGetOrderResponse(o), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @Operation(summary = "List orders for a consumer",
      description = "Returns every order placed by the given consumer, newest first.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Zero or more orders")
  })
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<GetOrderResponse>> getOrders(
      @Parameter(description = "Consumer whose orders to list", required = true)
      @RequestParam long consumerId) {
    List<GetOrderResponse> orders = orderRepository.findAllByConsumerId(consumerId)
            .stream()
            .map(this::makeGetOrderResponse)
            .collect(Collectors.toList());

    return new ResponseEntity<>(orders, HttpStatus.OK);
  }

  private GetOrderResponse makeGetOrderResponse(Order order) {
    return new GetOrderResponse(order.getId(),
            order.getOrderState().name(),
            order.getOrderTotal(),
            order.getRestaurant().getName(),
            order.getAssignedCourier() == null ? null : order.getAssignedCourier().getId(),
            order.getAssignedCourier() == null ? null : order.getAssignedCourier().actionsForDelivery(order)
    );
  }

  @Operation(summary = "Cancel an order",
      description = "Moves an APPROVED order to the CANCELLED state. No-op if the order is already cancelled.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Order cancelled"),
      @ApiResponse(responseCode = "404", description = "Order not found"),
      @ApiResponse(responseCode = "409", description = "Order is in a state that cannot be cancelled")
  })
  @RequestMapping(path = "/{orderId}/cancel", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> cancel(@PathVariable long orderId) {
    try {
      Order order = orderService.cancel(orderId);
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @Operation(summary = "Revise an order",
      description = "Updates the quantities on an order's line items while it is still in APPROVED state.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Revision applied"),
      @ApiResponse(responseCode = "404", description = "Order not found")
  })
  @RequestMapping(path = "/{orderId}/revise", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> revise(@PathVariable long orderId, @RequestBody ReviseOrderRequest request) {
    try {
      Order order = orderService.reviseOrder(orderId, new OrderRevision(Optional.empty(), request.getRevisedLineItemQuantities()));
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @Operation(summary = "Restaurant accepts an order",
      description = "Restaurant-facing state transition: APPROVED → ACCEPTED.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Order accepted"),
      @ApiResponse(responseCode = "409", description = "Order is not in APPROVED state")
  })
  @RequestMapping(path="/{orderId}/accept", method= RequestMethod.POST)
  public ResponseEntity<String> accept(@PathVariable long orderId, @RequestBody OrderAcceptance orderAcceptance) {
    orderService.accept(orderId, orderAcceptance.getReadyBy());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Restaurant starts preparation",
      description = "Restaurant-facing state transition: ACCEPTED → PREPARING.")
  @RequestMapping(path="/{orderId}/preparing", method= RequestMethod.POST)
  public ResponseEntity<String> preparing(@PathVariable long orderId) {
    orderService.notePreparing(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Order is ready for pickup",
      description = "Restaurant-facing state transition: PREPARING → READY_FOR_PICKUP.")
  @RequestMapping(path="/{orderId}/ready", method= RequestMethod.POST)
  public ResponseEntity<String> ready(@PathVariable long orderId) {
    orderService.noteReadyForPickup(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Courier picks up the order",
      description = "Courier-facing state transition: READY_FOR_PICKUP → PICKED_UP.")
  @RequestMapping(path="/{orderId}/pickedup", method= RequestMethod.POST)
  public ResponseEntity<String> pickedup(@PathVariable long orderId) {
    orderService.notePickedUp(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Courier delivers the order",
      description = "Courier-facing state transition: PICKED_UP → DELIVERED.")
  @RequestMapping(path="/{orderId}/delivered", method= RequestMethod.POST)
  public ResponseEntity<String> delivered(@PathVariable long orderId) {
    orderService.noteDelivered(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
