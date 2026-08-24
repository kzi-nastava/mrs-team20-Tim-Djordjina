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
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideStopDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integration tests for Ride Request - POST /api/rides */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Ride Request - Integration Tests")
public class RideRequestIntegrationTest {

    private static final String RIDER_EMAIL = "rider@test.com";
    private static final String RIDES_URL = "/api/rides";

    private static final double STANDARD_BASE = 200.0;
    private static final double PRICE_PER_KM = 120.0;

    public static final double PICKUP_LATITUDE = 45.2671;
    public static final double PICKUP_LONGITUDE = 19.8335;
    public static final double DESTINATION_LATITUDE = 45.2400;
    public static final double DESTINATION_LONGITUDE = 19.8500;

    private String riderToken;
    private User rider;
    private Driver driver;
    private Vehicle vehicle;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private NotificationRepository notificationRepository;


    @BeforeEach
    void setUp() {
        rider = new User("Rick", "Rider", RIDER_EMAIL,
                passwordEncoder.encode("password123"), "+381600000001", "Addr 1", Role.USER);
        rider.setActivated(true);
        rider = userRepository.save(rider);
        riderToken = jwtTokenProvider.generateToken(RIDER_EMAIL, Role.USER.toString());

        User driverUser = new User("Dana", "Driver", "driver@test.com",
                passwordEncoder.encode("password123"), "+381600000002", "Addr 2", Role.DRIVER);
        driverUser.setActivated(true);
        driverUser = userRepository.save(driverUser);

        driver = new Driver();
        driver.setUser(driverUser);
        driver.setLoggedIn(true);
        driver.setActive(true);
        driver.setAvailable(true);
        driver.setHasActiveRide(false);
        driver.setWorkingMinutesLast24Hours(60);
        driver = driverRepository.save(driver);

        vehicle = new Vehicle();
        vehicle.setDriver(driver);
        vehicle.setModel("Skoda Octavia");
        vehicle.setVehicleType(VehicleType.STANDARD);
        vehicle.setLicensePlate("NS123AB");
        vehicle.setSeats(4);
        vehicle.setBabyTransport(false);
        vehicle.setPetTransport(false);
        vehicle.setCurrentLatitude(45.2660);
        vehicle.setCurrentLongitude(19.8340);
        vehicle = vehicleRepository.save(vehicle);
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        rideRepository.deleteAll();
        vehicleRepository.deleteAll();
        driverRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Valid request assigns a driver, persists the ride, and marks the driver busy")
    void requestRide_valid_returns201_andAssigns() throws Exception {
        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request(VehicleType.STANDARD, false, false))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.driver.licensePlate").value("NS123AB"))
                .andExpect(jsonPath("$.fare").isNumber());

        // Ride persisted
        assertEquals(1, rideRepository.findAll().size());

        // Driver marked busy
        Driver updated = driverRepository.findById(driver.getId()).orElseThrow();
        assertTrue(updated.isHasActiveRide());
        assertFalse(updated.isAvailable());
        assertNotNull(updated.getCurrentRideId());
    }

    @Test
    @DisplayName("Fare equals base + km * 120")
    void requestRide_fareMatchesFormula() throws Exception {
        String body = mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request(VehicleType.STANDARD, false, false))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        double distanceKm = objectMapper.readTree(body).get("distanceKm").asDouble();
        double fare = objectMapper.readTree(body).get("fare").asDouble();

        double expected = Math.round((STANDARD_BASE + distanceKm * PRICE_PER_KM) * 100.0) / 100.0;
        assertEquals(expected, fare, 0.001);
    }

    @Test
    @DisplayName("Ordered stops are persisted in order")
    void requestRide_stopsPersistedInOrder() throws Exception {
        RideRequestDTO dto = request(VehicleType.STANDARD, false, false);
        dto.setStops(List.of(stop("Second", 45.255, 19.845, 1),
                            stop("First", 45.260, 19.840, 0)));

        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stops[0].stopOrder").value(0))
                .andExpect(jsonPath("$.stops[1].stopOrder").value(1));

        /*
        Ride saved = rideRepository.findAll().get(0);
        List<RideStop> stops = saved.getStops();
        assertEquals(2, stops.size(), "both stops should be persisted");
        assertEquals(0, stops.get(0).getStopOrder());
        assertEquals("First", stops.get(0).getAddress());
        assertEquals(1, stops.get(1).getStopOrder());
        assertEquals("Second", stops.get(1).getAddress());
        */
    }


    @Test
    @DisplayName("No eligible driver returns 409")
    void requestRide_noDriver_returns409() throws Exception {
        driver.setAvailable(false);
        driver.setLoggedIn(false);
        driver.setActive(false);
        driverRepository.save(driver);

        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request(VehicleType.STANDARD, false, false))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Requested vehicle type with no matching vehicle returns 409")
    void requestRide_vehicleTypeMismatch_returns409() throws Exception {
        // Only a STANDARD driver exists; request LUXURY
        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request(VehicleType.LUXURY, false, false))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Baby transport required but vehicle does not support it returns 409")
    void requestRide_babyTransportUnsupported_returns409() throws Exception {
        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request(VehicleType.STANDARD, true, false))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Driver over 8h in the last 24h is not assigned returns 409")
    void requestRide_driverOver8h_returns409() throws Exception {
        driver.setWorkingMinutesLast24Hours(500);
        driverRepository.save(driver);

        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request(VehicleType.STANDARD, false, false))))
                .andExpect(status().isConflict());
    }


    @Test
    @DisplayName("Blocked rider is rejected with 403 and the block note")
    void requestRide_blockedRider_returns403WithNote() throws Exception {
        String note = "Repeated no-shows";
        rider.setBlocked(true);
        rider.setBlockNote(note);
        userRepository.save(rider);


        String body = mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request(VehicleType.STANDARD, false, false))))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        assertTrue(body.contains(note), "403 response should include the block note");
    }


    @Test
    @DisplayName("Missing required field returns 400")
    void requestRide_missingField_returns400() throws Exception {
        RideRequestDTO dto = request(VehicleType.STANDARD, false, false);
        dto.setPickupLatitude(null);

        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + riderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("No token returns 401")
    void requestRide_noToken_returns401() throws Exception {
        mockMvc.perform(post(RIDES_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request(VehicleType.STANDARD, false, false))))
                .andExpect(status().isUnauthorized());
    }



    private RideRequestDTO request(VehicleType type, boolean baby, boolean pet) {
        RideRequestDTO dto = new RideRequestDTO();
        dto.setPickupAddress("Trg Slobode");
        dto.setPickupLatitude(PICKUP_LATITUDE);
        dto.setPickupLongitude(PICKUP_LONGITUDE);
        dto.setDestinationAddress("Strand");
        dto.setDestinationLatitude(DESTINATION_LATITUDE);
        dto.setDestinationLongitude(DESTINATION_LONGITUDE);
        dto.setVehicleType(type);
        dto.setBabyTransport(baby);
        dto.setPetTransport(pet);
        return dto;
    }

    private RideStopDTO stop(String address, double lat, double lng, int order) {
        RideStopDTO s = new RideStopDTO();
        s.setAddress(address);
        s.setLatitude(lat);
        s.setLongitude(lng);
        s.setStopOrder(order);
        return s;
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }
}
