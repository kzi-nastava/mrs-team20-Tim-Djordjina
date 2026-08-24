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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Stop Ride In Progress - Integration Tests")
public class StopRideIntegrationTest {

    private static final String RIDER_EMAIL = "rider@test.com";
    private static final String DRIVER_EMAIL = "driver@test.com";
    private static final String OTHER_DRIVER_EMAIL = "other-driver@test.com";
    private static final double ORIGINAL_FARE = 1400.0;
    private static final double STOP_LAT = 45.2680;   // -> close to pickup
    private static final double STOP_LNG = 19.8340;

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
    private MockMvc mockMvc;

    private User rider;
    private Driver driver;
    private Driver otherDriver;
    private String driverToken, otherDriverToken;


    @BeforeEach
    void setUp() {
        cleanup();
        rider = makeUser("Rick", "Rider", RIDER_EMAIL, Role.USER);
        User driverUser = makeUser("Dana", "Driver", DRIVER_EMAIL, Role.DRIVER);
        User otherUser = makeUser("Otto", "Other", OTHER_DRIVER_EMAIL, Role.DRIVER);
        driver = makeDriver(driverUser, "NS-STOP-1");
        otherDriver = makeDriver(otherUser, "NS-STOP-2");

        driverToken = jwtTokenProvider.generateToken(DRIVER_EMAIL, Role.DRIVER.toString());
        otherDriverToken = jwtTokenProvider.generateToken(OTHER_DRIVER_EMAIL, Role.DRIVER.toString());
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        notificationRepository.deleteAll();
        rideRepository.deleteAll();
        vehicleRepository.deleteAll();
        driverRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String stopBody() {
        return "{\"latitude\":" + STOP_LAT + ",\"longitude\":" + STOP_LNG + "}";
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("Driver stops an in-progress ride: FINISHED, new destination, lower fare")
    void driverStops_finishesWithNewDestinationAndFare() throws Exception {
        Ride ride = seedRide(RideStatus.IN_PROGRESS, driver);

        mockMvc.perform(post("/api/rides/" + ride.getId() + "/stop")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stopBody()))
                .andExpect(status().isOk());

        Ride updated = rideRepository.findById(ride.getId()).orElseThrow();
        assertEquals(RideStatus.FINISHED, updated.getStatus());
        assertEquals(STOP_LAT, updated.getDestinationLatitude(), 0.0001);
        assertEquals(STOP_LNG, updated.getDestinationLongitude(), 0.0001);
        assertTrue(updated.getFare() < ORIGINAL_FARE,
                "Fare should drop for the shortened trip (was " + updated.getFare() + ")");
    }

    @Test
    @DisplayName("Stopping frees the driver")
    void stop_freesDriver() throws Exception {
        Ride ride = seedRide(RideStatus.IN_PROGRESS, driver);

        mockMvc.perform(post("/api/rides/" + ride.getId() + "/stop")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stopBody()))
                .andExpect(status().isOk());

        Driver freed = driverRepository.findById(driver.getId()).orElseThrow();
        assertFalse(freed.isHasActiveRide(), "Driver should be freed after stopping");
    }

    @Test
    @DisplayName("A driver who isn't assigned to the ride cannot stop it (403)")
    void nonAssignedDriver_returns403() throws Exception {
        Ride ride = seedRide(RideStatus.IN_PROGRESS, driver);

        mockMvc.perform(post("/api/rides/" + ride.getId() + "/stop")
                        .header("Authorization", "Bearer " + otherDriverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stopBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A ride that isn't in progress cannot be stoped (409)")
    void nonActiveRide_returns409() throws Exception {
        Ride ride = seedRide(RideStatus.ASSIGNED, driver);

        mockMvc.perform(post("/api/rides/" + ride.getId() + "/stop")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stopBody()))
                .andExpect(status().isConflict());
    }

    // ---------- Helpers ----------

    private Ride seedRide(RideStatus status, Driver assigned) {
        Ride r = new Ride();
        r.setRider(rider);
        r.setDriver(assigned);
        r.setPickupAddress("Trg slobode");
        r.setPickupLatitude(45.2671);
        r.setPickupLongitude(19.8335);
        r.setDestinationAddress("Somewhere far");
        r.setDestinationLatitude(45.3000);
        r.setDestinationLongitude(19.9000);
        r.setVehicleType(VehicleType.STANDARD);
        r.setDistanceKM(10.0);
        r.setFare(ORIGINAL_FARE);
        r.setStatus(status);
        if (status == RideStatus.IN_PROGRESS) {
            r.setStartedAt(LocalDateTime.now().minusMinutes(10));
        }
        Ride saved = rideRepository.save(r);

        if (status == RideStatus.IN_PROGRESS) {
            assigned.setHasActiveRide(true);
            assigned.setAvailable(false);
            assigned.setCurrentRideId(saved.getId());
            driverRepository.save(assigned);
        }
        return saved;
    }

    private Driver makeDriver(User user, String plate) {
        Driver d = new Driver();
        d.setUser(user);
        d.setLoggedIn(true);
        d.setActive(true);
        d.setAvailable(true);
        d.setHasActiveRide(false);
        d.setWorkingMinutesLast24Hours(60);
        d = driverRepository.save(d);

        Vehicle v = new Vehicle();
        v.setDriver(d);
        v.setModel("Skoda Octavia");
        v.setVehicleType(VehicleType.STANDARD);
        v.setLicensePlate(plate);
        v.setSeats(4);
        v.setCurrentLatitude(45.2660);
        v.setCurrentLongitude(19.8340);
        vehicleRepository.save(v);

        return d;
    }

    private User makeUser(String first, String last, String email, Role role) {
        User u = new User(first, last, email, passwordEncoder.encode("password123"),
                "+381600000000", "Addr", role);
        u.setActivated(true);
        return userRepository.save(u);
    }
}
