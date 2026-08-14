package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.nullness.qual.NonNull;
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
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Driver State - Integration Tests")
public class DriverStateIntegrationTest {


    private static final String DRIVER_EMAIL = "driver@test.com";
    private static final String RIDER_EMAIL = "rider@test.com";
    private static final String STATE_URL = "/api/drivers/me/state";
    private static final String ACTIVE_URL = "/api/drivers/me/active";
    private static final String LOGOUT_URL = "/api/drivers/me/logout";
    private static final String RIDES_URL = "/api/rides";

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
    @Autowired
    private ObjectMapper objectMapper;

    private Driver driver;
    private String driverToken, riderToken;


    @BeforeEach
    void setUp() {
        cleanup();
        User driverUser = makeUser("Dana", "Driver", DRIVER_EMAIL, Role.DRIVER);
        driver = makeDriver(driverUser);
        driverToken = jwtTokenProvider.generateToken(DRIVER_EMAIL, Role.DRIVER.toString());

        makeUser("Rick", "Rider", RIDER_EMAIL, Role.USER);
        riderToken = jwtTokenProvider.generateToken(RIDER_EMAIL, Role.USER.toString());
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
    @DisplayName("State returns the driver's current status")
    void getState_returnsStatus() throws Exception {
        mockMvc.perform(get(STATE_URL)
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("Going off-duty sets available false and returns the new state")
    void setActive_offDuty_returnsState() throws Exception {
        mockMvc.perform(post(ACTIVE_URL)
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getStatusJson(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @DisplayName("An off-duty driver is not matched")
    void offDuty_driverNotMatched() throws Exception {
        setActive(false);

        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rideRequestJson()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Toggling back on-duty makes the driver matchable again")
    void onDuty_driverMatched() throws Exception {
        setActive(false);
        setActive(true);

        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rideRequestJson()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Logout during an active ride is blocked (409)")
    void logout_duringRide_returns409() throws Exception {
        driver.setHasActiveRide(true);
        driverRepository.save(driver);

        mockMvc.perform(post(LOGOUT_URL)
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Logout when free clears the driver's flags")
    void logout_whenFree_clearsFlags() throws Exception {
        mockMvc.perform(post(LOGOUT_URL)
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk());

        Driver after = driverRepository.findById(driver.getId()).orElseThrow();
        assertFalse(after.isLoggedIn());
        assertFalse(after.isActive());
        assertFalse(after.isAvailable());
    }


    // ---------- Helpers ----------

    private String rideRequestJson() throws Exception {
        RideRequestDTO dto = new RideRequestDTO();
        dto.setPickupAddress("Trg slobode");
        dto.setPickupLatitude(45.2671);
        dto.setPickupLongitude(19.8335);
        dto.setDestinationAddress("Strand");
        dto.setDestinationLatitude(45.2400);
        dto.setDestinationLongitude(19.8500);
        dto.setVehicleType(VehicleType.STANDARD);
        return objectMapper.writeValueAsString(dto);
    }

    private void setActive(boolean active) throws Exception {
        mockMvc.perform(post(ACTIVE_URL)
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getStatusJson(active)))
                .andExpect(status().isOk());
    }

    private static @NonNull String getStatusJson(boolean active) {
        return "{\"active\": " + active + "}";
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
