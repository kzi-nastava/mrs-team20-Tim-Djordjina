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
@DisplayName("Admin Per User History - Integration Tests")
public class AdminUserHistoryIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String RIDER_EMAIL = "rider@test.com";
    private static final String DRIVER_EMAIL = "driver@test.com";

    @Autowired
    private PanicRepository panicRepository;
    @Autowired
    private RatingRepository ratingRepository;
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
    private User driverUser;
    private Driver driver;
    private String adminToken, riderToken;

    @BeforeEach
    void setUp() {
        cleanup();
        makeUser("Ada", "Admin", ADMIN_EMAIL, Role.ADMIN);
        rider = makeUser("Rick", "Rider", RIDER_EMAIL, Role.USER);
        driverUser = makeUser("Dana", "Driver", DRIVER_EMAIL, Role.DRIVER);
        driver = makeDriver(driverUser);

        adminToken = jwtTokenProvider.generateToken(ADMIN_EMAIL, Role.ADMIN.toString());
        riderToken = jwtTokenProvider.generateToken(RIDER_EMAIL, Role.USER.toString());
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        panicRepository.deleteAll();
        ratingRepository.deleteAll();
        notificationRepository.deleteAll();
        rideRepository.deleteAll();
        vehicleRepository.deleteAll();
        driverRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String getUserHistoryUrl(User user) {
        return "/api/admin/users/" + user.getId() + "/history";
    }

    private String getRideFullUrl(Ride ride) {
        return "/api/admin/rides/" + ride.getId() + "/full";
    }

    //---------- Tests ----------

    @Test
    @DisplayName("Admin sees a rider's rides, newest first")
    void adminSeesRiderRides_newestFirst() throws Exception {
        seedRide(rider, RideStatus.FINISHED, LocalDateTime.now().minusDays(2));
        Ride newer = seedRide(rider, RideStatus.FINISHED, LocalDateTime.now().minusDays(1));

        mockMvc.perform(get(getUserHistoryUrl(rider))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ride.id").value(newer.getId()));
    }

    @Test
    @DisplayName("Rows are enriched with panic flag and ratings")
    void enrichment_panicAndRatings() throws Exception {
        Ride ride = seedRide(rider, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));
        seedRating(ride,5,4);
        seedPanic(ride, rider);

        mockMvc.perform(get(getUserHistoryUrl(rider))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].panicTriggered").value(true))
                .andExpect(jsonPath("$[0].driverRating").value(5))
                .andExpect(jsonPath("$[0].vehicleRating").value(4))
                .andExpect(jsonPath("$[0].cancelled").value(false));
    }

    @Test
    @DisplayName("A cancelled ride is flagged")
    void cancelledFlag() throws Exception {
        seedRide(rider, RideStatus.CANCELLED, LocalDateTime.now().minusHours(1));

        mockMvc.perform(get(getUserHistoryUrl(rider))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cancelled").value(true));
    }

    @Test
    @DisplayName("Date filter narrows the list")
    void dateFilter_narrowsList() throws Exception {
        seedRide(rider, RideStatus.FINISHED, LocalDateTime.now().minusDays(10));
        seedRide(rider, RideStatus.FINISHED, LocalDateTime.now().minusDays(1));

        mockMvc.perform(get(getUserHistoryUrl(rider))
                        .param("from", LocalDateTime.now().minusDays(5).toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Admin sees a driver's rides")
    void driverHistory() throws Exception {
        seedRide(rider, RideStatus.FINISHED, LocalDateTime.now().minusHours(2));
        seedRide(rider, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));

        mockMvc.perform(get(getUserHistoryUrl(driverUser))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("A non-admin is forbidden")
    void nonAdmin_returns403() throws Exception {
        mockMvc.perform(get(getUserHistoryUrl(rider))
                        .header("Authorization", "Bearer " + riderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Full ride detail is enriched")
    void rideFull_enriched() throws Exception {
        Ride ride = seedRide(rider, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));
        seedPanic(ride, rider);

        mockMvc.perform(get(getRideFullUrl(ride))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ride.id").value(ride.getId()))
                .andExpect(jsonPath("$.panicTriggered").value(true));
    }

    //---------- Helpers ----------

    private void seedRating(Ride ride, int driverScore, int vehicleScore) {
        Rating rating = new Rating();
        rating.setRide(ride);
        rating.setRater(ride.getRider());
        rating.setDriverRating(driverScore);
        rating.setVehicleRating(vehicleScore);
        ratingRepository.save(rating);
    }

    private void seedPanic(Ride ride, User by) {
        Panic panic = new Panic();
        panic.setRide(ride);
        panic.setTriggeredBy(by);
        panicRepository.save(panic);
    }

    private Ride seedRide(User rider, RideStatus status, LocalDateTime createdAt) {
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
        r.setCreatedAt(createdAt);
        if (status == RideStatus.FINISHED) {
            r.setStartedAt(createdAt.plusMinutes(2));
            r.setFinishedAt(createdAt.plusMinutes(15));
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
        v.setLicensePlate("NS-USERHISTORY-1");
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
