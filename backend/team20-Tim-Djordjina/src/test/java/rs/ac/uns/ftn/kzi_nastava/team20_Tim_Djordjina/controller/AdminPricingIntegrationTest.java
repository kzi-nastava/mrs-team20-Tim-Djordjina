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
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.PricingConfigDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.dto.RideRequestDTO;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.model.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.repository.*;
import rs.ac.uns.ftn.kzi_nastava.team20_Tim_Djordjina.security.JwtTokenProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Admin Pricing - Integration Tests")
public class AdminPricingIntegrationTest {

    private static final String PRICING_URL = "/api/admin/pricing";
    private static final String RIDES_URL = "/api/rides";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String A_EMAIL = "a@test.com";
    private static final String B_EMAIL = "b@test.com";

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
    private PricingConfigRepository pricingConfigRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User riderA, riderB;
    private String adminToken, tokenA, tokenB;


    @BeforeEach
    void setUp() {
        cleanup();
        makeUser("Ada", "Admin", ADMIN_EMAIL, Role.ADMIN);
        riderA = makeUser("Ann", "A", A_EMAIL, Role.USER);
        riderB = makeUser("Bob", "B", B_EMAIL, Role.USER);

        adminToken = jwtTokenProvider.generateToken(ADMIN_EMAIL, Role.ADMIN.toString());
        tokenA = jwtTokenProvider.generateToken(A_EMAIL, Role.USER.toString());
        tokenB = jwtTokenProvider.generateToken(B_EMAIL, Role.USER.toString());
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
        pricingConfigRepository.deleteAll();
    }

    // ---------- Tests ----------

    @Test
    @DisplayName("Pricing returns the defaults before any change")
    void getPricing_returnsDefaults() throws Exception {
        mockMvc.perform(get(PRICING_URL)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.standardPrice").value(200.0))
                .andExpect(jsonPath("$.luxuryPrice").value(500.0))
                .andExpect(jsonPath("$.vanPrice").value(350.0))
                .andExpect(jsonPath("$.pricePerKm").value(120.0));
    }

    @Test
    @DisplayName("Admin can update pricing and it persists")
    void updatePricing_persists() throws Exception {
        mockMvc.perform(put(PRICING_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pricingJson(250, 600, 400, 130)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.standardPrice").value(250.0));
    }

    @Test
    @DisplayName("A ride's fare reflects the updated base price")
    void rideFare_reflectsUpdatedPrice() throws Exception {
        makeDriver("NS-PRICE-1", 45.2660, 19.8340);
        makeDriver("NS-PRICE-2", 45.2662, 19.8342);

        mockMvc.perform(post(RIDES_URL)
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rideRequestJson()))
                .andExpect(status().isCreated());
        double fareFirst = fareForRider(riderA.getId());

        mockMvc.perform(put(PRICING_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pricingJson(300, 500, 350, 120)))
                .andExpect(status().isOk());

        mockMvc.perform(post(RIDES_URL)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rideRequestJson()))
                .andExpect(status().isCreated());
        double fareSecond = fareForRider(riderB.getId());

        assertEquals(100.0, fareSecond - fareFirst, 0.01);
    }

    @Test
    @DisplayName("A non-admin cannot read pricing (403)")
    void nonAdmin_getPricing_returns403() throws Exception {
        mockMvc.perform(get(PRICING_URL)
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A non-admin cannot update pricing (403)")
    void nonAdmin_updatePricing_returns403() throws Exception {
        mockMvc.perform(put(PRICING_URL)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pricingJson(250,600,400,130)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A negative price is rejected (400)")
    void negativePrice_returns400() throws Exception {
        mockMvc.perform(put(PRICING_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pricingJson(-1,500,350,120)))
                .andExpect(status().isBadRequest());
    }


    // ---------- Helpers ----------

    private double fareForRider(Long riderId) {
        return rideRepository.findAll().stream()
                .filter(r -> r.getRider() != null && r.getRider().getId().equals(riderId))
                .findFirst().orElseThrow().getFare();
    }

    private String pricingJson(double std, double lux, double van, double perKm) throws Exception {
        return objectMapper.writeValueAsString(new PricingConfigDTO(std, lux, van, perKm));
    }

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

    private void makeDriver(String plate, double lat, double lng) {
        User u = makeUser("Drv" + plate, "D", plate + "@test.com", Role.DRIVER);
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
        v.setLicensePlate(plate);
        v.setSeats(4);
        v.setCurrentLatitude(lat);
        v.setCurrentLongitude(lng);
        vehicleRepository.save(v);
    }

    private User makeUser(String first, String last, String email, Role role) {
        User u = new User(first, last, email, passwordEncoder.encode("password123"),
                "+381600000000", "Addr", role);
        u.setActivated(true);
        return userRepository.save(u);
    }
}
