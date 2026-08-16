package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Admin Ride-State View - Integration Tests")
public class AdminRideStateIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String A_EMAIL = "a@test.com";
    private static final String B_EMAIL = "b@test.com";
    private static final String DRIVER_EMAIL = "driver@test.com";
    public static final String ADMIN_RIDES_URL = "/api/admin/rides";

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

    private User riderA, riderB;
    private Driver driver;
    private String adminToken, userToken;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cleanup();
        makeUser("Ada", "Admin", ADMIN_EMAIL, Role.ADMIN);
        riderA = makeUser("Ann", "A", A_EMAIL, Role.USER);
        riderB = makeUser("Bob", "B", B_EMAIL, Role.USER);
        adminToken = jwtTokenProvider.generateToken(ADMIN_EMAIL, Role.ADMIN.toString());
        userToken = jwtTokenProvider.generateToken(A_EMAIL, Role.USER.toString());

        User driverUser = makeUser("Dana", "Driver", DRIVER_EMAIL, Role.DRIVER);
        driver = makeDriver(driverUser);
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

    // ---------- Tests ----------

    @Test
    @DisplayName("Admin sees all rides across users and statuses")
    void adminSeesAllRides() throws Exception {
        seedRide(riderA, RideStatus.FINISHED);
        seedRide(riderB, RideStatus.IN_PROGRESS);
        seedRide(riderA, RideStatus.SCHEDULED);

        mockMvc.perform(get(ADMIN_RIDES_URL)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("Status filter narrows the list")
    void statusFilter_narrows() throws Exception {
        seedRide(riderA, RideStatus.FINISHED);
        seedRide(riderB, RideStatus.IN_PROGRESS);
        seedRide(riderA, RideStatus.SCHEDULED);

        mockMvc.perform(get(ADMIN_RIDES_URL).param("status", "IN_PROGRESS")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Admin can fetch a ride's detail")
    void detail_returnsRide() throws Exception {
        Ride ride = seedRide(riderA, RideStatus.FINISHED);

        mockMvc.perform(get(ADMIN_RIDES_URL + "/" + ride.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ride.getId()))
                .andExpect(jsonPath("$.rider.firstName").value("Ann"));
    }

    @Test
    @DisplayName("A non-admin cannot list rides (403)")
    void nonAdmin_list_returns403() throws Exception {
        mockMvc.perform(get(ADMIN_RIDES_URL)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A non-admin cannot fetch ride detail (403)")
    void nonAdmin_detail_returns403() throws Exception {
        Ride ride = seedRide(riderA, RideStatus.FINISHED);

        mockMvc.perform(get(ADMIN_RIDES_URL + "/" + ride.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("An invalid status filter is rejected (400)")
    void badStatus_returns400() throws Exception {
        mockMvc.perform(get(ADMIN_RIDES_URL).param("status", "INVALID")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }


    // ---------- Helpers ----------

    private Ride seedRide(User rider, RideStatus status) {
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
        if (status == RideStatus.FINISHED) {
            r.setStartedAt(LocalDateTime.now().minusMinutes(20));
            r.setFinishedAt(LocalDateTime.now().minusMinutes(5));
        } else if (status == RideStatus.IN_PROGRESS){
            r.setStartedAt(LocalDateTime.now().minusMinutes(10));
        }
        return rideRepository.save(r);
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
        v.setLicensePlate("NS-RIDESTATE-1");
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
