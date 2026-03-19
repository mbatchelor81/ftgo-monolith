package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Transactional
public class OrderService {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private OrderRepository orderRepository;

  private RestaurantServiceClient restaurantServiceClient;

  private Optional<MeterRegistry> meterRegistry;

  private ConsumerServiceClient consumerServiceClient;
  private DeliveryServiceClient deliveryServiceClient;

  public OrderService(OrderRepository orderRepository,
                      RestaurantServiceClient restaurantServiceClient,
                      Optional<MeterRegistry> meterRegistry,
                      ConsumerServiceClient consumerServiceClient,
                      DeliveryServiceClient deliveryServiceClient) {

    this.orderRepository = orderRepository;
    this.restaurantServiceClient = restaurantServiceClient;
    this.meterRegistry = meterRegistry;
    this.consumerServiceClient = consumerServiceClient;
    this.deliveryServiceClient = deliveryServiceClient;
  }

  @Transactional
  public Order createOrder(long consumerId, long restaurantId,
                           List<MenuItemIdAndQuantity> lineItems) {
    RestaurantDTO restaurant = restaurantServiceClient.findById(restaurantId);
    if (restaurant == null) {
      throw new RestaurantNotFoundException(restaurantId);
    }

    List<OrderLineItem> orderLineItems = makeOrderLineItems(lineItems, restaurant);

    Order order = new Order(consumerId, restaurantId, restaurant.getName(), orderLineItems);

    consumerServiceClient.validateOrderForConsumer(consumerId, order.getOrderTotal());

    orderRepository.save(order);

    meterRegistry.ifPresent(mr1 -> mr1.counter("approved_orders").increment());

    meterRegistry.ifPresent(mr -> mr.counter("placed_orders").increment());

    return order;
  }

  private List<OrderLineItem> makeOrderLineItems(List<MenuItemIdAndQuantity> lineItems, RestaurantDTO restaurant) {
    return lineItems.stream().map(li -> {
      MenuItemDTO om = restaurant.getMenuItems().stream()
              .filter(mi -> mi.getId().equals(li.getMenuItemId()))
              .findFirst()
              .orElseThrow(() -> new InvalidMenuItemIdException(li.getMenuItemId()));
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

    DeliveryDTO delivery = deliveryServiceClient.scheduleDelivery(order.getId(), readyBy);
    order.scheduleWithCourier(delivery.getCourierId());
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
