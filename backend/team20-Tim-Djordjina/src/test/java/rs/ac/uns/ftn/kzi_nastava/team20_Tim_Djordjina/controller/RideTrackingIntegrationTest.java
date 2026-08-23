package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Ride Tracking + Inconsistency - Integration Tests")
public class RideTrackingIntegrationTest {

    private static final String RIDER_EMAIL = "rider@test.com";
    private static final String DRIVER_EMAIL = "driver@test.com";
    private static final String OTHER_EMAIL = "other@test.com";
    private static final double VEHICLE_LAT = 45.2660;
    private static final double VEHICLE_LNG = 19.8340;

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private InconsistencyReportRepository inconsistencyReportRepository;
    @Autowired
    private MockMvc mockMvc;

    private User rider;
    private Driver driver;
    private String riderToken, otherToken;



    @BeforeEach
    void setUp() {
        cleanup();
        rider = makeUser("Rick", "Rider", RIDER_EMAIL, Role.USER);
        makeUser("Otto", "Other", OTHER_EMAIL, Role.USER);
        User driverUser = makeUser("Dana", "Driver", DRIVER_EMAIL, Role.DRIVER);
        driver = makeDriver(driverUser);

        riderToken = jwtTokenProvider.generateToken(RIDER_EMAIL, Role.USER.toString());
        otherToken = jwtTokenProvider.generateToken(OTHER_EMAIL, Role.USER.toString());
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        inconsistencyReportRepository.deleteAll();
        rideRepository.deleteAll();
        vehicleRepository.deleteAll();
        driverRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ---------- Tracking ----------

    @Test
    @DisplayName("Rider gets tracking: vehicle location, ETA, status")
    void tracking_returnsLocationAndEta() throws Exception {
        Ride ride = seedRide(RideStatus.IN_PROGRESS);

        mockMvc.perform(get("/api/rides/" + ride.getId() + "/tracking")
                        .header("Authorization", "Bearer " + riderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleLatitude").isNumber())
                .andExpect(jsonPath("$.vehicleLongitude").isNumber())
                .andExpect(jsonPath("$.etaMinutes").isNumber())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("A non-rider cannot track the ride (403)")
    void tracking_nonRider_returns403() throws Exception {
        Ride ride = seedRide(RideStatus.IN_PROGRESS);

        mockMvc.perform(get("/api/rides/" + ride.getId() + "/tracking")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    // ---------- Inconsistency ----------

    @Test
    @DisplayName("Rider reports an inconsistency: stored (201)")
    void report_stored() throws Exception {
        Ride ride = seedRide(RideStatus.IN_PROGRESS);

        mockMvc.perform(post("/api/rides/" + ride.getId() + "/inconsistency")
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Driver took a strange detour\"}"))
                .andExpect(status().isCreated());

        var reports = inconsistencyReportRepository.findByRideId(ride.getId());
        assertEquals(1, reports.size());
        assertEquals("Driver took a strange detour", reports.get(0).getText());
    }

    @Test
    @DisplayName("Empty report text is rejected (400)")
    void report_emptyText_returns400() throws Exception {
        Ride ride = seedRide(RideStatus.IN_PROGRESS);

        mockMvc.perform(post("/api/rides/" + ride.getId() + "/inconsistency")
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("A non-rider cannot report (403)")
    void report_nonRider_returns403() throws Exception {
        Ride ride = seedRide(RideStatus.IN_PROGRESS);

        mockMvc.perform(post("/api/rides/" + ride.getId() + "/inconsistency")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"not my ride\"}"))
                .andExpect(status().isForbidden());
    }

    // ---------- Helpers ----------

    private Ride seedRide(RideStatus status) {
        Ride r = new Ride();
        r.setRider(rider);
        r.setDriver(driver);
        r.setPickupAddress("Trg slobode");
        r.setPickupLatitude(45.2671);
        r.setPickupLongitude(19.8335);
        r.setDestinationAddress("Strand");
        r.setDestinationLatitude(45.3000);
        r.setDestinationLongitude(19.9000);
        r.setVehicleType(VehicleType.STANDARD);
        r.setDistanceKM(6.0);
        r.setFare(920.0);
        r.setStatus(status);
        if (status == RideStatus.IN_PROGRESS) {
            r.setStartedAt(LocalDateTime.now().minusMinutes(5));
        }
        return rideRepository.save(r);
    }

    private Driver makeDriver(User user) {
        Driver d = new Driver();
        d.setUser(user);
        d.setLoggedIn(true);
        d.setActive(true);
        d.setAvailable(false);
        d.setHasActiveRide(true);
        d.setWorkingMinutesLast24Hours(60);
        d = driverRepository.save(d);

        Vehicle v = new Vehicle();
        v.setDriver(d);
        v.setModel("Skoda Octavia");
        v.setVehicleType(VehicleType.STANDARD);
        v.setLicensePlate("NS-TRACK-1");
        v.setSeats(4);
        v.setCurrentLatitude(VEHICLE_LAT);
        v.setCurrentLongitude(VEHICLE_LNG);
        vehicleRepository.save(v);

        d.setVehicle(v);
        driverRepository.save(d);
        return d;
    }

    private User makeUser(String first, String last, String email, Role role) {
        User u = new User(first, last, email, passwordEncoder.encode("password123"),
                "+381600000000", "Addr", role);
        u.setActivated(true);
        return userRepository.save(u);
    }
}
