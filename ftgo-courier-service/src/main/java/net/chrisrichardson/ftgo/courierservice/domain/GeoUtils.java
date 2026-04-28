package net.chrisrichardson.ftgo.courierservice.domain;

public final class GeoUtils {

  private static final double EARTH_RADIUS_KM = 6371.0;

  private GeoUtils() {
  }

  /**
   * Calculates the distance in kilometers between two geographic coordinates
   * using the Haversine formula.
   */
  public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(dLon / 2) * Math.sin(dLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS_KM * c;
  }

  /**
   * Estimates travel time in minutes assuming average speed of 30 km/h.
   */
  public static long estimatedTravelTimeMinutes(double distanceKm) {
    return Math.round((distanceKm / 30.0) * 60);
  }
}
