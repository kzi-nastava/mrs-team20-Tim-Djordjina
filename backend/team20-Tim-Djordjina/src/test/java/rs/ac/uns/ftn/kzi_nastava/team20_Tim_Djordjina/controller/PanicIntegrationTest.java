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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Panic Button - Integration Tests")
public class PanicIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String RIDER_EMAIL = "rider@test.com";
    private static final String OTHER_EMAIL = "other@test.com";
    private static final String DRIVER_EMAIL = "driver@test.com";
    private static final String ADMIN_PANICS_URL = "/api/admin/panics";
    private static final String RIDES_URL = "/api/rides";

    @Autowired
    private PanicRepository panicRepository;
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

    private User rider;
    private Driver driver;
    private String adminToken, riderToken,driverToken, otherToken;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cleanup();
        makeUser("Ada", "Admin", ADMIN_EMAIL, Role.ADMIN);
        rider = makeUser("Rick", "Rider", RIDER_EMAIL, Role.USER);
        makeUser("Otto", "Other", OTHER_EMAIL, Role.USER);
        User driverUser = makeUser("Dana", "Driver", DRIVER_EMAIL, Role.DRIVER);
        driver = makeDriver(driverUser);

        adminToken = jwtTokenProvider.generateToken(ADMIN_EMAIL, Role.ADMIN.toString());
        riderToken = jwtTokenProvider.generateToken(RIDER_EMAIL, Role.USER.toString());
        otherToken = jwtTokenProvider.generateToken(OTHER_EMAIL, Role.USER.toString());
        driverToken = jwtTokenProvider.generateToken(DRIVER_EMAIL, Role.DRIVER.toString());
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        panicRepository.deleteAll();
        notificationRepository.deleteAll();
        rideRepository.deleteAll();
        vehicleRepository.deleteAll();
        driverRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("Rider can trigger panic on their active ride; admin is notified")
    void riderTriggersPanic_succeeds() throws Exception {
        Ride ride = seedRide(RideStatus.ASSIGNED);

        mockMvc.perform(post(RIDES_URL + "/" + ride.getId() + "/panic")
                .header("Authorization", "Bearer " + riderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\": \"Help\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rideId").value(ride.getId()))
                .andExpect(jsonPath("$.triggeredByName").value("Rick Rider"));

        assertEquals(1, panicRepository.count());
        assertTrue(notificationRepository.count() >= 1, "admin should be notified");
    }

    @Test
    @DisplayName("Assigned driver can trigger panic on their active ride")
    void driverTriggersPanic_succeeds() throws Exception {
        Ride ride = seedRide(RideStatus.IN_PROGRESS);

        mockMvc.perform(post(RIDES_URL + "/" + ride.getId() + "/panic")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());

        assertEquals(1, panicRepository.count());
    }

    @Test
    @DisplayName("A non-participant cannot trigger panic (403)")
    void nonParticipant_returns403() throws Exception {
        Ride ride = seedRide(RideStatus.IN_PROGRESS);

        mockMvc.perform(post(RIDES_URL + "/" + ride.getId() + "/panic")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Panic cannot be triggered on a non-active ride (409)")
    void nonActiveRide_returns409() throws Exception {
        Ride ride = seedRide(RideStatus.FINISHED);

        mockMvc.perform(post(RIDES_URL + "/" + ride.getId() + "/panic")
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Admin sees the panic list; non-admin is forbidden")
    void adminSeesPanics_nonAdmin403() throws Exception {
        Ride ride = seedRide(RideStatus.ASSIGNED);

        mockMvc.perform(post(RIDES_URL + "/" + ride.getId() + "/panic")
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\": \"Help\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get(ADMIN_PANICS_URL)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rideId").value(ride.getId()));

        mockMvc.perform(get(ADMIN_PANICS_URL)
                        .header("Authorization", "Bearer " + riderToken))
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
        r.setDestinationLatitude(45.2400);
        r.setDestinationLongitude(19.8500);
        r.setVehicleType(VehicleType.STANDARD);
        r.setDistanceKM(2.0);
        r.setFare(440.0);
        r.setStatus(status);
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
        v.setLicensePlate("NS-PANIC-1");
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
