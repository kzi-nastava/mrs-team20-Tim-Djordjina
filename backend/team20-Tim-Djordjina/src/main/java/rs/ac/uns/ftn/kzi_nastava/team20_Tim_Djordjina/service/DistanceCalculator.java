package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import org.springframework.stereotype.Component;

import java.util.List;

/** Computes distance between coordinates.
 *
 * MVP: straight-line (Haversine) distance.*/
@Component
public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /** Great circle distance between two points, in kilometres */
    public double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /** Total distance over an ordered list of points, e.g.
     * pickup -> stop1 -> stop2 -> ... -> destination.
     * Each element is {latitude, longitude} */
    public double totalDistanceKm(List<double[]> orderedPoints) {
        double total = 0.0;
        for (int i = 0; i < orderedPoints.size() - 1; i++){
            double[] a = orderedPoints.get(i);
            double[] b = orderedPoints.get(i + 1);
            total += distanceKm(a[0], a[1], b[0], b[1]);
        }
        return total;
    }
}
