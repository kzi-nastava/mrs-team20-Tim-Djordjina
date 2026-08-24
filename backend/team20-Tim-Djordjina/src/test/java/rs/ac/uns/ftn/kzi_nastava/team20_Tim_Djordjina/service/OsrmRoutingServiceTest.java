package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("OSRM Routing - Tests")
public class OsrmRoutingServiceTest {

    private static final String BASE = "https://osrm.test/route/v1/driving/";

    private final DistanceCalculator distanceCalculator = new DistanceCalculator();

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private OsrmRoutingService service;

    private final List<double[]> points = List.of(
            new double[]{45.2671, 19.8335},
            new double[]{45.2400, 19.8500}
    );

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        service = new OsrmRoutingService(distanceCalculator, restTemplate, BASE);
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("Parses OSRM road distance (meters -> km)")
    void roadDistance_parsesOsrmResponse() {
        String body = "{\"code\":\"Ok\",\"routes\":[{\"distance\":5000.0,\"duration\":600.0}]}";
        server.expect(requestTo(containsString("/route/v1/driving/")))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        double km = service.roadDistanceKm(points);

        assertEquals(5.0, km, 0.001);       // 5000 m = 5 km
        server.verify();
    }

    @Test
    @DisplayName("Parses OSRM duration (seconds -> minutes)")
    void estimateMinutes_parsesOsrmDuration() {
        String body = "{\"code\":\"Ok\",\"routes\":[{\"distance\":5000.0,\"duration\":600.0}]}";
        server.expect(requestTo(containsString("/route/v1/driving/")))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        double minutes = service.estimateMinutes(points);

        assertEquals(10.0, minutes, 0.001);      // 600 s = 10 min
        server.verify();
    }

    @Test
    @DisplayName("Falls back to straight-line distance when OSRM errors")
    void roadDistance_fallsBackOnError() {
        server.expect(requestTo(containsString("/route/v1/driving/")))
                .andRespond(withServerError());

        double km = service.roadDistanceKm(points);
        double expected = distanceCalculator.totalDistanceKm(points);

        assertEquals(expected, km, 0.001);      // falls back to Haversine
    }

    @Test
    @DisplayName("Falls back when OSRM reports a non-OK code")
    void roadDistance_fallsBackOnNotOk() {
        String body = "{\"code\":\"NoRoute\",\"routes\":[]}";
        server.expect(requestTo(containsString("/route/v1/driving/")))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        double km = service.roadDistanceKm(points);
        double expected = distanceCalculator.totalDistanceKm(points);

        assertEquals(expected, km, 0.001);
    }

    @Test
    @DisplayName("Road distance is longer than straight-line (sanity)")
    void roadDistance_longerThanStraightLine() {
        double straight = distanceCalculator.totalDistanceKm(points);
        String body = "{\"code\":\"Ok\",\"routes\":[{\"distance\":"
                + (straight * 1000.0 * 1.3) + ",\"duration\":600.0}]}";
        server.expect(requestTo(containsString("/route/v1/driving/")))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        double road = service.roadDistanceKm(points);

        assertTrue(road > straight, "Road distance should exceed straight-line");
    }

}
