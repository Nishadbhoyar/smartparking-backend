package com.smartparking.utils;

/**
 * Utility class for GPS / geographic calculations.
 *
 * Haversine Formula:
 * Calculates the real-world distance (in km) between two GPS coordinates,
 * accounting for the curvature of the Earth.
 *
 * This is the same formula used by Google Maps, Uber, etc.
 */
public class GeoUtils {

    // Earth's radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculate distance in kilometers between two GPS points.
     *
     * Example:
     *   Pune Railway Station: 18.5285, 73.8740
     *   Pune Airport:         18.5793, 73.9089
     *   → Distance: ~7.2 km
     *
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    public static double calculateDistanceKm(double lat1, double lon1,
                                             double lat2, double lon2) {
        // Convert degrees to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculate estimated travel time in minutes.
     * Assumes average city speed of 25 km/h (accounts for traffic, signals).
     *
     * @param distanceKm Distance in kilometers
     * @return Estimated minutes
     */
    public static int estimatedMinutes(double distanceKm) {
        double avgSpeedKmPerHour = 25.0;
        return (int) Math.ceil((distanceKm / avgSpeedKmPerHour) * 60);
    }

    /**
     * Check if a location is within a given radius.
     *
     * @param centerLat  Center latitude
     * @param centerLon  Center longitude
     * @param pointLat   Point latitude to check
     * @param pointLon   Point longitude to check
     * @param radiusKm   Radius in kilometers
     * @return true if point is within radius
     */
    public static boolean isWithinRadius(double centerLat, double centerLon,
                                         double pointLat, double pointLon,
                                         double radiusKm) {
        return calculateDistanceKm(centerLat, centerLon, pointLat, pointLon) <= radiusKm;
    }
}