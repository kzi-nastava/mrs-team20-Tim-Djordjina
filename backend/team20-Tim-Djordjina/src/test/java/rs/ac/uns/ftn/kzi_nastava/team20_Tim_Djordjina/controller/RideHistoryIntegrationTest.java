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
@DisplayName("Ride History - Integration Tests")
public class RideHistoryIntegrationTest {

    private static final String A_EMAIL = "a@test.com";
    private static final String B_EMAIL = "b@test.com";
    private static final String DRIVER_EMAIL = "driver@test.com";
    public static final String HISTORY_URL = "/api/rides/history";

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

    private User riderA, riderB;
    private Driver driver;
    private String tokenA, tokenB, tokenDriver;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cleanup();
        riderA = makeUser("Ann", "A", A_EMAIL, Role.USER);
        riderB = makeUser("Bob", "B", B_EMAIL, Role.USER);
        tokenA = jwtTokenProvider.generateToken(A_EMAIL, Role.USER.toString());
        tokenB = jwtTokenProvider.generateToken(B_EMAIL, Role.USER.toString());

        User driverUser = makeUser("Dana", "Driver", DRIVER_EMAIL, Role.DRIVER);
        driver = makeDriver(driverUser);
        tokenDriver = jwtTokenProvider.generateToken(DRIVER_EMAIL, Role.DRIVER.toString());
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
    @DisplayName("Rider history returns only their finished rides, newest first")
    void riderHistory_returnsOwnFinished_newestFirst() throws Exception {
        seedRide(riderA, RideStatus.FINISHED, LocalDateTime.now().minusHours(2));
        Ride newer = seedRide(riderA, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));
        seedRide(riderB, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));

        mockMvc.perform(get(HISTORY_URL)
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(newer.getId()));
    }

    @Test
    @DisplayName("Driver history returns their completed rides")
    void driverHistory_returnsDriverFinished() throws Exception {
        seedRide(riderA, RideStatus.FINISHED, LocalDateTime.now().minusHours(2));
        seedRide(riderB, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));

        mockMvc.perform(get(HISTORY_URL)
                        .header("Authorization", "Bearer " + tokenDriver))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("History excludes non-finished rides")
    void history_excludesNonFinished() throws Exception {
        seedRide(riderA, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));
        seedRide(riderA, RideStatus.ASSIGNED, null);
        seedRide(riderA, RideStatus.IN_PROGRESS, null);

        mockMvc.perform(get(HISTORY_URL)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Ride detail returns for the rider")
    void detail_returnsForRider() throws Exception {
        Ride ride = seedRide(riderA, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));

        mockMvc.perform(get(HISTORY_URL + "/" + ride.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ride.getId()))
                .andExpect(jsonPath("$.rider.firstName").value("Ann"))
                .andExpect(jsonPath("$.driver.firstName").value("Dana"));
    }

    @Test
    @DisplayName("Ride detail is forbidden for an unrelated user (403)")
    void detail_forStranger_returns403() throws Exception {
        Ride ride = seedRide(riderA, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));

        mockMvc.perform(get(HISTORY_URL + "/" + ride.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("History is empty when the user has no finished rides")
    void history_empty_whenNone() throws Exception {
        mockMvc.perform(get(HISTORY_URL)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    // ---------- Helpers ----------

    private Ride seedRide(User rider, RideStatus status, LocalDateTime finishedAt) {
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
        if (status == RideStatus.FINISHED){
            r.setStartedAt(finishedAt != null ? finishedAt.minusMinutes(15) : null);
            r.setFinishedAt(finishedAt);
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
        v.setLicensePlate("NS-HIST-1");
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
