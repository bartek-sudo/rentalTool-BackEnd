package com.example.rentalTool_BackEnd.shared.util;

/**
 * Utility class for geolocation calculations
 */
public class GeoLocationUtil {

    private static final int EARTH_RADIUS_KM = 6371;

    /**
     * Calculate distance between two points using Haversine formula
     *
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Round distance to 2 decimal places
     *
     * @param distance Distance in kilometers
     * @return Rounded distance
     */
    public static double roundDistance(double distance) {
        return Math.round(distance * 100.0) / 100.0;
    }
}
