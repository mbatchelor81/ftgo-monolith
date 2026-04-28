package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierResponse;
import net.chrisrichardson.ftgo.courierservice.api.UpdateLocationRequest;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.DeliveryTracking;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CourierController {

  private CourierService courierService;

  public CourierController(CourierService courierService) {
    this.courierService = courierService;
  }

  @RequestMapping(path="/couriers", method= RequestMethod.POST)
  public ResponseEntity<CreateCourierResponse> create(@RequestBody CreateCourierRequest request) {
    Courier courier = courierService.createCourier(request.getName(), request.getAddress());
    return new ResponseEntity<>(new CreateCourierResponse(courier.getId()), HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/availability", method= RequestMethod.POST)
  public ResponseEntity<String> updateCourierAvailability(@PathVariable long courierId, @RequestBody CourierAvailability availability) {
    courierService.updateAvailability(courierId, availability.isAvailable());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/couriers/{courierId}/location", method= RequestMethod.POST)
  public ResponseEntity<String> updateLocation(@PathVariable long courierId, @RequestBody UpdateLocationRequest request) {
    try {
      courierService.updateLocation(courierId, request.getLatitude(), request.getLongitude());
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (CourierNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path="/couriers/{courierId}/active-deliveries", method= RequestMethod.GET)
  public ResponseEntity<List<ActiveDeliveryResponse>> getActiveDeliveries(@PathVariable long courierId) {
    try {
      List<DeliveryTracking> deliveries = courierService.getActiveDeliveries(courierId);
      List<ActiveDeliveryResponse> response = deliveries.stream()
          .map(dt -> new ActiveDeliveryResponse(
              dt.getId(),
              dt.getOrder().getId(),
              dt.getStatus().name(),
              dt.getDistanceKm(),
              dt.getEstimatedPickupTime(),
              dt.getEstimatedDeliveryTime()))
          .collect(Collectors.toList());
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (CourierNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path="/couriers/{courierId}", method= RequestMethod.GET)
  public ResponseEntity<Courier> get(@PathVariable long courierId) {
    Courier courier = courierService.findCourierById(courierId);
    return new ResponseEntity<>(courier, HttpStatus.OK);
  }

}
