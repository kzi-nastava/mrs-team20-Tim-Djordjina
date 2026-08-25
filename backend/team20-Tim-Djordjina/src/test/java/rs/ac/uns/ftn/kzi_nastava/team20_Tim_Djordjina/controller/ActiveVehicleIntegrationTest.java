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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.DriverRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.RideRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.UserRepository;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.VehicleRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Active Vehicles (Home Map) - Integration Tests")
public class ActiveVehicleIntegrationTest {

    private static final String URL = "/api/vehicles/active";

    private Long freeVehicleId;
    private Long busyVehicleId;
    private Long offDutyVehicleId;
    private Long loggedOutVehicleId;
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
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        cleanup();

        // on-duty, free, has coords -> should appear, busy = false
        freeVehicleId = makeDriverWithVehicle("free@test.com", true, true, false, 45.2671, 19.8335, "NS-FREE-1");
        // on-duty, on a ride, has coords -> should appear, busy = true
        busyVehicleId = makeDriverWithVehicle("busy@test.com", true, true, true, 45.2550, 19.8450, "NS-BUSY-1");
        // off-duty (isActive = false) -> excluded
        offDutyVehicleId = makeDriverWithVehicle("off@test.com", true, false, false, 45.2600, 19.8300, "NS-OFF-1");
        // logged out -> excluded
        loggedOutVehicleId = makeDriverWithVehicle("out@test.com", false, true, false, 45.2620, 19.8320, "NS-OUT-1");
        // on-duty but no coords -> excluded
        makeDriverWithVehicle("nocoord@test.com", true, true, false, null, null, "NS-NOCOORD-1");
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        rideRepository.deleteAll();
        vehicleRepository.deleteAll();
        driverRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("Public (no token) returns only on-duty vehicles with coordinates")
    void publicAccess_returnsOnDutyVehicles() throws Exception {
        MvcResult result = mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(arr.isArray());
        assertEquals(2, arr.size(), "Only the two on-duty vehicles with coordinates");

        assertNotNull(findById(arr, freeVehicleId), "Free vehicle should be present");
        assertNotNull(findById(arr, busyVehicleId), "Busy vehicle should be present");
    }

    @Test
    @DisplayName("Busy flag reflects whether the driver has an active ride")
    void busyFlag_isCorrect() throws Exception {
        MvcResult result = mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString());

        assertFalse(findById(arr, freeVehicleId).get("busy").asBoolean(), "free -> busy=false");
        assertTrue(findById(arr, busyVehicleId).get("busy").asBoolean(), "on a ride -> busy=true");
    }

    @Test
    @DisplayName("Off-duty, logged-out, and no-coordinate vehicles are excluded")
    void excludedVehicles_notReturned() throws Exception {
        MvcResult result = mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode arr = objectMapper.readTree(result.getResponse().getContentAsString());

        assertNull(findById(arr, offDutyVehicleId), "off-duty excluded");
        assertNull(findById(arr, loggedOutVehicleId), "logged-out excluded");
        assertEquals(2, arr.size());
    }

    // ---------- Helpers ----------

    private JsonNode findById(JsonNode arr, Long vehicleId) {
        for (JsonNode n : arr) {
            if (n.get("vehicleId").asLong() == vehicleId) return n;
        }
        return null;
    }

    private Long makeDriverWithVehicle(String email, boolean loggedIn, boolean active,
                                  boolean hasActiveRide, Double lat, Double lng, String plate) {
        User u = new User("Dana", "Driver", email,
                passwordEncoder.encode("password123"), "+381600000000", "Addr", Role.DRIVER);
        u.setActivated(true);
        u = userRepository.save(u);

        Driver d = new Driver();
        d.setUser(u);
        d.setLoggedIn(loggedIn);
        d.setActive(active);
        d.setAvailable(!hasActiveRide);
        d.setHasActiveRide(hasActiveRide);
        d.setWorkingMinutesLast24Hours(60);
        d = driverRepository.save(d);

        Vehicle v = new Vehicle();
        v.setDriver(d);
        v.setModel("Skoda Octavia");
        v.setVehicleType(VehicleType.STANDARD);
        v.setLicensePlate(plate);
        v.setSeats(4);
        v.setBabyTransport(false);
        v.setPetTransport(false);
        v.setCurrentLatitude(lat);
        v.setCurrentLongitude(lng);
        v = vehicleRepository.save(v);
        return v.getId();
    }
}
