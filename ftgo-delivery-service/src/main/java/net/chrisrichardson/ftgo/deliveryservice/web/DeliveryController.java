package net.chrisrichardson.ftgo.deliveryservice.web;

import net.chrisrichardson.ftgo.deliveryservice.domain.Delivery;
import net.chrisrichardson.ftgo.deliveryservice.domain.DeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/deliveries")
public class DeliveryController {

  private DeliveryService deliveryService;

  public DeliveryController(DeliveryService deliveryService) {
    this.deliveryService = deliveryService;
  }

  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<CreateDeliveryResponse> create(@RequestBody CreateDeliveryRequest request) {
    Delivery delivery = deliveryService.scheduleDelivery(request.getOrderId(), request.getReadyBy());
    return new ResponseEntity<>(new CreateDeliveryResponse(delivery.getId(), delivery.getCourierId()), HttpStatus.CREATED);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<GetDeliveryResponse>> getByOrderId(@RequestParam long orderId) {
    List<Delivery> deliveries = deliveryService.findByOrderId(orderId);
    List<GetDeliveryResponse> responses = deliveries.stream()
            .map(d -> new GetDeliveryResponse(d.getId(), d.getOrderId(), d.getCourierId(),
                    d.getPickupTime(), d.getDropoffTime(), d.getStatus(), d.getActions()))
            .collect(Collectors.toList());
    return new ResponseEntity<>(responses, HttpStatus.OK);
  }
}
