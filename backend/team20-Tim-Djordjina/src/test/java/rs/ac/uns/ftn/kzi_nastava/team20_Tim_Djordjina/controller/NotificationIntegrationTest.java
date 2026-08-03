package rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.controller;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.ResultMatcher;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integrated test for persisted notifications */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Notifications - Integration Tests")
public class NotificationIntegrationTest {

    public static final String RIDER_EMAIL = "rider@notif.com";
    public static final String DRIVER_EMAIL = "driver@notif.com";
    public static final String RIDES_URL = "/api/rides";
    public static final String NOTIF_URL = "/api/notifications";

    private String riderToken;
    private String driverToken;
    private Driver driver;

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

    @BeforeEach
    void setup() {
        cleanup();

        User rider = new User("Rick", "Rider", RIDER_EMAIL,
                passwordEncoder.encode("password123"), "+381600000001", "Addr 1", Role.USER);
        rider.setActivated(true);
        userRepository.save(rider);
        riderToken = jwtTokenProvider.generateToken(RIDER_EMAIL, Role.USER.toString());

        User driverUser = new User("Dana", "Driver", DRIVER_EMAIL,
                passwordEncoder.encode("password123"), "+381600000002", "Addr 2", Role.DRIVER);
        driverUser.setActivated(true);
        driverUser = userRepository.save(driverUser);
        driverToken = jwtTokenProvider.generateToken(DRIVER_EMAIL, Role.DRIVER.toString());

        driver = new Driver();
        driver.setUser(driverUser);
        driver.setLoggedIn(true);
        driver.setActive(true);
        driver.setAvailable(true);
        driver.setHasActiveRide(false);
        driver.setWorkingMinutesLast24Hours(60);
        driver = driverRepository.save(driver);

        Vehicle vehicle = new Vehicle();
        vehicle.setDriver(driver);
        vehicle.setModel("Skoda Octavia");
        vehicle.setVehicleType(VehicleType.STANDARD);
        vehicle.setLicensePlate("NS-NOTIF-1");
        vehicle.setSeats(4);
        vehicle.setCurrentLatitude(45.2660);
        vehicle.setCurrentLongitude(19.8340);
        vehicleRepository.save(vehicle);
    }

    @AfterEach
    void tearDown(){
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
    @DisplayName("A successful ride notifies both the driver and the rider")
    void successfulRide_createsDriverAndRiderNotifications() throws Exception {
        requestRideExpecting(status().isCreated());

        assertTrue(listContainsType(driverToken, "NEW_RIDE"),
                "driver should have a NEW_RIDE notification");
        assertTrue(listContainsType(riderToken, "RIDE_ACCEPTED"),
                "rider should have a RIDE_ACCEPTED notification");
    }

    @Test
    @DisplayName("No available driver still notifies the rider (rollback-safe RIDE_FAILED)")
    void noDriver_createsFailedNotifications() throws Exception {
        // Mark the driver ineligible
        driver.setAvailable(false);
        driver.setLoggedIn(false);
        driver.setActive(false);
        driverRepository.save(driver);

        requestRideExpecting(status().isConflict());    // 409, ride rolled back

        assertEquals(0, rideRepository.count(), "no ride should be persisted");
        assertTrue(listContainsType(riderToken, "RIDE_FAILED"),
                "rider should still have a RIDE_FAILED notification despite the rollback");
    }

    @Test
    @DisplayName("A user only sees their own notifications")
    void list_returnsOnlyOwnNotifications() throws Exception {
        requestRideExpecting(status().isCreated());

        assertFalse(listContainsType(driverToken, "RIDE_ACCEPTED"),
                "driver should NOT see the rider's RIDE_ACCEPTED");
        assertFalse(listContainsType(riderToken, "NEW_RIDE"),
                "rider should NOT see the driver's NEW_RIDE");
    }

    @Test
    @DisplayName("Marking own notification read works")
    void markRead_ownNotification_succeeds() throws Exception {
        requestRideExpecting(status().isCreated());

        Long id = firstNotificationId(riderToken);

        mockMvc.perform(post(NOTIF_URL + "/" + id + "/read")
                        .header("Authorization", "Bearer " + riderToken))
                .andExpect(status().isOk());

        JsonNode list = getList(riderToken);
        boolean read = false;
        for (JsonNode n : list) {
            if (n.get("id").asLong() == id) read = n.get("read").asBoolean();
        }
        assertTrue(read, "the notification should be marked read");
    }

    @Test
    @DisplayName("Cannot mark another user's notification read")
    void markRead_othersNotification_returns400() throws Exception {
        requestRideExpecting(status().isCreated());

        Long riderNotifId = firstNotificationId(riderToken);

        mockMvc.perform(post(NOTIF_URL + "/" + riderNotifId + "/read")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Notifications require authentication")
    void list_noToken_returns401() throws Exception {
        mockMvc.perform(get(NOTIF_URL))
                .andExpect(status().isUnauthorized());
    }

        // ---------- Helpers ----------

    private void requestRideExpecting(ResultMatcher matcher) throws Exception {
        RideRequestDTO dto = new RideRequestDTO();
        dto.setPickupAddress("Trg slobode");
        dto.setPickupLatitude(45.2671);
        dto.setPickupLongitude(19.8335);
        dto.setDestinationAddress("Strand");
        dto.setDestinationLatitude(45.2400);
        dto.setDestinationLongitude(19.8500);
        dto.setVehicleType(VehicleType.STANDARD);

        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(matcher);
    }



    private JsonNode getList(String token) throws Exception {
        String body = mockMvc.perform(get(NOTIF_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private boolean listContainsType(String token, String type) throws Exception {
        for (JsonNode n : getList(token)) {
            if (type.equals(n.get("type").asText())) return true;
        }
        return false;
    }

    private Long firstNotificationId(String token) throws Exception {
        JsonNode list = getList(token);
        assertTrue(list.size() > 0, "expected at least one notification");
        return list.get(0).get("id").asLong();
    }

}
