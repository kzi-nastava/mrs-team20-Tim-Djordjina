package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RatingDTO;
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
@DisplayName("Ratings - Integration Tests")
public class RatingIntegrationTest {

    private static final String A_EMAIL = "a@test.com";
    private static final String B_EMAIL = "b@test.com";
    private static final String DRIVER_EMAIL = "driver@test.com";
    private static final String RIDES_URL = "/api/rides";

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

    private User riderA;
    private Driver driver;
    private String tokenA;
    private String tokenB;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        cleanup();
        riderA = makeUser("Ann", "A", A_EMAIL, Role.USER);
        makeUser("Bob", "B", B_EMAIL, Role.USER);
        tokenA = jwtTokenProvider.generateToken(A_EMAIL, Role.USER.toString());
        tokenB = jwtTokenProvider.generateToken(B_EMAIL, Role.USER.toString());

        User driverUser = makeUser("Dana", "Driver", DRIVER_EMAIL, Role.DRIVER);
        driver = makeDriver(driverUser);
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        ratingRepository.deleteAll();
        notificationRepository.deleteAll();
        rideRepository.deleteAll();
        vehicleRepository.deleteAll();
        driverRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("The rider can rate a finished ride within the window")
    void rate_validFinishedRide_succeeds() throws Exception {
        Ride ride = seedRide(riderA, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));

        mockMvc.perform(post(RIDES_URL + "/" + ride.getId() + "/rating")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ratingJson(5, 4, "Great ride")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverRating").value(5))
                .andExpect(jsonPath("$.vehicleRating").value(4));

        assertEquals(1, ratingRepository.count());
    }

    @Test
    @DisplayName("A non-owner cannot rate the ride (403)")
    void rate_byNonOwner_returns403() throws Exception {
        Ride ride = seedRide(riderA, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));

        mockMvc.perform(post(RIDES_URL + "/" + ride.getId() + "/rating")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ratingJson(5, 5, null)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A non-finished ride cannot be rated (409)")
    void rate_nonFinishedRide_returns409() throws Exception {
        Ride ride = seedRide(riderA, RideStatus.IN_PROGRESS, null);

        mockMvc.perform(post(RIDES_URL + "/" + ride.getId() + "/rating")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ratingJson(5, 5, null)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Rating after the 3-day window is rejected (409)")
    void rate_afterWindow_returns409() throws Exception {
        Ride ride = seedRide(riderA, RideStatus.FINISHED, LocalDateTime.now().minusDays(4));

        mockMvc.perform(post(RIDES_URL + "/" + ride.getId() + "/rating")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ratingJson(5, 5, null)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("A ride can only be rated once (409)")
    void rate_ratedTwice_returns409() throws Exception {
        Ride ride = seedRide(riderA, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));

        mockMvc.perform(post(RIDES_URL + "/" + ride.getId() + "/rating")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ratingJson(5, 4, null)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(RIDES_URL + "/" + ride.getId() + "/rating")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ratingJson(3, 3, null)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("A score outside 0-5 is rejected (400)")
    void rate_badScore_returns400() throws Exception {
        Ride ride = seedRide(riderA, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));

        mockMvc.perform(post(RIDES_URL + "/" + ride.getId() + "/rating")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ratingJson(9, 5, null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Driver summary returns the averages")
    void driverSummary_returnAverages() throws Exception {
        Ride firstRide = seedRide(riderA, RideStatus.FINISHED, LocalDateTime.now().minusHours(2));
        Ride secondRide = seedRide(riderA, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));
        rate(tokenA, firstRide.getId(), 5, 4);
        rate(tokenA, secondRide.getId(), 3, 2);

        String body = mockMvc.perform(get("/api/drivers/" + driver.getId() + "/rating")
                    .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var json = objectMapper.readTree(body);
        assertEquals(4.0, json.get("averageDriverRating").asDouble(), 0.01);
        assertEquals(3.0, json.get("averageVehicleRating").asDouble(), 0.01);
        assertEquals(2, json.get("ratingCount").asLong());
    }

    // ---------- Helpers ----------

    private void rate(String token, long rideId, int driverScore, int vehicleScore) throws Exception  {
        mockMvc.perform(post(RIDES_URL + "/" + rideId + "/rating")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ratingJson(driverScore, vehicleScore, null)))
                .andExpect(status().isCreated());
    }

    private String ratingJson(int driverScore, int vehicleScore, String comment) throws Exception {
        RatingDTO dto = new RatingDTO();
        dto.setDriverRating(driverScore);
        dto.setVehicleRating(vehicleScore);
        dto.setComment(comment);
        return objectMapper.writeValueAsString(dto);
    }

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
        if (finishedAt != null){
            r.setStartedAt(finishedAt.minusMinutes(15));
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
        v.setLicensePlate("NS-RATE-1");
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
