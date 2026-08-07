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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.service.RideSchedulerService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for scheduled rides (US#2.4.1)
 * */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "ride.scheduler.interval-ms=3600000")
@DisplayName("Ride Scheduling - Integration Tests")
public class RideSchedulingIntegrationTest {

    public static final String RIDER_EMAIL = "rider@test.com";
    public static final String RIDES_URL = "/api/rides";
    public static final double PICKUP_LAT = 45.2671;
    public static final double PICKUP_LNG = 19.8335;
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

    private String riderToken;
    private User rider;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RideSchedulerService rideSchedulerService;

    @BeforeEach
    void setUp() {
        cleanup();
        rider = makeUser("Sam", "Rider", RIDER_EMAIL, Role.USER);
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

    // ---------- Create ----------

    @Test
    @DisplayName("Scheduling <= 5h ahead creates a SCHEDULER ride with no driver")
    void schedule_valid_createsScheduledRideWithoutDriver() throws Exception {
        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rideJson(LocalDateTime.now().plusHours(2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        assertEquals(1, rideRepository.count());
        Ride ride = rideRepository.findAll().get(0);
        assertEquals(RideStatus.SCHEDULED, ride.getStatus());
        assertNull(ride.getDriver(), "A scheduled ride has no driver yet");
        assertNotNull(ride.getScheduledFor());
    }

    @Test
    @DisplayName("Scheduling more than 5h ahead is rejected (400)")
    void schedule_tooFarAhead_returns400() throws Exception {
        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rideJson(LocalDateTime.now().plusHours(6))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Scheduling in the past is rejected (400)")
    void schedule_inPast_returns400() throws Exception {
        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rideJson(LocalDateTime.now().minusHours(1))))
                .andExpect(status().isBadRequest());
    }

    // ---------- Scheduler activation ----------

    @Test
    @DisplayName("The scheduler assigns a driver to a due scheduled ride")
    void scheduler_activatesDueRide_assignsDriver() throws Exception {
        Driver driver = makeFreeDriver();
        Ride scheduled = seedDueScheduledRide();

        rideSchedulerService.assignDueScheduledRides();

        Ride after = rideRepository.findById(scheduled.getId()).orElseThrow();
        assertEquals(RideStatus.ASSIGNED, after.getStatus());
        assertNotNull(after.getDriver());
        assertEquals(driver.getId(), after.getDriver().getId());

        Driver busy = driverRepository.findById(driver.getId()).orElseThrow();
        assertTrue(busy.isHasActiveRide());
        assertFalse(busy.isAvailable());
        assertEquals(scheduled.getId(), busy.getCurrentRideId());
    }

    @Test
    @DisplayName("A due scheduled ride stays SCHEDULED when no driver is available")
    void scheduler_noDriver_leavesScheduled() throws Exception {
        Ride scheduled = seedDueScheduledRide();

        rideSchedulerService.assignDueScheduledRides();

        Ride after = rideRepository.findById(scheduled.getId()).orElseThrow();
        assertEquals(RideStatus.SCHEDULED, after.getStatus());
        assertNull(after.getDriver());
    }

    @Test
    @DisplayName("A rider can schedule a future ride even while on an active one")
    void schedule_whileActive_allowed() throws Exception {
        Driver driver = makeFreeDriver();
        Ride active = new Ride();
        active.setRider(rider);
        active.setDriver(driver);
        active.setPickupAddress("P");
        active.setPickupLatitude(PICKUP_LAT);
        active.setPickupLongitude(PICKUP_LNG);
        active.setDestinationAddress("D");
        active.setDestinationLatitude(45.2400);
        active.setDestinationLongitude(19.8500);
        active.setVehicleType(VehicleType.STANDARD);
        active.setStatus(RideStatus.IN_PROGRESS);
        rideRepository.save(active);

        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rideJson(LocalDateTime.now().plusHours(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }


        // ---------- Helpers ----------

    private String rideJson(LocalDateTime scheduledFor) throws Exception {
        RideRequestDTO dto = new RideRequestDTO();
        dto.setPickupAddress("Trg Slobode");
        dto.setPickupLatitude(PICKUP_LAT);
        dto.setPickupLongitude(PICKUP_LNG);
        dto.setDestinationAddress("Strand");
        dto.setDestinationLatitude(45.2400);
        dto.setDestinationLongitude(19.8500);
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setScheduledFor(scheduledFor);
        return objectMapper.writeValueAsString(dto);
    }

    private Ride seedDueScheduledRide() {
        Ride r = new Ride();
        r.setRider(rider);
        r.setPickupAddress("Trg slobode");
        r.setPickupLatitude(PICKUP_LAT);
        r.setPickupLongitude(PICKUP_LNG);
        r.setDestinationAddress("Strand");
        r.setDestinationLatitude(45.2400);
        r.setDestinationLongitude(19.8500);
        r.setVehicleType(VehicleType.STANDARD);
        r.setDistanceKM(2.0);
        r.setFare(440.0);
        r.setStatus(RideStatus.SCHEDULED);
        r.setScheduledFor(LocalDateTime.now().minusMinutes(1));
        return rideRepository.save(r);
    }

    private Driver makeFreeDriver() {
        User u = makeUser("Dana", "Driver", "driver@test.com", Role.DRIVER);
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
        v.setLicensePlate("NS-SCHED-1");
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
