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
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integration tests for ride execution */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Ride Execution - Integration Tests")
public class RideExecutionIntegrationTest {


    public static final String RIDER_EMAIL = "rider@test.com";
    public static final String DRIVER1_EMAIL = "driver1@test.com";
    public static final String DRIVER2_EMAIL = "driver2@test.com";
    public static final String RIDES_URL = "/api/rides";

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
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
    private Driver driver1;
    private String riderToken;
    private String driver1Token;
    private String driver2Token;



    @BeforeEach
    void setUp(){
        cleanup();

        rider = makeUser("Rick", "Rider", RIDER_EMAIL, Role.USER);
        riderToken = jwtTokenProvider.generateToken(RIDER_EMAIL, Role.USER.toString());

        // driver1: eligible -> gets assigned
        User d1User = makeUser("Dana", "Driver", DRIVER1_EMAIL, Role.DRIVER);
        driver1 = makeDriver(d1User, true);
        makeVehicle(driver1, "NS-EXEC-1", 45.2600, 19.8340);
        driver1Token = jwtTokenProvider.generateToken(DRIVER1_EMAIL, Role.DRIVER.toString());

        // driver2: not eligible -> needs a valid token
        User d2User = makeUser("Dave", "OtherDriver", DRIVER2_EMAIL, Role.DRIVER);
        Driver driver2 = makeDriver(d2User, false);
        makeVehicle(driver2, "NS-EXEC-2", 45.3000, 19.9000);
        driver2Token = jwtTokenProvider.generateToken(DRIVER2_EMAIL, Role.DRIVER.toString());

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
    @DisplayName("Start transitions an assigned ride to IN_PROGRESS")
    void start_transitionsToInProgress() throws Exception {
        long rideId = createRideAsRider();

        mockMvc.perform(post(RIDES_URL + "/" + rideId + "/start")
                        .header("Authorization", "Bearer " + driver1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        Ride ride = rideRepository.findById(rideId).orElseThrow();
        assertEquals(RideStatus.IN_PROGRESS, ride.getStatus());
        assertNotNull(ride.getStartedAt());
    }

    @Test
    @DisplayName("Finish frees the driver and notifies the rider")
    void finish_freesDriverAndNotifiesRider() throws Exception {
        long rideId = createRideAsRider();
        start(rideId, driver1Token);

        mockMvc.perform(post(RIDES_URL + "/" + rideId + "/finish")
                        .header("Authorization", "Bearer " + driver1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"));

        Ride ride = rideRepository.findById(rideId).orElseThrow();
        assertEquals(RideStatus.FINISHED, ride.getStatus());
        assertNotNull(ride.getFinishedAt());

        Driver freed = driverRepository.findById(driver1.getId()).orElseThrow();
        assertFalse(freed.isHasActiveRide());
        assertTrue(freed.isAvailable());
        assertNull(freed.getCurrentRideId());

        boolean hasFinishNotif = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(rider.getId()).stream()
                .anyMatch(n -> n.getType() == NotificationType.RIDE_FINISHED);
        assertTrue(hasFinishNotif, "rider should get a RIDE_FINISHED notification");
    }

    @Test
    @DisplayName("A non-assigned driver cannot start the ride (403)")
    void start_byNonAssignedDriver_returns403() throws Exception {
        long rideId = createRideAsRider();

        mockMvc.perform(post(RIDES_URL + "/" + rideId + "/start")
                        .header("Authorization", "Bearer " + driver2Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Finishing a ride that hasn't started returns 409")
    void finish_notStarted_returns409() throws Exception {
        long rideId = createRideAsRider();

        mockMvc.perform(post(RIDES_URL + "/" + rideId + "/finish")
                        .header("Authorization", "Bearer " + driver1Token))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Starting an already-started ride returns (409)")
    void start_alreadyStarted_returns409() throws Exception {
        long rideId = createRideAsRider();
        start(rideId, driver1Token);

        mockMvc.perform(post(RIDES_URL + "/" + rideId + "/start")
                        .header("Authorization", "Bearer " + driver1Token))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Rider cannot order a new ride while one is active (409)")
    void requestRide_whileActive_returns409() throws Exception {
        createRideAsRider();

        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rideRequestJson()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Current ride endpoints: 204 when none, the ride once assigned")
    void currentRide_noneThenAssigned() throws Exception {
        mockMvc.perform(get(RIDES_URL + "/current")
                        .header("Authorization", "Bearer " + driver1Token))
                .andExpect(status().isNoContent());

        long rideId = createRideAsRider();

        mockMvc.perform(get(RIDES_URL + "/current")
                        .header("Authorization", "Bearer " + driver1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rideId));
    }

    @Test
    @DisplayName("Start without a token return 401")
    void start_noToken_returns401() throws Exception {
        mockMvc.perform(post(RIDES_URL + "/1/start"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Helpers ----------

    private long createRideAsRider() throws Exception {
        String body = mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rideRequestJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private void start(long rideId, String token) throws Exception {
        mockMvc.perform(post(RIDES_URL + "/" + rideId + "/start")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String rideRequestJson() throws Exception {
        RideRequestDTO dto = new RideRequestDTO();
        dto.setPickupAddress("Trg Slobode");
        dto.setPickupLatitude(45.2671);
        dto.setPickupLongitude(19.8335);
        dto.setDestinationAddress("Strand");
        dto.setDestinationLatitude(45.2400);
        dto.setDestinationLongitude(19.8500);
        dto.setVehicleType(VehicleType.STANDARD);
        return objectMapper.writeValueAsString(dto);
    }

    private User makeUser(String first, String last, String email, Role role) {
        User u = new User(first, last, email, passwordEncoder.encode("password123"),
                "+381600000000", "Addr", role);
        u.setActivated(true);
        return userRepository.save(u);
    }

    private Driver makeDriver(User user, boolean eligible) {
        Driver d = new Driver();
        d.setUser(user);
        d.setLoggedIn(eligible);
        d.setActive(eligible);
        d.setAvailable(eligible);
        d.setHasActiveRide(false);
        d.setWorkingMinutesLast24Hours(60);
        return driverRepository.save(d);
    }

    private void makeVehicle(Driver driver, String plate, double lat, double lng) {
        Vehicle v = new Vehicle();
        v.setDriver(driver);
        v.setModel("Skoda Octavia");
        v.setVehicleType(VehicleType.STANDARD);
        v.setLicensePlate(plate);
        v.setSeats(4);
        v.setCurrentLatitude(lat);
        v.setCurrentLongitude(lng);
        vehicleRepository.save(v);
    }
}
