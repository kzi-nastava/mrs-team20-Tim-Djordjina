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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Linked Passengers - Integration Tests")
public class LinkedPassengerIntegrationTest {

    private static final String A_EMAIL = "creator@test.com";
    private static final String B_EMAIL = "passenger@test.com";
    private static final String DRIVER_EMAIL = "driver@test.com";
    private static final String STRANGER_EMAIL = "stranger@test.com";
    private static final String RIDES_URL = "/api/rides";

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private LinkedPassengerRepository linkedPassengerRepository;
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

    private String creatorToken;
    private String passengerToken;
    private String driverToken;
    private User passengerB;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        cleanup();

        makeUser("Ann", "Creator", A_EMAIL, Role.USER);
        creatorToken = jwtTokenProvider.generateToken(A_EMAIL, Role.USER.toString());

        passengerB = makeUser("Bob", "Passenger", B_EMAIL, Role.USER);
        passengerToken = jwtTokenProvider.generateToken(B_EMAIL, Role.USER.toString());

        makeFreeDriver();
        driverToken = jwtTokenProvider.generateToken(DRIVER_EMAIL, Role.DRIVER.toString());
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    /** FK safe order: notifications, linked_passengers, rides, vehicles, drivers, users */
    private void cleanup() {
        notificationRepository.deleteAll();
        linkedPassengerRepository.deleteAll();
        rideRepository.deleteAll();
        vehicleRepository.deleteAll();
        driverRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("Linking by email creates records; registered gets in-app, unregistered does not")
    void request_withLinkedEmails_createsLinksAndNotifiesRegistered() throws Exception {
        requestRide(List.of(B_EMAIL, STRANGER_EMAIL));

        assertEquals(2, linkedPassengerRepository.count(), "two links created");

        LinkedPassenger bLink = linkedPassengerRepository.findByEmail(B_EMAIL).get(0);
        assertNotNull(bLink.getUser(), "registered passenger resolves to a user");

        LinkedPassenger sLink = linkedPassengerRepository.findByEmail(STRANGER_EMAIL).get(0);
        assertNull(sLink.getUser(), "unregistered passenger has no user");

        long bNotifs = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(passengerB.getId()).size();
        assertTrue(bNotifs >= 1, "registered linked passenger gets an in-app notification");

    }

    @Test
    @DisplayName("A registered linked passenger sees the ride via /linked")
    void linkedRide_listedForRegisteredPassenger() throws Exception {
        long rideId = requestRide(List.of(B_EMAIL));

        mockMvc.perform(get(RIDES_URL + "/linked")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(rideId));
    }

    @Test
    @DisplayName("Finishing the ride notifies linked passengers")
    void finish_notifiesLinkedPassengers() throws Exception {
        long rideId = requestRide(List.of(B_EMAIL));

        mockMvc.perform(post(RIDES_URL + "/" + rideId + "/start")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk());
        mockMvc.perform(post(RIDES_URL + "/" + rideId + "/finish")
                        .header("Authorization", "Bearer " + driverToken))
                .andExpect(status().isOk());

        boolean hasFinish = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(passengerB.getId()).stream()
                .anyMatch(n -> n.getType() == NotificationType.RIDE_FINISHED);
        assertTrue(hasFinish, "linked passenger gets a RIDE_FINISHED notification");
    }

    @Test
    @DisplayName("The creator's own email is not linked")
    void selfLink_isSkipped() throws Exception {
        requestRide(List.of(A_EMAIL));
        assertEquals(0, linkedPassengerRepository.count(), "self-link is skipped");
    }


    // ---------- Helpers ----------

    private long requestRide(List<String> linkedEmails) throws Exception {
        RideRequestDTO dto = new RideRequestDTO();
        dto.setPickupAddress("Trg slobode");
        dto.setPickupLatitude(45.2671);
        dto.setPickupLongitude(19.8335);
        dto.setDestinationAddress("Strand");
        dto.setDestinationLatitude(45.2400);
        dto.setDestinationLongitude(19.8500);
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setLinkedPassengerEmails(linkedEmails);

        String body = mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private void makeFreeDriver() {
        User u = makeUser("Dana", "Driver", DRIVER_EMAIL, Role.DRIVER);
        Driver d = new Driver();
        d.setUser(u);
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
        v.setLicensePlate("NS-LINK-1");
        v.setSeats(4);
        v.setCurrentLatitude(45.2660);
        v.setCurrentLongitude(19.8340);
        vehicleRepository.save(v);
    }

    private User makeUser(String first, String last, String email, Role role) {
        User u = new User(first, last, email,
                passwordEncoder.encode("password123"), "+381600000000", "Addr", role);
        u.setActivated(true);
        return userRepository.save(u);
    }
}
