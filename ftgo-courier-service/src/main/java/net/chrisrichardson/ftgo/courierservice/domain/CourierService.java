package net.chrisrichardson.ftgo.courierservice.domain;


import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CourierService {

  private CourierRepository courierRepository;
  private DeliveryTrackingRepository deliveryTrackingRepository;

  public CourierService(CourierRepository courierRepository, DeliveryTrackingRepository deliveryTrackingRepository) {
    this.courierRepository = courierRepository;
    this.deliveryTrackingRepository = deliveryTrackingRepository;
  }

  @Transactional
  public void updateAvailability(long courierId, boolean available) {
    if (available)
      noteAvailable(courierId);
    else
      noteUnavailable(courierId);
  }

  @Transactional
  public Courier createCourier(PersonName name, Address address) {
    Courier courier = new Courier(name, address);
    courierRepository.save(courier);
    return courier;
  }

  @Transactional
  public void updateLocation(long courierId, double latitude, double longitude) {
    Courier courier = courierRepository.findById(courierId)
        .orElseThrow(() -> new CourierNotFoundException(courierId));
    courier.updateLocation(latitude, longitude);
  }

  public Optional<Courier> findNearestAvailableCourier(double restaurantLat, double restaurantLng) {
    List<Courier> couriers = courierRepository.findAllAvailableWithLocation();
    return couriers.stream()
        .min(Comparator.comparingDouble(c ->
            GeoUtils.haversineDistance(restaurantLat, restaurantLng, c.getLatitude(), c.getLongitude())));
  }

  public List<DeliveryTracking> getActiveDeliveries(long courierId) {
    return deliveryTrackingRepository.findByCourierIdAndStatus(courierId, DeliveryStatus.ASSIGNED);
  }

  void noteAvailable(long courierId) {
    courierRepository.findById(courierId).get().noteAvailable();
  }

  void noteUnavailable(long courierId) {
    courierRepository.findById(courierId).get().noteUnavailable();
  }

  public Courier findCourierById(long courierId) {
    return courierRepository.findById(courierId).get();
  }

}
