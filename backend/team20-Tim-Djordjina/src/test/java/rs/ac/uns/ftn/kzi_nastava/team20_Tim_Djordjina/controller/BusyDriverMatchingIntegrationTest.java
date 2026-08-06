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
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Busy-Driver Matching - Integration Tests")
public class BusyDriverMatchingIntegrationTest {

    public static final String RIDER_EMAIL = "requester@test.com";
    public static final String RIDES_URL = "/api/rides";
    public static final double PICKUP_LAT = 45.2671;
    public static final double PICKUP_LNG = 19.8335;

    private String riderToken;
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
    void setUp() {
        cleanup();
        User rider = makeUser("Requester", "Rider", RIDER_EMAIL, Role.USER);
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
    @DisplayName("A free driver is preferred over a busy one")
    void freeDriverPreferredOverBusy() throws Exception {
        Driver free = makeFreeDriver("free", 45.2660, 19.8340);
        makeBusyNearFinishingDriver("busy", 45.2662, 19.8342, 2.0, false);

        long assignedId = requestRideExpectingAssigned();
        assertEquals(free.getId(), assignedId, "the free driver should be chosen");
    }

    @Test
    @DisplayName("A busy driver ~10 min from finishing is matched when none are free")
    void busyNearFinishingDriver_matchedWhenNoneFree() throws Exception {
        Driver busy = makeBusyNearFinishingDriver("busy", 45.2600, 19.8340, 2.0, false);

        long assignedId = requestRideExpectingAssigned();
        assertEquals(busy.getId(), assignedId, "the busy near-finishing driver should be chosen");
    }

    @Test
    @DisplayName("A busy driver far from finishing is not matched")
    void busyDriverFarFromFinishing_returns409() throws Exception {
        makeBusyNearFinishingDriver("busy", 45.2600, 19.8340, 100.0, false);

        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rideRequestJson()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("A busy near-finishing driver with a scheduled future ride is excluded")
    void busyDriverWithScheduledRide_returns409() throws Exception {
        makeBusyNearFinishingDriver("busy", 45.2600, 19.8340, 2.0, true);

        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rideRequestJson()))
                .andExpect(status().isConflict());
    }


    // ---------- Helpers ----------

    private long requestRideExpectingAssigned() throws Exception {
        String body = mockMvc.perform(post(RIDES_URL)
                                        .header("Authorization", "Bearer " + riderToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(rideRequestJson()))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("driver").get("driverId").asLong();
    }

    private String rideRequestJson() throws Exception {
        RideRequestDTO dto = new RideRequestDTO();
        dto.setPickupAddress("Trg slobode");
        dto.setPickupLatitude(PICKUP_LAT);
        dto.setPickupLongitude(PICKUP_LNG);
        dto.setDestinationAddress("Strand");
        dto.setDestinationLatitude(45.2400);
        dto.setDestinationLongitude(19.8500);
        dto.setVehicleType(VehicleType.STANDARD);
        return objectMapper.writeValueAsString(dto);
    }

    private Driver makeFreeDriver(String tag, double lat, double lng) {
        User u = makeUser("Free", tag, tag + "@test.com", Role.DRIVER);
        Driver d = makeDriver(u, true, false);
        makeVehicle(d, "NS-" + tag, lat, lng);
        return d;
    }

    private Driver makeBusyNearFinishingDriver(String tag, double lat, double lng, double distanceKm, boolean withScheduled) {
        User u = makeUser("Busy", tag, tag + "@test.com", Role.DRIVER);
        Driver d = makeDriver(u, false, true);
        makeVehicle(d, "NS-" + tag, lat, lng);

        User otherRider = makeUser("Other", tag, "other-" + tag + "@test.com", Role.USER);

        Ride current = new Ride();
        current.setRider(otherRider);
        current.setDriver(d);
        current.setPickupAddress("A");
        current.setPickupLatitude(lat);
        current.setPickupLongitude(lng);
        current.setDestinationAddress("B");
        current.setDestinationLatitude(45.25);
        current.setDestinationLongitude(19.85);
        current.setVehicleType(VehicleType.STANDARD);
        current.setDistanceKM(distanceKm);
        current.setFare(200 + distanceKm * 120);
        current.setStatus(RideStatus.IN_PROGRESS);
        current.setStartedAt(LocalDateTime.now());
        current = rideRepository.save(current);

        d.setCurrentRideId(current.getId());
        driverRepository.save(d);

        if (withScheduled) {
            Ride scheduled = new Ride();
            scheduled.setRider(otherRider);
            scheduled.setDriver(d);
            scheduled.setPickupAddress("C");
            scheduled.setPickupLatitude(45.26);
            scheduled.setPickupLongitude(19.83);
            scheduled.setDestinationAddress("D");
            scheduled.setDestinationLatitude(45.27);
            scheduled.setDestinationLongitude(19.84);
            scheduled.setVehicleType(VehicleType.STANDARD);
            scheduled.setStatus(RideStatus.ASSIGNED);
            scheduled.setScheduledFor(LocalDateTime.now().plusHours(2));
            rideRepository.save(scheduled);
        }
        return d;
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

    private Driver makeDriver(User user, boolean available, boolean hasActiveRide) {
        Driver d = new Driver();
        d.setUser(user);
        d.setLoggedIn(true);
        d.setActive(true);
        d.setAvailable(available);
        d.setHasActiveRide(hasActiveRide);
        d.setWorkingMinutesLast24Hours(60);
        d = driverRepository.save(d);
        return d;
    }

    private User makeUser(String first, String last, String email, Role role) {
        User u = new User(first, last, email, passwordEncoder.encode("password123"),
                "+381600000000", "Addr", role);
        u.setActivated(true);
        return userRepository.save(u);
    }
}
