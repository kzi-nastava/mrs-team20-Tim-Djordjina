package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class OsrmRoutingService {

    private static final String BASE_URL =
            "https://router.project-osmr.org/route/v1/driving/";
    private static final double FALLBACK_SPEED_KMH = 40.0;

    private static final Logger log = LoggerFactory.getLogger(OsrmRoutingService.class);

    private final RestTemplate restTemplate = buildRestTemplate();
    private final DistanceCalculator distanceCalculator;

    private static RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(3000);
        f.setReadTimeout(4000);
        return new RestTemplate(f);
    }

    /** Road distance (km) over an ordered path; falls back to Haversine total. */
    public double roadDistanceKm(List<double[]> orderedPoints) {
        RouteResult r = tryRoute(orderedPoints);
        return r != null ? r.distanceKm() : distanceCalculator.totalDistanceKm(orderedPoints);
    }

    /** Two point convenience */
    public double roadDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        return roadDistanceKm(List.of(new double[]{lat1, lon1}, new double[]{lat2, lon2}));
    }

    /** Estimated travel time (minutes); OSRM duration, else distance / fixed speed */
    public double estimateMinutes(List<double[]> orderedPoints) {
        RouteResult r = tryRoute(orderedPoints);
        if (r != null) return r.durationMinutes();
        double km = distanceCalculator.totalDistanceKm(orderedPoints);
        return (km / FALLBACK_SPEED_KMH) * 60.0;
    }

    public double estimateMinutes(double lat1, double lon1, double lat2, double lon2) {
        return estimateMinutes(List.of(new double[]{lat1, lon1}, new double[]{lat2, lon2}));
    }

    // ---------- Internal ----------

    public record RouteResult(double distanceKm, double durationMinutes) {}

    /** Calls OSRM; returns null on any failure so callers use the fallback. */
    private RouteResult tryRoute(List<double[]> orderedPoints) {
        if (orderedPoints == null || orderedPoints.size() < 2) return null;
        try {
            String url = BASE_URL + coordinates(orderedPoints) + "?overview=false";
            OsrmResponse resp = restTemplate.getForObject(url, OsrmResponse.class);
            if (resp != null && "Ok".equals(resp.code)
                    && resp.routes != null && !resp.routes.isEmpty()) {
                OsrmResponse.Route route = resp.routes.get(0);
                double km = route.distance / 1000.0;        // meters -> km
                double minutes = route.duration / 60.0;     // seconds -> minutes
                return new RouteResult(km, minutes);
            }
            log.warn("OSRM returned no usable route (code={})", resp != null ? resp.code : "null");
        } catch (Exception e) {
            log.warn("OSRM routing failed, falling back to straight-line distance: {}", e.getMessage());
        }
        return null;
    }

    /** OSRM expects "lon,lat;lon,lat;..." (longitude first)*/
    private String coordinates(List<double[]> orderedPoints) {
        StringJoiner sj = new StringJoiner(";");
        for (double[] p : orderedPoints){
            // p = {latitude, longitude} -> lon,lat
            sj.add(String.format(Locale.US, "%.6f,%.6f", p[1], p[0]));
        }
        return sj.toString();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class OsrmResponse {
        public String code;
        public List<Route> routes;

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Route {
            public double distance; // meters
            public double duration; // seconds
        }
    }

}
