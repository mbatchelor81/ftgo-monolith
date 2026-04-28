package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.courierservice.domain.GeoUtils;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static java.util.stream.Collectors.toList;

@Transactional
public class OrderService {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private OrderRepository orderRepository;

  private RestaurantRepository restaurantRepository;

  private Optional<MeterRegistry> meterRegistry;

  private ConsumerService consumerService;
  private CourierRepository courierRepository;
  private CourierService courierService;
  private DeliveryTrackingService deliveryTrackingService;
  private Random random = new Random();

  public OrderService(OrderRepository orderRepository,
                      RestaurantRepository restaurantRepository,
                      Optional<MeterRegistry> meterRegistry,
                      ConsumerService consumerService,
                      CourierRepository courierRepository,
                      CourierService courierService,
                      DeliveryTrackingService deliveryTrackingService) {

    this.orderRepository = orderRepository;
    this.restaurantRepository = restaurantRepository;
    this.meterRegistry = meterRegistry;
    this.consumerService = consumerService;
    this.courierRepository = courierRepository;
    this.courierService = courierService;
    this.deliveryTrackingService = deliveryTrackingService;
  }

  @Transactional
  public Order createOrder(long consumerId, long restaurantId,
                           List<MenuItemIdAndQuantity> lineItems) {
    Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));


    List<OrderLineItem> orderLineItems = makeOrderLineItems(lineItems, restaurant);

    Order order = new Order(consumerId, restaurant, orderLineItems);

    consumerService.validateOrderForConsumer(consumerId, order.getOrderTotal());

    // TODO - charge a credit card too

    orderRepository.save(order);

    meterRegistry.ifPresent(mr1 -> mr1.counter("approved_orders").increment());

    meterRegistry.ifPresent(mr -> mr.counter("placed_orders").increment());

    return order;
  }

  private List<OrderLineItem> makeOrderLineItems(List<MenuItemIdAndQuantity> lineItems, Restaurant restaurant) {
    return lineItems.stream().map(li -> {
      MenuItem om = restaurant.findMenuItem(li.getMenuItemId()).orElseThrow(() -> new InvalidMenuItemIdException(li.getMenuItemId()));
      return new OrderLineItem(li.getMenuItemId(), om.getName(), om.getPrice(), li.getQuantity());
    }).collect(toList());
  }

  @Transactional
  public Order cancel(Long orderId) {
    Order order = tryToFindOrder(orderId);

    order.cancel();

    return order;
  }

  @Transactional
  public Order reviseOrder(long orderId, OrderRevision orderRevision) {
    Order order = tryToFindOrder(orderId);
    order.revise(orderRevision);
    return order;
  }

  public void accept(long orderId, LocalDateTime readyBy) {
    Order order = tryToFindOrder(orderId);
    order.acceptTicket(readyBy);
    scheduleDelivery(order, readyBy);
  }

  public void scheduleDelivery(Order order, LocalDateTime readyBy) {
    Restaurant restaurant = order.getRestaurant();
    Courier courier;
    double distanceKm = 0;

    if (restaurant.getLatitude() != null && restaurant.getLongitude() != null) {
      Optional<Courier> nearest = courierService.findNearestAvailableCourier(
          restaurant.getLatitude(), restaurant.getLongitude());

      if (nearest.isPresent()) {
        courier = nearest.get();
        distanceKm = GeoUtils.haversineDistance(
            restaurant.getLatitude(), restaurant.getLongitude(),
            courier.getLatitude(), courier.getLongitude());
        logger.info("Assigned nearest courier {} at {} km from restaurant {}",
            courier.getId(), distanceKm, restaurant.getId());
      } else {
        logger.warn("No couriers with location data available, falling back to random assignment");
        courier = pickRandomAvailableCourier();
      }
    } else {
      logger.warn("Restaurant {} has no coordinates, falling back to random assignment",
          restaurant.getId());
      courier = pickRandomAvailableCourier();
    }

    long travelMinutes = GeoUtils.estimatedTravelTimeMinutes(distanceKm);
    long deliveryMinutes = travelMinutes > 0 ? travelMinutes : 30;

    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, readyBy.plusMinutes(deliveryMinutes)));
    order.schedule(courier);

    LocalDateTime estimatedPickup = readyBy;
    LocalDateTime estimatedDelivery = readyBy.plusMinutes(deliveryMinutes);

    deliveryTrackingService.createTracking(order, courier, distanceKm,
        estimatedPickup, estimatedDelivery);
  }

  private Courier pickRandomAvailableCourier() {
    List<Courier> couriers = courierRepository.findAllAvailable();
    if (couriers.isEmpty()) {
      throw new NoCourierAvailableException();
    }
    return couriers.get(random.nextInt(couriers.size()));
  }


  private Order tryToFindOrder(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
  }

  @Transactional
  public void notePreparing(long orderId) {
    Order order = tryToFindOrder(orderId);
    order.notePreparing();
  }

  @Transactional
  public void noteReadyForPickup(long orderId) {
    Order order = tryToFindOrder(orderId);
    order.noteReadyForPickup();
  }

  @Transactional
  public void notePickedUp(long orderId) {
    Order order = tryToFindOrder(orderId);
    order.notePickedUp();
  }

  @Transactional
  public void noteDelivered(long orderId) {
    Order order = tryToFindOrder(orderId);
    order.noteDelivered();
  }
}
