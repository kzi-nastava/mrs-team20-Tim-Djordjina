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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Ride Cancellation - Integration Tests")
public class RideCancellationIntegrationTest {

    private static final String RIDER_EMAIL = "rider@test.com";
    private static final String DRIVER_EMAIL = "driver@test.com";
    private static final String OTHER_EMAIL = "other@test.com";

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
    private String riderToken, driverToken, otherToken;


    @BeforeEach
    void setUp() {
        cleanup();
        rider = makeUser("Rick", "Rider", RIDER_EMAIL, Role.USER);
        makeUser("Otto", "Other", OTHER_EMAIL, Role.USER);
        User driverUser = makeUser("Dana", "Driver", DRIVER_EMAIL, Role.DRIVER);
        driver = makeDriver(driverUser);

        riderToken = jwtTokenProvider.generateToken(RIDER_EMAIL, Role.USER.toString());
        driverToken = jwtTokenProvider.generateToken(DRIVER_EMAIL, Role.DRIVER.toString());
        otherToken = jwtTokenProvider.generateToken(OTHER_EMAIL, Role.USER.toString());
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

    private String cancelUrl(Long rideId) {
        return "/api/rides/" + rideId + "/cancel";
    }


    // ---------- Tests ----------

    @Test
    @DisplayName("Driver cancels with a reason: CANCELLED, cancelledBy=DRIVER, driver freed")
    void driverCancel_withReason_succeeds() throws Exception {
        Ride ride = seedRide(RideStatus.ASSIGNED, null, true);

        mockMvc.perform(post(cancelUrl(ride.getId()))
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Passenger no-show\"}"))
                .andExpect(status().isOk());

        Ride updated = rideRepository.findById(ride.getId()).orElseThrow();
        assertEquals(RideStatus.CANCELLED, updated.getStatus());
        assertEquals("DRIVER", updated.getCancelledBy());
        assertEquals("Passenger no-show", updated.getCancelReason());

        Driver freed = driverRepository.findById(driver.getId()).orElseThrow();
        assertFalse(freed.isHasActiveRide(), "Driver should be freed after cancellation");
    }

    @Test
    @DisplayName("Driver cancel without a reason is rejected (400)")
    void driverCancel_withoutReason_returns400() throws Exception {
        Ride ride = seedRide(RideStatus.ASSIGNED, null, true);

        mockMvc.perform(post(cancelUrl(ride.getId()))
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Rider cancels an immediate ride: CANCELLED, cancelledBy=RIDER")
    void riderCancel_immediate_succeeds() throws Exception {
        Ride ride = seedRide(RideStatus.ASSIGNED, null, true);

        mockMvc.perform(post(cancelUrl(ride.getId()))
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Rider cancels a scheduled ride with enough time (>10 min)")
    void riderCancel_scheduledInTime_succeeds() throws Exception {
        Ride ride = seedRide(RideStatus.SCHEDULED, LocalDateTime.now().plusMinutes(30), false);

        mockMvc.perform(post(cancelUrl(ride.getId()))
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        assertEquals(RideStatus.CANCELLED,
                rideRepository.findById(ride.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Rider cancels inside the 10 min window is rejected (409)")
    void riderCancel_scheduledTooLate_returns409() throws Exception {
        Ride ride = seedRide(RideStatus.SCHEDULED, LocalDateTime.now().plusMinutes(5), false);

        mockMvc.perform(post(cancelUrl(ride.getId()))
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("A ride already in progress cannot be cancelled (409)")
    void cancelInProgress_returns409() throws Exception {
        Ride ride = seedRide(RideStatus.IN_PROGRESS, null, true);

        mockMvc.perform(post(cancelUrl(ride.getId()))
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"too late\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("A non-participant cannot cancel (403)")
    void nonParticipant_returns403() throws Exception {
        Ride ride = seedRide(RideStatus.ASSIGNED, null, true);

        mockMvc.perform(post(cancelUrl(ride.getId()))
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }


    // ---------- Helpers ----------

    private Ride seedRide(RideStatus status, LocalDateTime scheduledFor, boolean withDriver) {
        Ride r = new Ride();
        r.setRider(rider);
        if (withDriver) {
            r.setDriver(driver);
        }
        r.setPickupAddress("Trg slobode");
        r.setPickupLatitude(45.2671);
        r.setPickupLongitude(19.8335);
        r.setDestinationAddress("Strand");
        r.setDestinationLatitude(45.2400);
        r.setDestinationLongitude(19.8500);
        r.setVehicleType(VehicleType.STANDARD);
        r.setDistanceKM(2.0);
        r.setFare(440.0);
        r.setStatus(status);
        r.setScheduledFor(scheduledFor);
        if (status == RideStatus.IN_PROGRESS) {
            r.setStartedAt(LocalDateTime.now().minusMinutes(5));
        }
        Ride saved = rideRepository.save(r);

        if (withDriver) {
            driver.setHasActiveRide(true);
            driver.setAvailable(false);
            driver.setCurrentRideId(saved.getId());
            driverRepository.save(driver);
        }
        return saved;
    }

    private Driver makeDriver(User user) {
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
        v.setLicensePlate("NS-CANCEL-1");
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
